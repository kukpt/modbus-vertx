package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.RegisterLocator;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import org.hibernate.reactive.mutiny.Mutiny;

public class RegisterLocatorRepository extends AbstractBaseRepository<RegisterLocator, Long> {

  /**
   * 构造器注入
   *
   * @param sessionFactory
   */
  protected RegisterLocatorRepository(Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @Override
  protected Class<RegisterLocator> entityClass() {
    return RegisterLocator.class;
  }
}
