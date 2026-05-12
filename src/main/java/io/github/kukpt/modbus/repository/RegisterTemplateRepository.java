package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import io.github.kukpt.modbus.repository.core.PageResult;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Map;

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


  public Uni<PageResult<RegisterTemplate>> findTemplateWithLocators(int page, int size) {
    String dataHql =
        """
            FROM RegisterTemplate e 
            LEFT JOIN FETCH e.registerLocators
            """;
    String countHql = """
          SELECT COUNT(e) FROM RegisterTemplate e
        """;
    return findPageByHql(dataHql, countHql, Map.of(), page, size);
  }


  public Uni<RegisterTemplate> findDeviceWithTemplateById(Long id) {
    String dataHql =
        """
            FROM RegisterTemplate e 
            LEFT JOIN FETCH e.registerLocators
            WHERE e.id = :id
            """;
    return findByHql(dataHql, id);
  }
}
