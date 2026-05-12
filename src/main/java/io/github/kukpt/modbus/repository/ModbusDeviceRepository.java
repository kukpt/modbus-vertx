package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.entity.dto.ModbusDeviceTemplateLocatorVo;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import io.github.kukpt.modbus.repository.core.PageResult;
import io.github.kukpt.modbus.repository.core.QuerySpec;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

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


  public Uni<PageResult<ModbusDeviceTemplateLocatorVo>> findDeviceWithTemplate(int page, int pageSize) {
    validatePage(page, pageSize);

    Uni<Long> totalUni = countBySpec(QuerySpec.create());
    Uni<List<ModbusDeviceTemplateLocatorVo>> dataUni = withSession(s -> {
      return s.createQuery("""
                             FROM ModbusDevice d
                             """, ModbusDevice.class)
              .setFirstResult((page - 1) * pageSize)
              .setMaxResults(pageSize)
              .getResultList()
              .onItem().transformToMulti(list -> Multi.createFrom().iterable(list))
              .onItem().transformToUniAndConcatenate(device -> {
          return s.createQuery("""
                                   FROM RegisterTemplate e
                                   LEFT JOIN FETCH e.registerLocators
                                   WHERE e.id = :id
                                 """, RegisterTemplate.class)
                  .setParameter("id", device.getRegisterTemplateId())
                  .getSingleResultOrNull()
                  .map(template -> ModbusDeviceTemplateLocatorVo.of(device, template));
        }).collect().asList();
    });
    return Uni.combine().all()
              .unis(totalUni, dataUni)
              .asTuple()
              .map(t -> new PageResult<>(t.getItem2(), t.getItem1(), page, pageSize));

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
              .onItem().transformToMulti(list -> Multi.createFrom().iterable(list))
              .onItem().transformToUniAndConcatenate(device -> {
          return s.createQuery("""
                                   FROM RegisterTemplate e
                                   LEFT JOIN FETCH e.registerLocators
                                   WHERE e.id = :id
                                 """, RegisterTemplate.class)
                  .setParameter("id", device.getRegisterTemplateId())
                  .getSingleResultOrNull()
                  .map(template -> ModbusDeviceTemplateLocatorVo.of(device, template));
        }).collect().asList();


    });
  }
}
