package io.github.kukpt.modbus.common;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;

public class ConnectionUtil {
  public static ModbusMaster createMaster(String host, int port) {
    IpParameters parameters = new IpParameters();
    parameters.setHost(host);
    parameters.setPort(port);
    ModbusFactory modbusFactory = new ModbusFactory();
    ModbusMaster tcpMaster = modbusFactory.createTcpMaster(parameters, true);
    tcpMaster.setTimeout(1500);
    tcpMaster.setRetries(10);
    return tcpMaster;
  }
}
