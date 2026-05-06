package io.github.kukpt.modbus.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@DataObject
@JsonGen(publicConverter = false)
public class JoinToCtxConf {
  public JoinToCtxConf() {

  }

  public JoinToCtxConf(JsonObject json) {
    JoinToCtxConfConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    JoinToCtxConfConverter.toJson(this, json);
    return json;
  }

  private List<RegisterLocator> locators = new ArrayList<>();

  private Long deviceId;

  private String deviceName;

  private String modbusIp;

  private Integer modbusPort;

  private Boolean getValueOnlyChanged;


}
