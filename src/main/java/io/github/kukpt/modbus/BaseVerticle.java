package io.github.kukpt.modbus;

import io.github.kukpt.modbus.repository._Repository;
import io.github.kukpt.modbus.repository.core.DatabaseService;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.hibernate.reactive.mutiny.Mutiny;

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

}
