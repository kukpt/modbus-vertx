package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import io.github.kukpt.modbus.repository.core.PageResult;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Map;

public class ModbusDeviceRepository extends AbstractBaseRepository<ModbusDevice, Long> {

  /**
   *
   * @param sessionFactory
   */
  public ModbusDeviceRepository(Mutiny.SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @Override
  protected Class<ModbusDevice> entityClass() {
    return ModbusDevice.class;
  }


  public Uni<PageResult<ModbusDevice>> findDeviceWithTemplate(int page, int size) {
    String dataHql =
        """
            FROM ModbusDevice d 
            LEFT JOIN FETCH d.registerTemplate 
            LEFT JOIN FETCH d.registerTemplate.registerLocators
            """;
    String countHql = """
          SELECT COUNT(e) FROM ModbusDevice e
        """;
    return findPageByHql(dataHql, countHql, Map.of(), page, size);
  }

  public Uni<ModbusDevice> findDeviceWithTemplateById(Long id) {
    String dataHql =
        """
            FROM ModbusDevice d 
            LEFT JOIN FETCH d.registerTemplate 
            LEFT JOIN FETCH d.registerTemplate.registerLocators
            WHERE d.id = :id
            """;
    return findByHql(dataHql, id);
  }

  public Uni<List<ModbusDevice>> findDeviceWithTemplate() {
    return findByHql("""
        FROM ModbusDevice d 
            LEFT JOIN FETCH d.registerTemplate 
            LEFT JOIN FETCH d.registerTemplate.registerLocators
        """, Map.of());
  }
}
