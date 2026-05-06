package io.github.kukpt.modbus;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
@DataObject
@JsonGen(publicConverter = false)
public class ModbusTcpOptions {
  /**
   * 连接池大小
   */
  private Integer poolSize = 4;
  /**
   * 问询间隔 单位秒
   */
  private Integer queryInterval = 2;
  /**
   * 重连间隔 单位秒
   */
  private Integer reconnectionInterval = 10;

  public ModbusTcpOptions setPoolSize(Integer poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public ModbusTcpOptions setQueryInterval(Integer queryInterval) {
    this.queryInterval = queryInterval;
    return this;
  }

  public ModbusTcpOptions setReconnectionInterval(Integer reconnectionInterval) {
    this.reconnectionInterval = reconnectionInterval;
    return this;
  }

  public ModbusTcpOptions() {

  }

  public ModbusTcpOptions(JsonObject json) {
    ModbusTcpOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ModbusTcpOptionsConverter.toJson(this, json);
    return json;
  }



}
