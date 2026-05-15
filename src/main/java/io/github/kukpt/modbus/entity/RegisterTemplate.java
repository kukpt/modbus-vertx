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

  @Column(name = "tag_name")
  private String tagName;

  private Long version;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "template_locator",
      joinColumns = @JoinColumn(name = "template_id"),
      inverseJoinColumns = @JoinColumn(name = "locator_id")
  )
  @ToString.Exclude
  private List<RegisterLocator> registerLocators = new ArrayList<>();
}
