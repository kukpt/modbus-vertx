package io.github.kukpt.modbus.common;

import com.google.common.base.Strings;
import com.google.common.primitives.*;
import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.sero.util.ArrayUtils;
import io.github.kukpt.modbus.entity.JoinToCtxConf;
import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterLocator;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniCreate;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shuo
 * 绑定了每个modbus网关
 */
@Slf4j
public class ModbusDeviceConn {

  @Getter
  private final Long deviceId;

  @Getter
  @Setter
  private String deviceName = "";

  @Getter
  private final String useIp;

  @Getter
  private final Integer usePort;

  private final ModbusMaster master;

  private final Boolean getValueOnlyChanged;

  public boolean isInitialized() {
    return master.isInitialized();
  }

  /**
   * 读取寄存器里的数据
   */
  private final Map<Long, DeviceRegisterLocator> locators;

  public ModbusDeviceConn(ModbusDevice device) {
    this.deviceId = device.getId();
    this.deviceName = device.getName();
    this.useIp = device.getUseIp();
    this.usePort = device.getUsePort();
    if (device.getGetOnlyChanged() == null) {
      device.setGetOnlyChanged(false);
    }
    this.getValueOnlyChanged = device.getGetOnlyChanged();
    RegisterTemplate template = device.getRegisterTemplate();
    this.locators = template.getRegisterLocators()
                            .stream()
                            .collect(Collectors.toMap(v -> v.getId(),
                                v ->
                                    new DeviceRegisterLocator(
                                        v.getId(),
                                        v.getSlaveId(),
                                        v.getRegisterRange(),
                                        v.getRegisterOffset(),
                                        v.getDataType(),
                                        v.getRegisterBit())));
    this.master = ConnectionUtil.createMaster(useIp, usePort);
  }

  public ModbusDeviceConn(JoinToCtxConf conf) {
    this.deviceId = conf.getDeviceId();
    this.deviceName = conf.getDeviceName();
    this.useIp = conf.getModbusIp();
    this.usePort = conf.getModbusPort();
    this.getValueOnlyChanged = conf.getGetValueOnlyChanged();
    this.locators = conf.getLocators().stream()
                        .collect(Collectors.toMap(v -> v.getId(), v ->
                            new DeviceRegisterLocator(v.getId(), v.getSlaveId(), v.getRegisterRange(), v.getRegisterOffset(), v.getDataType(), v.getRegisterBit())
                        ));
    this.master = ConnectionUtil.createMaster(useIp, usePort);
  }

  public ModbusDeviceConn(JsonObject config) {
    this.deviceId = config.getLong("deviceId");
    if (!Strings.isNullOrEmpty(config.getString("deviceName"))) {
      this.deviceName = config.getString("deviceName");
    }
    this.useIp = config.getString("modbusIp");
    this.usePort = config.getInteger("modbusPort");
    this.getValueOnlyChanged = config.getBoolean("getValueOnlyChanged", false);
    JsonArray arrays = config.getJsonArray("locators");
    this.locators = new HashMap<>(arrays.size());
    for (int i = 0; i < arrays.size(); i++) {
      JsonObject locatorConf = arrays.getJsonObject(i);
      DeviceRegisterLocator locator;
      if (locatorConf.containsKey("register_range")) { // 处理 camelCase and snake_case
        locator =
            new DeviceRegisterLocator(
                locatorConf.getLong("id")
                , locatorConf.getInteger("slave_id")
                , locatorConf.getInteger("register_range")
                , locatorConf.getInteger("register_offset")
                , locatorConf.getInteger("data_type")
                , locatorConf.getInteger("register_bit", -1));
      } else {
        locator =
            new DeviceRegisterLocator(
                locatorConf.getLong("id")
                , locatorConf.getInteger("slaveId")
                , locatorConf.getInteger("registerRange")
                , locatorConf.getInteger("registerOffset")
                , locatorConf.getInteger("dataType")
                , locatorConf.getInteger("registerBit", -1));
      }

      if (!Strings.isNullOrEmpty(locatorConf.getString("name"))) {
        locator.setLocatorName(locatorConf.getString("name"));
      }
      locators.put(locator.getId(), locator);
    }
    this.master = ConnectionUtil.createMaster(useIp, usePort);
  }

  public void init() {
    try {
      master.init();
      log.trace("设备: [{}] - [{}] - [{}] 初始化成功!", deviceId, useIp, usePort);
    } catch (ModbusInitException e) {
      log.trace("设备: [{}] - [{}] - [{}] 初始化失败!", deviceId, useIp, usePort);
      master.destroy();
    }
  }


  /**
   * 按位设置值
   *
   * @param locatorId
   * @param bit
   * @param value
   * @return
   */
  public Uni<Void> setBitValue(final Long locatorId, final Integer bit, final Boolean value) {
    DeviceRegisterLocator locator = locators.get(locatorId);
    int slaveId = locator.locator.getSlaveId();
    int range = locator.locator.getRange();
    int offset = locator.locator.getOffset();
    int dataType = 1; // Binary
    BaseLocator baseLocator = BaseLocator.createLocator(slaveId, range, offset, dataType, bit, DataType.getRegisterCount(dataType));
    return setValue(baseLocator, value);
  }

  public Uni<Void> setValue(final Long locatorId, final Object value) {
    UniCreate from = Uni.createFrom();
    DeviceRegisterLocator locator = locators.get(locatorId);
    if (locator == null) {
      return from.failure(new RuntimeException("locator is null"));
    }
    try {
      Object convertedValue = convertValue(value, locator.getJavaType());
      return setValue(locator.locator, convertedValue);
    } catch (ClassCastException e) {
      log.error("[SET_VALUE] ClassCastException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
      return from.failure(new RuntimeException("设置值类型转换异常"));
    }
  }

  public Uni<Void> setValue(BaseLocator locator, Object convertedValue) {
    UniCreate from = Uni.createFrom();
    if (!isInitialized()) {
      return from.failure(new RuntimeException("offline!"));
    }
    try {
      master.setValue(locator, convertedValue);
      log.trace("[SET_VALUE] Success! Id:  Id: [{}], IP: [{}], Port: [{}]", deviceId, useIp, usePort);
      return from.voidItem();
    } catch (ModbusTransportException e) {
      log.error("[SET_VALUE] ModbusTransportException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
      master.destroy();
      return from.failure(new RuntimeException("数据传输异常!"));
    } catch (ErrorResponseException e) {
      log.error("[SET_VALUE] ErrorResponseException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
      master.destroy();
      return from.failure(new RuntimeException("设备响应异常!"));
    } catch (Exception e) {
      log.error("[SET_VALUE] {}: Id: [{}], IP: [{}], Port: [{}] - err: {}", e.getClass()
                                                                             .getSimpleName(), deviceId, useIp, usePort, e.getMessage());
      return from.failure(e);
    }
  }

  public Object convertValue(Object value, Class<?> javaType) {
    if (value == null) return null;
    if (javaType.isInstance(value)) return value;

    String strValue = String.valueOf(value);

    return switch (javaType.getName()) {
      case "java.lang.Boolean" -> Boolean.valueOf(strValue);
      case "java.lang.Integer" -> Ints.stringConverter().convert(strValue);
      case "java.lang.Long" -> Longs.stringConverter().convert(strValue);
      case "java.math.BigInteger" -> new BigInteger(strValue);
      case "java.lang.Double" -> Doubles.stringConverter().convert(strValue);
      case "java.lang.Float" -> Floats.stringConverter().convert(strValue);
      case "java.lang.Short" -> Shorts.stringConverter().convert(strValue);
      case "java.lang.String" -> strValue;
      default -> throw new ClassCastException("无法将值转换为 " + javaType.getName());
    };
  }

  public List<MasterValue> getValues() {
    if (getValueOnlyChanged) {
      return getChangedValues();
    }
    return getAllValues();
  }

  /**
   * 获取寄存器数据
   *
   * @return
   */
  public List<MasterValue> getAllValues() {

    List values = new ArrayList<>();
    if (master.isInitialized()) {
      BatchRead<Long> read = new BatchRead<>();
      locators.values().forEach(locator -> {
        read.addLocator(locator.id, locator.locator);
      });
      try {
        BatchResults<Long> results = master.send(read);
        locators.values().forEach(locator -> {
          MasterValue masterValue = new MasterValue(locator.getId(), System.currentTimeMillis(), results.getValue(locator.getId()), deviceId, deviceName, locator.locatorName);
          values.add(masterValue);
        });
      } catch (ModbusTransportException e) {
        log.error("[GET_VALUE] ModbusTransportException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
        master.destroy();
      } catch (ErrorResponseException e) {
        log.error("[GET_VALUE] ErrorResponseException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
        master.destroy();
      }
    }
    return values;
  }

  public List<MasterValue> getChangedValues() {
    List values = new ArrayList<>();
    if (master.isInitialized()) {
      BatchRead<Long> read = new BatchRead<>();
      locators.values().forEach(locator -> {
        read.addLocator(locator.id, locator.locator);
      });
      try {
        BatchResults<Long> results = master.send(read);
        locators.values().forEach(locator -> {
          locator.value = results.getValue(locator.getId());
          if (locator.valueIsChanged()) {
            MasterValue masterValue = new MasterValue(locator.getId(), System.currentTimeMillis(), results.getValue(locator.getId()), deviceId, deviceName, locator.locatorName);
            values.add(masterValue);
          }
        });
      } catch (ModbusTransportException e) {
        log.error("[GET_VALUE] ModbusTransportException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
        master.destroy();
      } catch (ErrorResponseException e) {
        log.error("[GET_VALUE] ErrorResponseException: Id: [{}], IP: [{}], Port: [{}] - err: {}", deviceId, useIp, usePort, e.getMessage());
        master.destroy();
      }

    }
    return values;
  }

  /**
   * @author shuo
   * 设备寄存器定位器
   */
  class DeviceRegisterLocator {

    private boolean valueIsChanged() {
      if (preValue != null && preValue.equals(value)) {
        return false;
      }
      preValue = value;
      return true;
    }

    /**
     * 上次的值
     */
    private Object preValue;

    private Object value;

    @Getter
    private final Long id;

    @Getter
    @Setter
    private String locatorName = "";

    private final BaseLocator locator;

    @Getter
    private final Integer dateType;

    @Getter
    private Long ts = 0L;

    @Getter
    private final Class<?> javaType;

    public DeviceRegisterLocator(Long id, Integer slaveId, Integer range, Integer offset, Integer dataType, Integer bit) {
      this.dateType = dataType;
      this.javaType = DataType.getJavaType(dateType);
      this.id = id;
      this.locator = this.createLocator(slaveId, range, offset, dataType, bit);
    }

    /**
     * 创建设备定位器
     *
     * @param slaveId  从站地址
     * @param range    寄存器范围
     *                 <p>COIL_STATUS=       1      :0x0</p>
     *                 <p>INPUT_STATUS=      2      :0x10000</p>
     *                 <p>HOLDING_REGISTER=  3      :0x40000</p>
     *                 <p>INPUT_REGISTER=    4      :0x30000</p>
     * @param offset   寄存器偏移
     * @param dataType 数据类型
     * @return
     * @see com.serotonin.modbus4j.code.DataType
     * @see com.serotonin.modbus4j.code.RegisterRange
     */
    private BaseLocator createLocator(int slaveId, int range, int offset, int dataType, int bit) {
      /**
       * slaveId
       * range    com.serotonin.modbus4j.code.RegisterRange
       * offset
       * dataType com.serotonin.modbus4j.code.DataType
       * bit           当不从COIL_STATUS和INPUT_STATUS读取bit时使用
       * registerCount 读几个寄存器
       * charset       当使用StringLocator 时使用的字段
       */
      return BaseLocator.createLocator(slaveId, range, offset, dataType, bit, DataType.getRegisterCount(dataType));
    }

  }

  /**
   * @author shuo
   * 读取到的寄存器值内容
   */
  @Getter
  @ToString
  public class MasterValue {

    public JsonObject toJson() {
      return new JsonObject()
          .put("deviceId", deviceId)
          .put("deviceName", deviceName)
          .put("locatorId", locatorId)
          .put("locatorName", locatorName)
          .put("ts", ts)
          .put("value", value);
    }

    private MasterValue(Long id, Long ts, Object value, Long deviceId, String deviceName, String locatorName) {
      this.locatorId = id;
      this.ts = ts;
      this.value = value;
      this.deviceId = deviceId;
      this.deviceName = deviceName;
      this.locatorName = locatorName;
    }

    private Long deviceId;

    @Setter
    private String deviceName;

    private Long locatorId;

    @Setter
    private String locatorName;

    private Long ts;

    private Object value;
  }

}
