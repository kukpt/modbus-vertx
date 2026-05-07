package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import org.hibernate.reactive.mutiny.Mutiny;

public class RegisterTemplateRepository extends AbstractBaseRepository<RegisterTemplate, Long> {
  /**
   * 构造器注入
   *
   * @param sessionFactory
   */
  protected RegisterTemplateRepository(Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @Override
  protected Class<RegisterTemplate> entityClass() {
    return RegisterTemplate.class;
  }
}
