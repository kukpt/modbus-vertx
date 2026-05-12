package io.github.kukpt.modbus.repository.core;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 纯 Vert.x + Mutiny + Hibernate Reactive 通用 Repository 抽象实现。
 * 无任何框架容器依赖（Quarkus / Spring）。
 *
 * <h3>子类最小实现</h3>
 * <pre>
 * public class UserRepository extends AbstractBaseRepository&lt;UserEntity, Long&gt; {
 *
 *     public UserRepository(SessionFactoryProvider provider) {
 *         super(provider);
 *     }
 *
 *     {@literal @}Override protected Class&lt;UserEntity&gt; entityClass() { return UserEntity.class; }
 * }
 * </pre>
 *
 * @param <T>  JPA 实体类型
 * @param <ID> 主键类型
 */
public abstract class AbstractBaseRepository<T, ID> implements BaseRepository<T, ID> {

  private final Mutiny.SessionFactory sf;

  /**
   * 构造器注入
   */
  protected AbstractBaseRepository(Mutiny.SessionFactory sessionFactory) {
    this.sf = sessionFactory;
  }

  /** 子类声明实体类型 */
  protected abstract Class<T> entityClass();

  // ── 内部访问 ─────────────────────────────────────────────────

  protected Mutiny.SessionFactory sf() {
    return sf;
  }

  private String entityName() {
    return entityClass().getSimpleName();
  }

  // ── 逃生舱 ───────────────────────────────────────────────────

  @Override
  public <R> Uni<R> withSession(Function<Mutiny.Session, Uni<R>> work) {
    return sf().withSession(work);
  }

  @Override
  public <R> Uni<R> withTransaction(Function<Mutiny.Session, Uni<R>> work) {
    return sf().withTransaction(work);
  }

  // ── 基本 CRUD ─────────────────────────────────────────────────

  @Override
  public Uni<T> findById(ID id) {
    return sf().withSession(s -> s.find(entityClass(), id));
  }

  @Override
  public Uni<Optional<T>> findByIdOptional(ID id) {
    return findById(id).map(Optional::ofNullable);
  }

  @Override
  public Uni<List<T>> findAll() {
    return sf().withSession(s ->
        s.createQuery("FROM " + entityName() + " e", entityClass())
         .getResultList()
    );
  }

  @Override
  public Uni<T> persist(T entity) {
    return sf().withTransaction(s ->
        s.persist(entity).replaceWith(entity)
    );
  }

  @Override
  public Uni<T> merge(T entity) {
    return sf().withTransaction(s -> s.merge(entity));
  }

  @Override
  public Uni<Void> delete(T entity) {
    return sf().withTransaction(s ->
        // 先 merge 保证托管状态，再 remove
        s.merge(entity).flatMap(s::remove)
    );
  }

  @Override
  public Uni<Integer> deleteById(ID id) {
    return sf().withTransaction(s ->
        s.createQuery("DELETE FROM " + entityName() + " e WHERE e.id = :id")
         .setParameter("id", id)
         .executeUpdate()
    );
  }

  // ── 批量操作 ──────────────────────────────────────────────────

  @Override
  public Uni<List<T>> persistAll(List<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return Uni.createFrom().item(List.of());
    }
    return sf().withTransaction(s ->
        s.persistAll(entities.toArray()).replaceWith(entities)
    );
  }

  @Override
  public Uni<Void> deleteAll(List<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return Uni.createFrom().voidItem();
    }
    return sf().withTransaction(s -> {
      // 链式执行：merge → remove，保证每个实体处于托管状态
      Uni<Void> chain = Uni.createFrom().voidItem();
      for (T entity : entities) {
        chain = chain.flatMap(ignored ->
            s.merge(entity).flatMap(s::remove)
        );
      }
      return chain;
    });
  }

  // ── 条件查询 ──────────────────────────────────────────────────

  @Override
  public Uni<List<T>> findBySpec(QuerySpec spec) {
    return sf().withSession(s -> {
      var q = s.createQuery(selectHql(spec), entityClass());
      applyParams(q, spec);
      return q.getResultList();
    });
  }

  @Override
  public Uni<Optional<T>> findOneBySpec(QuerySpec spec) {
    return sf().withSession(s -> {
      var q = s.createQuery(selectHql(spec), entityClass());
      applyParams(q, spec);
      return q.getSingleResultOrNull().map(Optional::ofNullable);
    });
  }

  @Override
  public Uni<Long> count() {
    return sf().withSession(s ->
        s.createQuery("SELECT COUNT(e) FROM " + entityName() + " e", Long.class)
         .getSingleResult()
    );
  }

  @Override
  public Uni<Long> countBySpec(QuerySpec spec) {
    return sf().withSession(s -> {
      var q = s.createQuery(countHql(spec), Long.class);
      applyParams(q, spec);
      return q.getSingleResult();
    });
  }
  /**
   * 执行自定义 HQL（联表查询专用），结果映射到实体 T
   */
  protected Uni<T> findByHql(String hql, ID id) {
    return sf().withSession(s -> {
      var q = s.createQuery(hql, entityClass())
               .setParameter("id", id)
          .getSingleResultOrNull();
      return q;
    });
  }

  /**
   * 执行自定义 HQL（联表查询专用），结果映射到实体 T
   */
  protected Uni<List<T>> findByHql(String hql, Map<String, Object> params) {
    return sf().withSession(s -> {
      var q = s.createQuery(hql, entityClass());
      params.forEach(q::setParameter);
      return q.getResultList();
    });
  }

  /**
   * 联表 DTO 投影查询（结果不是实体，是任意类型 R）
   */
  protected <R> Uni<List<R>> findProjection(
      String hql, Map<String, Object> params, Class<R> resultClass) {
    return sf().withSession(s -> {
      var q = s.createQuery(hql, resultClass);
      params.forEach(q::setParameter);
      return q.getResultList();
    });
  }

  // ── 分页 ──────────────────────────────────────────────────────

  @Override
  public Uni<PageResult<T>> findPage(int page, int pageSize) {
    return findPage(QuerySpec.create(), page, pageSize);
  }

  @Override
  public Uni<PageResult<T>> findPage(QuerySpec spec, int page, int pageSize) {
    validatePage(page, pageSize);

    Uni<Long> totalUni = countBySpec(spec);

    Uni<List<T>> dataUni = sf().withSession(s -> {
      var q = s.createQuery(selectHql(spec), entityClass());
      applyParams(q, spec);
      q.setFirstResult((page - 1) * pageSize);
      q.setMaxResults(pageSize);
      return q.getResultList();
    });

    // count 和 data 并发执行，减少总延迟
    return Uni.combine().all()
              .unis(totalUni, dataUni)
              .asTuple()
              .map(t -> new PageResult<>(t.getItem2(), t.getItem1(), page, pageSize));
  }
  /**
   * 联表分页（JOIN FETCH 场景需用 DISTINCT + countQuery 分离）
   */
  protected Uni<PageResult<T>> findPageByHql(
      String dataHql, String countHql,
      Map<String, Object> params,
      int page, int pageSize) {

    Uni<Long> totalUni = sf().withSession(s -> {
      var q = s.createQuery(countHql, Long.class);
      params.forEach(q::setParameter);
      return q.getSingleResult();
    });

    Uni<List<T>> dataUni = sf().withSession(s -> {
      var q = s.createQuery(dataHql, entityClass());
      params.forEach(q::setParameter);
      q.setFirstResult((page - 1) * pageSize);
      q.setMaxResults(pageSize);
      return q.getResultList();
    });

    return Uni.combine().all()
              .unis(totalUni, dataUni)
              .asTuple()
              .map(t -> new PageResult<>(t.getItem2(), t.getItem1(), page, pageSize));
  }
  // ── 存在性 ────────────────────────────────────────────────────

  @Override
  public Uni<Boolean> existsById(ID id) {
    String hql = "SELECT COUNT(e) FROM " + entityName() + " e WHERE e.id = :id";
    return sf().withSession(s ->
        s.createQuery(hql, Long.class)
         .setParameter("id", id)
         .getSingleResult()
         .map(c -> c > 0)
    );
  }

  // ── HQL 构建工具（protected，子类可重写） ─────────────────────

  protected String selectHql(QuerySpec spec) {
    String where = spec.buildWhereClause();
    String orderBy = spec.buildOrderByClause();
    String base = "FROM " + entityName() + " e";
    return (where.isBlank() ? base : base + " WHERE " + where) + orderBy;
  }

  protected String countHql(QuerySpec spec) {
    String where = spec.buildWhereClause();
    String base = "SELECT COUNT(e) FROM " + entityName() + " e";
    return where.isBlank() ? base : base + " WHERE " + where;
  }

  protected void applyParams(Mutiny.SelectionQuery<?> query, QuerySpec spec) {
    Map<String, Object> params = spec.allParams();
    params.forEach(query::setParameter);
  }

  // ── 校验工具 ──────────────────────────────────────────────────

  protected void validatePage(int page, int pageSize) {
    if (page < 1) throw new IllegalArgumentException("page must be >= 1");
    if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
  }
}
