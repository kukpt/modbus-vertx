package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.entity.dto.ModbusDeviceTemplateLocatorVo;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import io.github.kukpt.modbus.repository.core.PageResult;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jctools.queues.MpmcArrayQueue;

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

  public Uni<ModbusDeviceTemplateLocatorVo> findDeviceWithTemplateById(Long id) {
    return withSession(s -> {
      return s.find(ModbusDevice.class, id)
              .onItem().ifNull()
              .failWith(new RuntimeException("未找到设备"))
              .chain(device -> {
                return s.createQuery("""
                            FROM RegisterTemplate e 
                            LEFT JOIN FETCH e.registerLocators
                            WHERE e.id = :id
                            """, RegisterTemplate.class)
                        .setParameter("id", device.getRegisterTemplateId())
                        .getSingleResultOrNull()
                        .map(registerTemplate -> {
                          return ModbusDeviceTemplateLocatorVo.of(device, registerTemplate);
                        });
              });
    });

  }

  public Uni<List<ModbusDeviceTemplateLocatorVo>> findDeviceWithTemplate() {

    return withSession(s -> {
      return s.createQuery("""
                  FROM ModbusDevice d
                  """, ModbusDevice.class)
              .getResultList()
              .chain(devices -> {
                List<Uni<ModbusDeviceTemplateLocatorVo>> vos = devices.stream().map(device -> {
                  return s.createQuery("""
                                FROM RegisterTemplate e 
                                LEFT JOIN FETCH e.registerLocators
                                WHERE e.id = :id
                              """, RegisterTemplate.class)
                          .setParameter("id", device.getRegisterTemplateId())
                          .getSingleResultOrNull()
                          .map(registerTemplate -> {
                            return ModbusDeviceTemplateLocatorVo.of(device, registerTemplate);
                          });
                }).toList();
                return Uni.join().all(vos).andCollectFailures();
              });


    });
  }
}
