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
import io.vertx.core.json.jackson.DatabindCodec;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;



@Slf4j
public class MainVerticle extends AbstractVerticle {
  // 在启动类或 Verticle 的 start 方法中最上方执行
  static {
    // 注册 JavaTimeModule 以支持 LocalDateTime
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
    // 可选：禁用将日期序列化为时间戳数字，改为标准的 ISO-8601 字符串
    DatabindCodec.mapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 如果你有多个 ObjectMapper（比如专门处理美化输出的），建议也一并注册
    DatabindCodec.prettyMapper().registerModule(new JavaTimeModule());
    DatabindCodec.prettyMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
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
    Map<String, Object> db = Map.of(
        "jakarta.persistence.spi.PersistenceProvider", conf.getString("_MOD_DB_PROVIDER"),
        "jakarta.persistence.jdbc.url", conf.getString("_MOD_DB_URL"),
        "jakarta.persistence.jdbc.user", conf.getString("_MOD_DB_USER"),
        "jakarta.persistence.jdbc.password", conf.getString("_MOD_DB_PASSWORD"),
        "hibernate.physical_naming_strategy", conf.getString("_MOD_DB_PHYSICAL_NAMING_STRATEGY"),
        "hibernate.show_sql", conf.getString("_MOD_DB_SHOW_SQL"),
        "hibernate.format_sql", conf.getString("_MOD_DB_FORMAT_SQL"),
        "hibernate.highlight_sql", conf.getString("_MOD_DB_HIGHLIGHT_SQL")
    );

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

    ConfigRetrieverOptions opts = new ConfigRetrieverOptions();


    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject().put("path", "config" + File.separator + CONFIG_FILE));
    opts.addStore(defaultConfig);

    ConfigStoreOptions config = new ConfigStoreOptions()
        .setOptional(true)
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject().put("path", cliConfigDir));
    opts.addStore(config);

    File file = new File(cliConfigDir);
    ConfigStoreOptions cliConfig = new ConfigStoreOptions();
    if (file.exists()) {
      cliConfig
          .setType("file")
          .setFormat("yaml")
          .setConfig(new JsonObject().put("path", cliConfigDir));
      opts.addStore(cliConfig);
    }

    ConfigStoreOptions env = new ConfigStoreOptions()
        .setType("env")
        .setConfig(new JsonObject().put("raw-data", true));
    opts.addStore(env);


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
