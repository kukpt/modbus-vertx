package io.github.kukpt.modbus;

import io.github.kukpt.modbus.common.RespResult;
import io.github.kukpt.modbus.repository._Repository;
import io.github.kukpt.modbus.repository.core.DatabaseService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.UniHelper;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.mutiny.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

@Slf4j
public class BaseVerticle extends AbstractVerticle {

  protected _Repository repository;


  @Override
  public void init(Vertx vertx, Context context) {
    repository = new _Repository(session());
    super.init(vertx, context);
  }

  protected final Mutiny.SessionFactory session() {
    return DatabaseService.getSessionFactory();
  }

  protected <T> void handleResponse(Uni<T> uni, RoutingContext ctx, String errorMsg) {
    uni.map(RespResult::ok)
        .onFailure().invoke(err -> log.error(errorMsg, err))
        .onFailure().recoverWithItem(err -> RespResult.error(errorMsg))
        .chain(ctx::json)
        .subscribe().with(UniHelper.NOOP);
  }

  protected int parsePage(RoutingContext ctx) {
    return Long.valueOf(parseId(ctx, "page")).intValue();
  }

  protected int parsePageSize(RoutingContext ctx) {
    return Long.valueOf(parseId(ctx, "pageSize")).intValue();
  }

  // 路径参数
  protected long parseId(RoutingContext ctx, String param) {
    try {
      return Long.parseLong(ctx.pathParam(param));
    } catch (NumberFormatException e) {
      ctx.response().setStatusCode(400).end("无效的ID");
      throw e; // 或 return -1L 加 guard
    }
  }

  // 请求体
  protected <T> T parseBody(RoutingContext ctx, Class<T> clazz) {
    T obj = ctx.body().asPojo(clazz);
    if (obj == null) {
      ctx.response().setStatusCode(400).end("请求体不能为空");
      return null;
    }
    return obj;
  }

}
