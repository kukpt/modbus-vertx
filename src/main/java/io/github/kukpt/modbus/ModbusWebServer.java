package io.github.kukpt.modbus;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterLocator;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.entity.dto.RegisterTemplateDto;
import io.github.kukpt.modbus.entity.dto.TemplateLocatorDto;
import io.github.kukpt.modbus.repository.core.QuerySpec;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.handler.BodyHandler;
import io.vertx.mutiny.ext.web.handler.TimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
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
    // 应用配置
    router.post("/device/apply/:id").handler(this::apply);
    // 设备
    // 添加设备
    router.post("/device/").handler(this::addDevice);
    // 修改设备
    router.put("/device/").handler(this::editDevice);
    // 删除设备
    router.delete("/device/:id").handler(this::deleteDevice);
    // 查看设备
    router.get("/device/list/:page/:pageSize").handler(this::getDevices);
    // 查看设备
    router.get("/device/:id").handler(this::getDevice);
    // 寄存器定位
    // 添加寄存器定位
    router.post("/device/locator").handler(this::addLocator);
    // 删除寄存器定位
    router.delete("/device/locator/:id").handler(this::deleteLocator);
    // 修改寄存器定位
    router.put("/device/locator").handler(this::editLocator);
    // 查看寄存器定位列表
    router.get("/device/locator/list/:page/:pageSize").handler(this::getLocators);
    // 查看寄存器定位
    router.get("/device/locator/:id").handler(this::getLocator);
    // 寄存器定位模板
    // 添加模板
    router.post("/device/template").handler(this::addTemplate);
    // 删除模板
    router.delete("/device/template/:id").handler(this::deleteTemplate);
    // 修改模板
    router.put("/device/template").handler(this::editTemplate);
    // 查看模板
    router.get("/device/template/:id").handler(this::getTemplate);
    // 查看模板列表
    router.get("/device/template/list/:page/:pageSize").handler(this::getTemplates);
    return router;
  }

  /**
   * 应用配置
   *
   * @param ctx
   */
  private void apply(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    Uni<Void> handler =
        repository.device().findDeviceWithTemplateById(id)
                  .onItem().ifNull().failWith(new RuntimeException("Device with id " + id + " not found"))
                  .map(JsonObject::mapFrom)
                  .chain(v ->
                      vertx.eventBus().<JsonObject>request(
                          ModbusConnContext.REPLACE_MODBUS_CONN_TOPIC,
                          v,
                          new DeliveryOptions().setSendTimeout(TimeUnit.SECONDS.toMillis(30)))
                  ).replaceWithVoid();
    handleResponse(handler, ctx, "应用配置失败");
  }


  /**
   * 添加定位器
   *
   * @param ctx
   */
  private void addLocator(RoutingContext ctx) {
    RegisterLocator locator = parseBody(ctx, RegisterLocator.class);
    handleResponse(repository.locator().persist(locator), ctx, "添加定位器失败");
  }

  /**
   * 删除定位器
   *
   * @param ctx
   */
  private void deleteLocator(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    handleResponse(repository.locator().deleteById(id), ctx, "删除寄存器定位失败");
  }

  /**
   * 修改定位器
   *
   * @param ctx
   */
  private void editLocator(RoutingContext ctx) {
    RegisterLocator locator = parseBody(ctx, RegisterLocator.class);
    handleResponse(repository.locator().merge(locator).replaceWithVoid(), ctx, "修改寄存器定位失败");
  }

  /**
   * 查看定位器
   *
   * @param ctx
   */
  private void getLocator(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    handleResponse(repository.locator().findById(id), ctx, "查看定位器失败");
  }

  /**
   * 查看定位器列表
   *
   * @param ctx
   */
  private void getLocators(RoutingContext ctx) {
    int page = parsePage(ctx);
    int pageSize = parsePageSize(ctx);
    handleResponse(repository.locator().findPage(page, pageSize), ctx, "获取列表失败");
  }

  /**
   * 添加设备
   *
   * @param ctx
   */
  private void addDevice(RoutingContext ctx) {
    ModbusDevice device = parseBody(ctx, ModbusDevice.class);
    handleResponse(repository.device().persist(device), ctx, "添加设备失败");
  }

  /**
   * 删除设备
   *
   * @param ctx
   */
  private void deleteDevice(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    Uni<Void> handler = repository.device().deleteById(id)
                                   .chain(unused ->
                                       vertx.eventBus().request(
                                           ModbusConnContext.DEL_MODBUS_CONN_TOPIC,
                                           new JsonObject().put("deviceId", id),
                                           new DeliveryOptions().setSendTimeout(TimeUnit.SECONDS.toMillis(30)))
                                   ).replaceWithVoid();
    handleResponse(handler, ctx, "删除设备失败");
  }

  /**
   * 修改设备
   *
   * @param ctx
   */
  private void editDevice(RoutingContext ctx) {
    ModbusDevice device = parseBody(ctx, ModbusDevice.class);
    handleResponse(repository.device().merge(device).replaceWithVoid(), ctx, "修改设备失败");
  }

  /**
   * 查看设备
   *
   * @param ctx
   */
  private void getDevice(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    handleResponse(repository.device().findDeviceWithTemplateById(id), ctx, "查看设备失败");
  }

  /**
   * 查看设备列表
   *
   * @param ctx
   */
  private void getDevices(RoutingContext ctx) {
    int page = parsePage(ctx);
    int pageSize = parsePageSize(ctx);
    handleResponse(repository.device().findDeviceWithTemplate(page, pageSize), ctx, "获取列表失败!");
  }

  /**
   * 新增模板
   *
   * @param ctx
   */
  private void addTemplate(RoutingContext ctx) {
    RegisterTemplate template = parseBody(ctx, RegisterTemplate.class);
    handleResponse(repository.template().persist(template), ctx, "添加模板失败");
  }

  /**
   * 删除
   *
   * @param ctx
   */
  private void deleteTemplate(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    handleResponse(repository.template().deleteById(id), ctx, "删除模板失败");
  }

  /**
   * 修改模板
   *
   * @param ctx
   */
  private void editTemplate(RoutingContext ctx) {
    RegisterTemplateDto template = parseBody(ctx, RegisterTemplateDto.class);
    QuerySpec spec = QuerySpec.create()
                              .in("id", Arrays.asList(template.getLocators()));
    Uni<Void> handler = repository.template().findDeviceWithTemplateById(template.getId())
                                  .chain(v -> {
                                    v.getRegisterLocators().clear();
                                    return Uni.createFrom().item(v);
                                  })
                                  .chain(v ->
                                      repository.locator().findBySpec(spec)
                                                .map(o -> {
                                                  v.setRegisterLocators(o);
                                                  return v;
                                                })
                                  )
                                  .chain(v -> repository.template().merge(v).replaceWithVoid());


    handleResponse(handler, ctx, "修改模板失败");
  }

  /**
   * 查看模板
   *
   * @param ctx
   */
  private void getTemplate(RoutingContext ctx) {
    long id = parseId(ctx, "id");
    handleResponse(repository.template().findDeviceWithTemplateById(id), ctx, "查看模板失败");
  }

  /**
   * 查看模板列表
   *
   * @param ctx
   */
  private void getTemplates(RoutingContext ctx) {
    int page = parsePage(ctx);
    int pageSize = parsePageSize(ctx);
    handleResponse(repository.template().findTemplateWithLocators(page, pageSize), ctx, "查看模板列表失败");
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
