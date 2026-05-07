package io.github.kukpt.modbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuo
 * 寄存器定位器
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "register_locator")
public class RegisterLocator {

  public enum LocatorType {
    BINARY_LOCATOR, STRING_LOCATOR, NUMERIC_LOCATOR
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * TODO
   * 定位器类型
   * @see  com.serotonin.modbus4j.locator.BinaryLocator
   * @see  com.serotonin.modbus4j.locator.StringLocator
   * @see  com.serotonin.modbus4j.locator.NumericLocator
   */
  @Enumerated(EnumType.STRING)
  private LocatorType type;

  private String name;

  /**
   * 从站地址
   */
  @Column(name = "slave_id")
  private Integer slaveId;

  /**
   * 寄存器范围
   * <p>COIL_STATUS=       1      :0x0</p>
   * <p>INPUT_STATUS=      2      :0x10000</p>
   * <p>HOLDING_REGISTER=  3      :0x40000</p>
   * <p>INPUT_REGISTER=    4      :0x30000</p>
   */
  @Column(name = "register_range")
  private Integer registerRange;

  /**
   * 地址偏移
   */
  @Column(name = "register_offset")
  private Integer registerOffset;

  /**
   * 数据类型
   * @see com.serotonin.modbus4j.code.DataType
   */
  @Column(name = "data_type")
  private Integer dataType;

  @Column(name = "register_bit")
  private Integer registerBit;

  @Column(name = "create_time")
  @CreationTimestamp
  private LocalDateTime createTime;

  @Column(name = "update_time")
  @UpdateTimestamp
  private LocalDateTime updateTime;

  @ManyToMany(mappedBy = "registerLocators")
  @ToString.Exclude
  private List<RegisterTemplate> registerTemplate = new ArrayList<>();

}
