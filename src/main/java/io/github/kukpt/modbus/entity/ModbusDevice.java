package io.github.kukpt.modbus.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "modbus_device")
@JsonGen(publicConverter = false)
@DataObject
public class ModbusDevice {

  public ModbusDevice(JsonObject json) {
    ModbusDeviceConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ModbusDeviceConverter.toJson(this, json);
    return json;
  }

  public enum OnlineState{
    ONLINE, OFFLINE
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(name = "online_state")
  @Enumerated(EnumType.STRING)
  private OnlineState onlineState;

  @Column(name = "use_ip")
  private String useIp;

  @Column(name = "use_port")
  private Integer usePort;

  @Column(name = "create_time")
  @CreationTimestamp
  private LocalDateTime createTime;

  @Column(name = "update_time")
  @UpdateTimestamp
  private LocalDateTime updateTime;

  @Column(name = "get_only_changed")
  private Boolean getOnlyChanged;

  @Column(name = "register_template_id")
  private Long registerTemplateId;

  @Column(name = "tag_name")
  private String tagName;

  @Column(name = "collect_interval")
  private Integer collectInterval;

}
