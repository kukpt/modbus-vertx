package io.github.kukpt.modbus.repository;

import org.hibernate.reactive.mutiny.Mutiny;

public class _Repository {

  private final ModbusDeviceRepository _ModbusDeviceRepository;

  private final RegisterLocatorRepository _RegisterLocatorRepository;

  private final RegisterTemplateRepository _RegisterTemplateRepository;

  public _Repository(Mutiny.SessionFactory sf) {
    this._ModbusDeviceRepository = new ModbusDeviceRepository(sf);
    this._RegisterLocatorRepository = new RegisterLocatorRepository(sf);
    this._RegisterTemplateRepository = new RegisterTemplateRepository(sf);
  }

  public RegisterTemplateRepository template() {
    return _RegisterTemplateRepository;
  }

  public ModbusDeviceRepository device() {
    return _ModbusDeviceRepository;
  }

  public RegisterLocatorRepository locator(){
    return _RegisterLocatorRepository;
  }

}
