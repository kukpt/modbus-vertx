package io.github.kukpt.modbus;

import io.github.kukpt.modbus.common.RespResult;
import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterLocator;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.UniHelper;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.handler.BodyHandler;
import io.vertx.mutiny.ext.web.handler.TimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ModbusWebServer extends BaseVerticle {

  private static final Integer DEFAULT_HTTP_PORT = 11885;

  private Integer httpPort;

  private void init() {
    JsonObject http = config().getJsonObject("http");
    this.httpPort = http.getInteger("port", DEFAULT_HTTP_PORT);
  }

  private Router createRouter() {
    Router router = Router.router(vertx);
    router.route().handler(TimeoutHandler.create(TimeUnit.SECONDS.toMillis(30)));
    router.route().handler(BodyHandler.create());
    // 设备
    // 添加设备
    router.post("/device/").handler(this::addDevice);
    // 修改设备
    router.put("/device/").handler(this::editDevice);
    // 删除设备
    router.delete("/device/:id").handler(this::deleteDevice);
    // 查看设备
    router.get("/device/:page/:pageSize").handler(this::getDevices);
    // 查看设备
    router.get("/device/:id").handler(this::getDevice);
    // 寄存器定位
    // 添加寄存器定位
    router.post("/device/locator").handler(this::addLocator);
    // 修改寄存器定位
    router.put("/device/locator").handler(this::editLocator);
    // 添加模板
    router.post("/device/template").handler(this::addTemplate);
    return router;
  }

  private void editLocator(RoutingContext ctx) {
    RegisterLocator locator = ctx.body().asPojo(RegisterLocator.class);

    repository.locator().merge(locator)
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("修改寄存器定位失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("修改寄存器定位失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void getDevice(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));
    repository.device().findDeviceWithTemplateById(id)
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("查看设备失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("查看设备失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void addTemplate(RoutingContext ctx) {
    RegisterTemplate template = ctx.body().asPojo(RegisterTemplate.class);
    repository.template().persist(template)
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("添加模板失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("添加模板失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void addLocator(RoutingContext ctx) {
    RegisterLocator locator = ctx.body().asPojo(RegisterLocator.class);

    repository.locator().persist(locator)
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("添加定位器失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("添加定位器失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void deleteDevice(RoutingContext ctx) {
    long id = Long.parseLong(ctx.pathParam("id"));

    repository.device().deleteById(id)
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("删除设备失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("删除设备失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void editDevice(RoutingContext ctx) {
    ModbusDevice device = ctx.body().asPojo(ModbusDevice.class);
    repository.device().merge(device)
              .replaceWithVoid()
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("修改设备失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("修改设备失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void getDevices(RoutingContext ctx) {
    int page = Integer.parseInt(ctx.pathParam("page"));
    int pageSize = Integer.parseInt(ctx.pathParam("pageSize"));
    repository.device().findDeviceWithTemplate(page, pageSize)
              .map(RespResult::ok)
              .onFailure().invoke(err -> log.error("获取列表失败!", err))
              .onFailure().recoverWithItem(err -> RespResult.error("获取列表失败!"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  private void addDevice(RoutingContext ctx) {
    ModbusDevice device = ctx.body().asPojo(ModbusDevice.class);

    repository.device().persist(device)
              .map(d -> RespResult.ok(d))
              .onFailure().invoke(err -> log.error("添加设备失败", err))
              .onFailure().recoverWithItem(err -> RespResult.error("添加设备失败"))
              .chain(ctx::json)
              .subscribe().with(UniHelper.NOOP);
  }

  @Override
  public Uni<Void> asyncStart() {
    init();
    return vertx.createHttpServer()
                .requestHandler(createRouter())
                .listen(httpPort)
                .invoke(s -> log.info("启动HTTP Server Port = {}", s.actualPort()))
                .onFailure().invoke(err -> log.error("启动HTTP Server失败!", err))
                .replaceWithVoid();
  }


  private Set<HttpMethod> allowedMethods = Set.of(
      HttpMethod.POST,
      HttpMethod.GET,
      HttpMethod.PUT,
      HttpMethod.DELETE,
      HttpMethod.OPTIONS);

  private Set<String> allowedHeaders = Set.of(
      "x-requested-with",
      "Access-Control-Allow-Origin",
      "origin",
      "Content-Type",
      "accept");
}
