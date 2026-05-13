package io.github.kukpt.modbus;

import io.netty.handler.codec.mqtt.MqttQoS;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.mqtt.MqttClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class MqttClientVerticle extends AbstractVerticle {

  private static final int MQTT_CONNECT_TIMEOUT_MS = 1000;
  private static final long MQTT_RECONNECT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);

  private MqttClient mqttClient;
  private Long reconnectTimerId;
  private boolean connecting;
  private boolean stopped;

  @Override
  public Uni<Void> asyncStart() {
    vertx.eventBus().<JsonObject>localConsumer(ModbusConnContext.SAVE_VALUE, ebMsg -> {
      MqttClient client = mqttClient;
      if (client != null && client.isConnected()) {
        client.publish(
                    config().getString("_MOD_MQTT_PUBLISH_TOPIC"),
                    Buffer.newInstance(ebMsg.body().toBuffer()),
                    MqttQoS.AT_MOST_ONCE,
                    false,
                    false
                          ).invoke(id -> {
                    log.info("packetId: {}, mqtt clientId: {}, publish: {}", id, client.clientId(), ebMsg.body());
                  })
                  .subscribe()
                  .with(id -> {
                  }, err -> log.error("mqtt publish error: {}", err.getMessage()));
      }
    });
    connect();
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> asyncStop() {
    stopped = true;
    cancelReconnectTimer();
    MqttClient client = mqttClient;
    mqttClient = null;
    if (client == null || !client.isConnected()) {
      return Uni.createFrom().voidItem();
    }
    return client.disconnect()
                 .onFailure().invoke(err -> log.warn("mqtt disconnect error: {}", err.getMessage()))
                 .onFailure().recoverWithNull();
  }

  private void connect() {
    if (stopped || connecting) {
      return;
    }

    MqttClient currentClient = mqttClient;
    if (currentClient != null && currentClient.isConnected()) {
      return;
    }

    connecting = true;
    MqttClientOptions options = new MqttClientOptions();
    options.setConnectTimeout(MQTT_CONNECT_TIMEOUT_MS);
    options.setUsername(config().getString("_MOD_MQTT_CLIENT_USERNAME"));
    options.setPassword(config().getString("_MOD_MQTT_CLIENT_PASSWORD"));
    options.setReconnectAttempts(0);

    MqttClient client = MqttClient.create(vertx, options);
    mqttClient = client;
    client.exceptionHandler(err -> log.error("mqtt client error", err));
    client.closeHandler(() -> {
      if (mqttClient == client) {
        mqttClient = null;
      }
      if (!stopped) {
        log.info("mqtt client closed, reconnect after {} ms", MQTT_RECONNECT_INTERVAL_MS);
        scheduleReconnect();
      }
    });

    client.connect(config().getInteger("_MOD_MQTT_CLIENT_PORT"), config().getString("_MOD_MQTT_CLIENT_HOST"))
          .subscribe()
          .with(msg -> {
            connecting = false;
            cancelReconnectTimer();
            log.info("mqtt client connected, clientId: {}", client.clientId());
          }, err -> {
            connecting = false;
            if (mqttClient == client) {
              mqttClient = null;
            }
            log.error("mqtt connect failed, reconnect after {} ms", MQTT_RECONNECT_INTERVAL_MS, err);
            scheduleReconnect();
          });
  }

  private void scheduleReconnect() {
    if (stopped || reconnectTimerId != null) {
      return;
    }
    reconnectTimerId = vertx.setTimer(MQTT_RECONNECT_INTERVAL_MS, id -> {
      reconnectTimerId = null;
      connect();
    });
  }

  private void cancelReconnectTimer() {
    if (reconnectTimerId != null) {
      vertx.cancelTimer(reconnectTimerId);
      reconnectTimerId = null;
    }
  }
}
