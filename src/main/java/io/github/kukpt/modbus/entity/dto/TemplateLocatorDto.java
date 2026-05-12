package io.github.kukpt.modbus.entity.dto;

import lombok.Data;

@Data
public class TemplateLocatorDto {
  /** TemplateLocator 自身 ID（新增时必填，由业务方指定） */
  private Long id;
  /** 所属模板 ID */
  private Long templateId;
  /** 关联的寄存器定位器 ID */
  private Long locatorId;
}