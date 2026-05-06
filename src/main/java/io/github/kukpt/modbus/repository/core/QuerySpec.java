package io.github.kukpt.modbus.repository.core;

import java.util.*;

/**
 * 链式查询规格构建器，生成安全的参数化 HQL 片段。
 */
public class QuerySpec {

  private final Map<String, Object> eqConditions  = new LinkedHashMap<>();
  private final List<String> rawConditions  = new ArrayList<>();
  private final Map<String, Object> rawParams      = new LinkedHashMap<>();
  private final List<String>        orderClauses   = new ArrayList<>();

  private QuerySpec() {}

  public static QuerySpec create() { return new QuerySpec(); }

  // ── 条件构建 ────────────────────────────────────────────────

  /** e.field = :field */
  public QuerySpec eq(String field, Object value) {
    eqConditions.put(field, value);
    return this;
  }

  /** e.field != :field */
  public QuerySpec ne(String field, Object value) {
    rawConditions.add("e." + field + " != :" + field + "_ne");
    rawParams.put(field + "_ne", value);
    return this;
  }

  /** e.field LIKE :field_like  （调用者传入带 % 的值） */
  public QuerySpec like(String field, String pattern) {
    rawConditions.add("e." + field + " LIKE :" + field + "_like");
    rawParams.put(field + "_like", pattern);
    return this;
  }

  /** e.field IS NULL */
  public QuerySpec isNull(String field) {
    rawConditions.add("e." + field + " IS NULL");
    return this;
  }

  /** e.field IS NOT NULL */
  public QuerySpec isNotNull(String field) {
    rawConditions.add("e." + field + " IS NOT NULL");
    return this;
  }

  /** e.field IN (:field_in) */
  public QuerySpec in(String field, Collection<?> values) {
    rawConditions.add("e." + field + " IN :" + field + "_in");
    rawParams.put(field + "_in", values);
    return this;
  }

  /** 任意自定义 HQL 片段 + 对应参数 */
  public QuerySpec raw(String hqlSnippet, Map<String, Object> params) {
    rawConditions.add(hqlSnippet);
    rawParams.putAll(params);
    return this;
  }

  // ── 排序 ────────────────────────────────────────────────────

  public QuerySpec orderByAsc(String field)  {
    orderClauses.add("e." + field + " ASC");  return this;
  }
  public QuerySpec orderByDesc(String field) {
    orderClauses.add("e." + field + " DESC"); return this;
  }

  // ── HQL 片段生成（供 Repository 内部使用） ───────────────────

  public String buildWhereClause() {
    List<String> parts = new ArrayList<>();
    eqConditions.keySet().forEach(k -> parts.add("e." + k + " = :" + k));
    parts.addAll(rawConditions);
    return parts.isEmpty() ? "" : String.join(" AND ", parts);
  }

  public String buildOrderByClause() {
    return orderClauses.isEmpty()
        ? "" : " ORDER BY " + String.join(", ", orderClauses);
  }

  /** 所有参数（eq + raw 合并） */
  public Map<String, Object> allParams() {
    Map<String, Object> all = new LinkedHashMap<>(eqConditions);
    all.putAll(rawParams);
    return all;
  }

  public boolean hasConditions() {
    return !eqConditions.isEmpty() || !rawConditions.isEmpty();
  }
}