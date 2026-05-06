package io.github.kukpt.modbus;

import io.github.kukpt.modbus.entity.*;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;


public class ModbusTcp {

  /**
   * modbusCtx中循环问询到的modbus寄存器数据处理
   */
  private Handler<DeviceValue> valueHandler;
  /**
   * modbus网关在线状态处理
   */
  private Handler<JsonObject> onlineStateHandler;

  private final Vertx vertx;

//  private void deployCtx(Vertx vertx, ModbusTcpOptions options) {
//    ModbusConnContext ctx = new ModbusConnContext();
//    if (options != null) {
//      ctx = new ModbusConnContext(options.getPoolSize(), options.getQueryInterval()
//      , options.getReconnectionInterval());
//    }
//    vertx.deployVerticle(ctx);
//  }

  public ModbusTcp(Vertx vertx, ModbusTcpOptions options) {
    this.vertx = vertx;
//    deployCtx(vertx, options);
    // 问询到的寄存器数据处理
    vertx.eventBus().<JsonObject>localConsumer(ModbusConnContext.SAVE_VALUE, this::saveValueHandler);
    // 网关在线状态
    vertx.eventBus().<JsonObject>localConsumer(ModbusConnContext.ONLINE_STATE, this::onlineStateHandler);
  }

  private void onlineStateHandler(Message<JsonObject> stateMsg) {
    if (this.onlineStateHandler != null) {
      this.onlineStateHandler.handle(stateMsg.body());
    }
  }

  private void saveValueHandler(Message<JsonObject> msg) {
    if (this.valueHandler != null) {
      DeviceValue deviceValue = new DeviceValue(msg.body());
      this.valueHandler.handle(deviceValue);
    }
  }

  public ModbusTcp() {
    this(Vertx.vertx(), null);
  }

  public void setValueHandler(Handler<DeviceValue> handler) {
    this.valueHandler = handler;
  }

  public void setOnlineStateHandler(Handler<JsonObject> handler) {
    this.onlineStateHandler = handler;
  }

  public void joinToCtx(JoinToCtxConf conf) {
    vertx.eventBus().publish(ModbusConnContext.REPLACE_MODBUS_CONN_TOPIC, conf.toJson());
  }

  public Future<SendValueResult> setValue(SendValueData request) {
    Future<JsonObject> response = vertx.eventBus()
                                       .<JsonObject>request(ModbusConnContext.SET_MODBUS_REGISTER_VALUE, request.toJson())
                                       .compose(msg -> Future.succeededFuture(msg.body()));
    return response.compose(json -> Future.succeededFuture(new SendValueResult().fromJson(json)));
  }

  public Future<SendValueResult> setBitValue(SendBitValueData request) {
    Future<JsonObject> response = vertx.eventBus()
                                      .<JsonObject>request(ModbusConnContext.SET_MODBUS_REGISTER_BIT_VALUE, request.toJson())
                                      .compose(msg -> Future.succeededFuture(msg.body()));
    return response.compose(json -> Future.succeededFuture(new SendValueResult().fromJson(json)));
  }
}
