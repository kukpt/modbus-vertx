package io.github.kukpt.modbus.repository;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.repository.core.AbstractBaseRepository;
import org.hibernate.reactive.mutiny.Mutiny;

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
}
