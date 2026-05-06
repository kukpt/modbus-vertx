package io.github.kukpt.modbus.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DataObject
@JsonGen(publicConverter = false)
public class SendValueData {

  private Long deviceId;

  private Long locatorId;

  private Object value = new Object();

  public SendValueData(JsonObject json) {
    SendValueDataConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("value", value);
    SendValueDataConverter.toJson(this, json);
    return json;
  }
}
