package io.github.kukpt.modbus.entity.dto;


import lombok.Data;
@Data
public class RegisterTemplateDto {
  private Long id;

  private String name;

  private Long version;

  Long[] locators;
}
