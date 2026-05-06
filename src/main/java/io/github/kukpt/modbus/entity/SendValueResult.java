package io.github.kukpt.modbus.entity;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendValueResult {

  private Boolean success;

  private String message;

  public SendValueResult fromJson(JsonObject json) {
    int code = json.getInteger("code");
    this.success = code == 0 ? true : false;
    this.message = json.getString("msg");
    return this;
  }
}
