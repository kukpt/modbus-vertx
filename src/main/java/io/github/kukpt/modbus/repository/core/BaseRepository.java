package io.github.kukpt.modbus.repository.core;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface BaseRepository<T, ID> {

  // ── CRUD ────────────────────────────────────────────────────
  Uni<T>           findById(ID id);
  Uni<Optional<T>> findByIdOptional(ID id);
  Uni<List<T>>     findAll();
  Uni<T>           persist(T entity);
  Uni<T>           merge(T entity);
  Uni<Void>        delete(T entity);
  Uni<Integer>     deleteById(ID id);

  // ── 批量 ────────────────────────────────────────────────────
  Uni<List<T>>     persistAll(List<T> entities);
  Uni<Void>        deleteAll(List<T> entities);

  // ── 条件查询 ─────────────────────────────────────────────────
  Uni<List<T>>     findBySpec(QuerySpec spec);
  Uni<Optional<T>> findOneBySpec(QuerySpec spec);
  Uni<Long>        count();
  Uni<Long>        countBySpec(QuerySpec spec);

  // ── 分页 ────────────────────────────────────────────────────
  Uni<PageResult<T>> findPage(int page, int pageSize);
  Uni<PageResult<T>> findPage(QuerySpec spec, int page, int pageSize);

  // ── 存在性 ───────────────────────────────────────────────────
  Uni<Boolean>     existsById(ID id);

  // ── 逃生舱：直接操作 Session ─────────────────────────────────
  <R> Uni<R> withSession(Function<Mutiny.Session, Uni<R>> work);
  <R> Uni<R> withTransaction(Function<Mutiny.Session, Uni<R>> work);
}