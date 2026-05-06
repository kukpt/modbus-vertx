package io.github.kukpt.modbus.common;

import io.vertx.core.json.JsonObject;


public class ResponseJson {

  enum result {code, data, msg}

  enum code {
    success(0),
    error(-1);

    private int code;

    private code(int v) {
      code = v;
    }
  }

  public static boolean isSuccess(JsonObject jo) {
    Integer joCode = jo.getInteger(result.code.name());
    return code.success.code == joCode;
  }

  public static JsonObject success() {
    return success(new JsonObject());
  }

  public static JsonObject success(Object data) {
    return new JsonObject()
    .put(result.code.name(), code.success.code)
    .put(result.msg.name(), "success")
    .put(result.data.name(), data);
  }
  public static JsonObject error() {
    return error("未知错误!");
  }

  public static JsonObject error(String errMsg) {
    return new JsonObject()
    .put(result.code.name(), code.error.code)
    .put(result.msg.name(), errMsg)
    .put(result.data.name(), new JsonObject());
  }
}
