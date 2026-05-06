package io.github.kukpt.modbus;

import io.github.kukpt.modbus.repository.core.DatabaseService;
import io.smallrye.mutiny.Uni;

import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.config.ConfigRetriever;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  public static final String CONFIG_FILE = "modbus-vertx.yaml";


  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new MainVerticle())
         .invoke(id -> log.info("deploy id: {}", id))
         .subscribe()
         .with(id -> log.info("start id: {}", id), throwable ->
             log.error("start error: {}", throwable, throwable));
  }

  @Override
  public Uni<Void> asyncStart() {
    return doConfig(vertx)
        .chain(this::createSessionFactory)
        .chain(this::deploy)
        .replaceWithVoid();
  }

  /**
   * 部署verticle
   *
   * @param conf
   * @return
   */
  private Uni<JsonObject> deploy(JsonObject conf) {
    return Uni.join().all(
                  vertx.deployVerticle(new ModbusConnContext(), new DeploymentOptions().setConfig(conf)),
                  vertx.deployVerticle(new ModbusWebServer(), new DeploymentOptions().setConfig(conf))
              ).andCollectFailures()
              .replaceWith(conf);
  }

  /**
   * 创建SessionFactory
   *
   * @param conf
   * @return
   */
  private Uni<JsonObject> createSessionFactory(JsonObject conf) {

    Map<String, Object> db = conf.getJsonObject("db")
                                 .getMap();
    return vertx.executeBlocking(Uni.createFrom().item(() -> {
      DatabaseService.initialize(db);
      return conf;
    }));
  }

  /**
   * 处理配置文件
   *
   * @param vertx
   * @return
   */
  private static Uni<JsonObject> doConfig(io.vertx.mutiny.core.Vertx vertx) {
    String userDir = System.getProperty("user.dir");
    String cliConfigDir = new StringBuilder(userDir).append(File.separator).append("config").append(File.separator)
                                                    .append(CONFIG_FILE).toString();

    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject().put("path", "config" + File.separator + CONFIG_FILE));
    ConfigStoreOptions config = new ConfigStoreOptions()
        .setOptional(true)
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject().put("path", cliConfigDir));
    ConfigRetrieverOptions opts = new ConfigRetrieverOptions();
    opts.addStore(defaultConfig)
        .addStore(config);
    File file = new File(cliConfigDir);
    ConfigStoreOptions cliConfig = new ConfigStoreOptions();
    if (file.exists()) {
      cliConfig
          .setType("file")
          .setFormat("yaml")
          .setConfig(new JsonObject().put("path", cliConfigDir));
      opts.addStore(cliConfig);
    }

    ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);
    cfgRetriever.listen(c -> {
      JsonObject ch = c.getNewConfiguration();
      log.info("config file change event: {}", ch);
    });

    return cfgRetriever
        .getConfig()
        .invoke(conf -> log.info("config: {}", conf))
        .onFailure().invoke(err -> log.error("获取配置文件失败!", err));
  }

}
