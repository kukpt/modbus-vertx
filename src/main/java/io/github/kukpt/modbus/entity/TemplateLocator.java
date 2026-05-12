package io.github.kukpt.modbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "template_locator")
public class TemplateLocator {
  @Id
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "template_id")
  private RegisterTemplate registerTemplate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "locator_id")
  private RegisterLocator registerLocator;

}
