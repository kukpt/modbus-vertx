package io.github.kukpt.modbus.entity;

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
public class ModbusDevice {

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


  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "register_template_id")
  private RegisterTemplate registerTemplate;

}
