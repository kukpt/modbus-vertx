package io.github.kukpt.modbus.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DataObject
@JsonGen(publicConverter = false)
public class DeviceValue {

  public DeviceValue(JsonObject json) {
    json.put("value", String.valueOf(json.getValue("value")));
    DeviceValueConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    DeviceValueConverter.toJson(this, json);
    return json;
  }

  private Long deviceId;

  private String deviceName;

  private Long locatorId;

  private String locatorName;

  private Long ts;

  private String value;
}
