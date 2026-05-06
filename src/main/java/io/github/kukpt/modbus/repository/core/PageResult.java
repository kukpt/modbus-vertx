package io.github.kukpt.modbus.repository.core;

import java.util.List;

public class PageResult<T> {

  private final List<T> data;
  private final long    total;
  private final int     page;
  private final int     pageSize;

  public PageResult(List<T> data, long total, int page, int pageSize) {
    this.data     = data;
    this.total    = total;
    this.page     = page;
    this.pageSize = pageSize;
  }

  public List<T> getData()   { return data; }
  public long    getTotal()  { return total; }
  public int     getPage()   { return page; }
  public int     getPageSize(){ return pageSize; }

  public int     getTotalPages() {
    return pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
  }
  public boolean hasNext()  { return page < getTotalPages(); }
  public boolean hasPrev()  { return page > 1; }

  @Override
  public String toString() {
    return "PageResult{page=" + page + ", pageSize=" + pageSize
        + ", total=" + total + ", data.size=" + data.size() + "}";
  }
}