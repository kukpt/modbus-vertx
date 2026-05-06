package io.github.kukpt.modbus.repository;

import org.hibernate.reactive.mutiny.Mutiny;

public class _Repository {

  private final ModbusDeviceRepository _ModbusDeviceRepository;


  public _Repository(Mutiny.SessionFactory sf) {
    this._ModbusDeviceRepository = new ModbusDeviceRepository(sf);
  }
  public ModbusDeviceRepository device() {
    return _ModbusDeviceRepository;
  }

}
