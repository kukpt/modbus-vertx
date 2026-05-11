package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.RegisterLocator;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
