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
public class SendBitValueData {

  private Long deviceId;

  private Long locatorId;

  private Integer bit;

  private Boolean flag;


  public SendBitValueData(JsonObject json) {
    SendBitValueDataConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    SendBitValueDataConverter.toJson(this, json);
    return json;
  }

}
