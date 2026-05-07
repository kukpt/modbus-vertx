package io.github.kukpt.modbus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "register_template")
public class RegisterTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private Long version;

  @ManyToMany(cascade = CascadeType.MERGE)
  @JoinTable(
      name = "template_locator",
      joinColumns = @JoinColumn(name = "template_id"),
      inverseJoinColumns = @JoinColumn(name = "locators_id")
  )
  @ToString.Exclude
  private List<RegisterLocator> registerLocators = new ArrayList<>();
}
