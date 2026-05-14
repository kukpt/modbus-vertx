package io.github.kukpt.modbus;

import com.google.common.util.concurrent.*;
import io.github.kukpt.modbus.common.ModbusDeviceConn;
import io.github.kukpt.modbus.common.ResponseJson;
import io.github.kukpt.modbus.entity.SendBitValueData;
import io.github.kukpt.modbus.entity.dto.ModbusDeviceTemplateLocatorVo;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.UniHelper;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.WorkerExecutor;
import io.vertx.mutiny.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;

/**
 * 管理modbus 连接
 *
 * @author shuo
 * <br/>添加连接的配置格式：JSON
 * <br/><pre>{@code conf: {
 *     "deviceId":66,
 *     "deviceName":"测试1",
 *     "modbusIp":"127.0.0.1",
 *     "modbusPort":505,
 *     "locators":[
 *         {
 *             "id":2,
 *             "name":"温度",
 *             "slaveId":1,
 *             "dataType":2,
 *             "registerBit":-1,
 *             "registerRange":3,
 *             "registerOffset":0
 *         },
 *         {
 *             "id":7,
 *             "name":"湿度",
 *             "slaveId":1,
 *             "dataType":2,
 *             "register_bit":-1,
 *             "registerRange":3,
 *             "registerOffset":1
 *         },
 *         {
 *             "id":8,
 *             "name":"位",
 *             "slaveId":1,
 *             "dataType":1,
 *             "register_bit":0,
 *             "registerRange":3,
 *             "registerOffset":0
 *         }
 *     ]
 * }}
 * </pre>
 */
@Slf4j
public class ModbusConnContext extends BaseVerticle {

  static {
    ThreadFactoryBuilder factoryBuilder = new ThreadFactoryBuilder();
    ThreadFactory threadFactory = factoryBuilder.setNameFormat("modbus-reConn-thread").build();
    reConnExecutor = Executors.newSingleThreadExecutor(threadFactory);
  }

  /**
   * 断线重连线程
   */
  private static final ExecutorService reConnExecutor;

  /**
   * 轮询寄存器线程
   */
  private WorkerExecutor executor;

  /**
   * 设置 MODBUS 寄存器值
   */
  public static final String SET_MODBUS_REGISTER_VALUE = "ctx.set.register.value";

  public static final String SET_MODBUS_REGISTER_BIT_VALUE = "ctx.set.register.bit.value";

  /**
   * 删除设备链接
   */
  public static final String DEL_MODBUS_CONN_TOPIC = "ctx.del.conn";

  /**
   * 替换或加入
   */
  public static final String REPLACE_MODBUS_CONN_TOPIC = "ctx.replaceOrJoin.conn";

  public static final String ONLINE_STATE = "ctx.conn.onlineState";

  public static final String SAVE_VALUE = "ctx.save.register.value";

  private ConcurrentHashMap<Long, ModbusDeviceConn> conns = new ConcurrentHashMap<>();

  private final static int DEFAULT_POOL_SIZE = 4;

  private final static int DEFAULT_QUERY_INTERVAL = 2;

  private final static int DEFAULT_RECONNECTION_INTERVAL = 10;

  private void init() {
    Object ps = config().getValue("_MOD_MODBUS_POOL_SIZE", DEFAULT_POOL_SIZE);
    Object qi = config().getValue("_MOD_MODBUS_QUERY_INTERVAL", DEFAULT_QUERY_INTERVAL);
    Object rei = config().getValue("_MOD_MODBUS_RECONNECTION_INTERVAL", DEFAULT_RECONNECTION_INTERVAL);
    this.poolSize = Integer.parseInt(String.valueOf(ps));;
    this.queryInterval =Integer.parseInt(String.valueOf(qi));
    this.reconnectionInterval = Integer.parseInt(String.valueOf(rei));
  }

  private int poolSize; // 线程池大小

  private int queryInterval; // 问询时间间隔 秒

  private int reconnectionInterval; // 断线重连间隔 秒

  @Override
  public Uni<Void> asyncStart() {
    init();
    repository.device().findDeviceWithTemplate()
              .invoke(v -> this.addConn(v))
              .subscribe().with(UniHelper.NOOP);


    this.executor = vertx.createSharedWorkerExecutor("modbus-pool", poolSize);
    vertx.eventBus().<JsonObject>localConsumer(REPLACE_MODBUS_CONN_TOPIC, this::addConn);
    vertx.eventBus().<JsonObject>localConsumer(DEL_MODBUS_CONN_TOPIC, this::delConnHandler);
    vertx.eventBus().<JsonObject>localConsumer(SET_MODBUS_REGISTER_VALUE, this::setConnValue);
    vertx.eventBus().<JsonObject>localConsumer(SET_MODBUS_REGISTER_BIT_VALUE, this::setConnBitValue);
    // modbusTCP 问询任务
    vertx.setPeriodic(TimeUnit.SECONDS.toMillis(queryInterval), this::getConnValue);
    // 断线重连任务
    vertx.setPeriodic(TimeUnit.SECONDS.toMillis(reconnectionInterval), this::reConnectHandler);
    // 在线离线状态广播
    vertx.setPeriodic(TimeUnit.SECONDS.toMillis(10), id -> this.publishOnLineState());
    return super.asyncStart();
  }

  /**
   * 在线离线状态广播
   */
  private void publishOnLineState() {
    conns.forEach((k, v) -> {
      JsonObject payload = new JsonObject().put("deviceId", k).put("onlineState", v.isInitialized());
      log.trace("在线状态:{}", payload);
      vertx.eventBus().publish(ONLINE_STATE, payload);
    });
  }

  /**
   * 按位设置寄存器数据
   *
   * @param msg
   */
  private void setConnBitValue(Message<JsonObject> msg) {
    JsonObject request = msg.body();
    SendBitValueData bitValueData = new SendBitValueData(request);
    Uni.createFrom().item(conns.get(bitValueData.getDeviceId()))
       .onItem().ifNull().failWith(() -> new RuntimeException("Connection missing"))
       .chain(conn -> conn.setBitValue(bitValueData.getLocatorId(), bitValueData.getBit(), bitValueData.getFlag()))
       .subscribe().with(
           ok -> msg.reply(ResponseJson.success()),
           err -> msg.reply(ResponseJson.error()));
  }

  /**
   * 设置寄存器数据
   *
   * @param msg
   */
  private void setConnValue(Message<JsonObject> msg) {
    JsonObject request = msg.body();
    Long deviceId = request.getLong("deviceId");
    Long locatorId = request.getLong("locatorId");
    Object value = request.getValue("value");

    executor.executeBlocking(
                conns.get(deviceId).setValue(locatorId, value)
                     .invoke(unused -> msg.reply(ResponseJson.success()))
                     .onFailure().invoke(() -> msg.reply(ResponseJson.error())), false)
            .subscribe().with(UniHelper.NOOP);
  }

  /**
   * 重连
   *
   * @param id
   */
  private void reConnectHandler(Long id) {
    reConnExecutor.submit(() -> {
      List<ModbusDeviceConn> collects = conns.values().stream().toList();
      for (ModbusDeviceConn conn : collects) {
        if (!conn.isInitialized()) {
          log.trace("[{}] - [{}] - [{}] 设备重连!", conn.getDeviceId(), conn.getUseIp(), conn.getUsePort());
          conn.init();
        }
      }
    });
  }

  private void disconnect(final ModbusDeviceConn conn) {
    executor.executeBlockingAndForget(() -> {
      conn.destroy();
      return null;
    }, false);
  }

  /**
   * 获取modbus设备数据
   *
   * @param id
   */
  private void getConnValue(Long id) {
    List<ModbusDeviceConn> clients = conns.values().stream().toList();
    for (ModbusDeviceConn conn : clients) {
      executor.executeBlockingAndForget(() -> this.getValueBlockingHandler(conn), false);
    }
  }

  private Void getValueBlockingHandler(final ModbusDeviceConn conn) {
    if (conn.isInitialized()) {
      Long startTime = System.currentTimeMillis();
      List<ModbusDeviceConn.MasterValue> values = conn.getValues();
      if (values.isEmpty()) {
        return null;
      }
      JsonObject result = new JsonObject()
          .put("deviceId", values.get(0).getDeviceId())
          .put("deviceName", values.get(0).getDeviceName());

      List<JsonObject> locators = values.stream().map(value -> {
        return new JsonObject()
            .put("locatorId", value.getLocatorId())
            .put("locatorName", value.getLocatorName())
            .put("value", value.getValue())
            .put("javaType", value.getJavaType())
            .put("tagName", value.getTagName())
            .put("ts", value.getTs());
      }).toList();

      result.put("locators", new JsonArray(locators));
      log.trace("value: {}", result);
      MultiMap headers = MultiMap.caseInsensitiveMultiMap();
      if (conn.getMqttPublishTopic() != null) {
        headers.add("mqttPublishTopic", conn.getMqttPublishTopic());
      } else {
        String topic = String.format("/device/modbus/%s/message/report", conn.getDeviceId());
        headers.add("mqttPublishTopic", topic);
      }

      vertx.eventBus().publish(SAVE_VALUE, result, new DeliveryOptions().setHeaders(headers));

      Long endTime = System.currentTimeMillis();
      log.debug("NAME=[{}]-IP=[{}]-PORT=[{}] 耗时[{}]/ms", conn.getDeviceName(), conn.getUseIp(), conn.getUsePort(), endTime - startTime);
    }
    return null;
  }


  private void delConnHandler(Message<JsonObject> msg) {
    Long deviceId = msg.body().getLong("deviceId");
    if (conns.containsKey(deviceId)) {
      ModbusDeviceConn conn = conns.remove(deviceId);
      conn.destroy();
      msg.reply(ResponseJson.success());
    } else {
      log.error("CTX 移除设备ERR: 未找到该设备 {}", deviceId);
    }
  }


  /**
   * conn 加入到Ctx
   *
   * @param msg
   */
  private void addConn(Message<JsonObject> msg) {
    JsonObject conf = msg.body();
    log.trace("replaceOrJoinToCtx: {}", conf);
    ModbusDeviceConn conn = new ModbusDeviceConn(conf.mapTo(ModbusDeviceTemplateLocatorVo.class));
    Long key = conn.getDeviceId();
    if (conns.containsKey(key)) {
      disconnect(conns.get(key));
      conns.replace(key, conn);
    } else {
      conns.put(key, conn);
    }
    msg.reply(ResponseJson.success());
    executor.executeBlockingAndForget(() -> {
      conn.init();
      this.publishOnLineState();
      return new Object();
    });
  }

  /**
   * conn 加入到Ctx
   *
   * @param devices
   */
  private void addConn(List<ModbusDeviceTemplateLocatorVo> devices) {
    for (ModbusDeviceTemplateLocatorVo device : devices) {
      ModbusDeviceConn conn = new ModbusDeviceConn(device);
      Long key = conn.getDeviceId();
      if (conns.containsKey(key)) {
        disconnect(conns.get(key));
        conns.replace(key, conn);
      } else {
        conns.put(key, conn);
      }
      executor.executeBlockingAndForget(() -> {
        conn.init();
        this.publishOnLineState();
        return new Object();
      });
    }
  }


  @Override
  public Uni<Void> asyncStop() {
    return executor.close();
  }
}
