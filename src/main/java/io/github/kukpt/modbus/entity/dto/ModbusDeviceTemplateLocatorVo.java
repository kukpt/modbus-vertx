package io.github.kukpt.modbus.entity.dto;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModbusDeviceTemplateLocatorVo {

  public static ModbusDeviceTemplateLocatorVo of(ModbusDevice device, RegisterTemplate registerTemplate) {
    ModbusDeviceTemplateLocatorVo vo = new ModbusDeviceTemplateLocatorVo();
    vo.setRegisterTemplate(registerTemplate);
    vo.setId(device.getId());
    vo.setName(device.getName());
    vo.setOnlineState(device.getOnlineState());
    vo.setUseIp(device.getUseIp());
    vo.setUsePort(device.getUsePort());
    vo.setCreateTime(device.getCreateTime());
    vo.setUpdateTime(device.getUpdateTime());
    vo.setGetOnlyChanged(device.getGetOnlyChanged());
    vo.setDeviceTagName(device.getTagName());
    vo.setCollectInterval(device.getCollectInterval());
    return vo;
  }

  private Long id;

  private String name;

  private ModbusDevice.OnlineState onlineState;

  private String useIp;

  private Integer usePort;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  private Boolean getOnlyChanged;

  private RegisterTemplate registerTemplate;

  private String deviceTagName;

  private Integer collectInterval;
}
