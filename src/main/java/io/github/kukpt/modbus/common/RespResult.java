package io.github.kukpt.modbus.common;

import lombok.Getter;

@Getter
public final class RespResult {

  public enum Code {
    SUCCESS(0),
    NOT_FOUND(404),
    SERVER_ERROR(500),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403);

    private int value;

    Code(int code) {
      this.value = code;
    }
  }

  private Integer code;

  private Object data;

  private String msg;

  public static RespResult ok(Object data) {
    RespResult result = new RespResult();
    result.code = Code.SUCCESS.value;
    result.data = data;
    return result;
  }

  private static RespResult error() {
    return error("未知错误!");
  }

  public static RespResult error(String msg) {
    return error(Code.BAD_REQUEST, msg);
  }

  public static RespResult error(Code code, String msg) {
    RespResult result = new RespResult();
    result.code = code.value;
    result.msg = msg;
    return result;
  }

}
