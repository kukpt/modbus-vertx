package io.github.kukpt.modbus.repository.core;

import io.github.kukpt.modbus.entity.ModbusDevice;
import io.github.kukpt.modbus.entity.RegisterLocator;
import io.github.kukpt.modbus.entity.RegisterTemplate;
import io.github.kukpt.modbus.entity.TemplateLocator;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;

import java.util.Map;
import java.util.Properties;


public class DatabaseService {

  private static Mutiny.SessionFactory sessionFactory;
  private static final Object lock = new Object(); // 用于同步初始化

  private DatabaseService() {}

  public static void initialize(Map<String, Object> config) {
    synchronized (lock) {
      if (sessionFactory == null) {
        Properties properties = new Properties();
        properties.putAll(config);

        Configuration configuration = new Configuration();
        configuration.setProperties(properties);
        configuration.addAnnotatedClasses(
            ModbusDevice.class,
            RegisterLocator.class,
            RegisterTemplate.class,
            TemplateLocator.class);
        StandardServiceRegistry build = new ReactiveServiceRegistryBuilder().applySettings(properties).build();

        sessionFactory = configuration.buildSessionFactory(build).unwrap(
          Mutiny.SessionFactory.class);

        if (sessionFactory == null) {
          throw new IllegalStateException("Failed to unwrap Mutiny.SessionFactory.");
        }
      }
    }
  }


  public static Mutiny.SessionFactory getSessionFactory() {
    synchronized (lock) {
      if (sessionFactory == null) {
        throw new IllegalStateException("Mutiny.SessionFactory has not been initialized yet.");
      }
      return sessionFactory;
    }
  }

  public static void closeSessionFactory() {
    synchronized (lock) {
      if (sessionFactory != null) {
        sessionFactory.close();
      }
    }
  }
}
