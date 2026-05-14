-- MariaDB dump 10.19  Distrib 10.10.2-MariaDB, for Win64 (AMD64)
--
-- Host: 192.168.1.6    Database: modbus_vertx
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `modbus_device`
--

DROP TABLE IF EXISTS `modbus_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `modbus_device` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `name` varchar(255) DEFAULT NULL,
                                 `online_state` varchar(255) DEFAULT NULL,
                                 `use_ip` varchar(255) DEFAULT NULL,
                                 `use_port` int DEFAULT NULL,
                                 `get_only_changed` bit(1) DEFAULT NULL,
                                 `create_time` datetime DEFAULT NULL,
                                 `update_time` datetime DEFAULT NULL,
                                 `register_template_id` bigint NOT NULL,
                                 `mqtt_publish_topic`   varchar(255) null,
                                 `collect_interval`     int          null,
                                 PRIMARY KEY (`id`),
                                 KEY `modbus_device_ip_port_index` (`use_ip`,`use_port`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modbus_device`
--

LOCK TABLES `modbus_device` WRITE;
/*!40000 ALTER TABLE `modbus_device` DISABLE KEYS */;
INSERT INTO `modbus_device` VALUES
                                (19,'测试-01',NULL,'192.168.1.5',508,NULL,'2026-05-12 10:43:48','2026-05-12 11:57:02',4, NULL, NULL),
                                (22,'测试-02',NULL,'192.168.1.5',508,'\0','2026-05-12 11:32:22','2026-05-14 17:13:34',5, NULL, NULL);
/*!40000 ALTER TABLE `modbus_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `register_locator`
--

DROP TABLE IF EXISTS `register_locator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `register_locator` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `type` varchar(255) DEFAULT NULL,
                                    `name` varchar(255) DEFAULT NULL,
                                    `slave_id` int DEFAULT NULL,
                                    `register_range` int DEFAULT NULL,
                                    `register_offset` int DEFAULT NULL,
                                    `data_type` int DEFAULT NULL,
                                    `register_bit` int DEFAULT NULL,
                                    `create_time` datetime DEFAULT NULL,
                                    `update_time` datetime DEFAULT NULL,
                                    `tag_name` varchar(255) DEFAULT NULL,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_locator`
--

LOCK TABLES `register_locator` WRITE;
/*!40000 ALTER TABLE `register_locator` DISABLE KEYS */;
INSERT INTO `register_locator` VALUES
                                   (1,'NUMERIC_LOCATOR','40001 入库泵温度1',1,3,0,8,-1,'2026-05-07 17:23:52','2026-05-13 10:35:22','in.pump_1.temp'),
                                   (3,'NUMERIC_LOCATOR','40003 入库泵温度2',1,3,2,8,-1,'2026-05-12 14:26:06','2026-05-13 10:35:26','in.pump_2.temp'),
                                   (4,'NUMERIC_LOCATOR','40005 入库泵温度3',1,3,4,8,-1,'2026-05-13 10:47:08',NULL,'in.pump_3.temp'),
                                   (5,'NUMERIC_LOCATOR','40007 入库泵温度4',1,3,6,8,-1,'2026-05-13 10:47:08',NULL,'in.pump_4.temp'),
                                   (6,'NUMERIC_LOCATOR','40009 入库泵站前池液位',1,3,8,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (7,'NUMERIC_LOCATOR','40011 入库泵站出水池液位',1,3,10,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (8,'NUMERIC_LOCATOR','40013 进水浊度',1,3,12,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (9,'NUMERIC_LOCATOR','40015 进水浊度温度',1,3,14,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (10,'NUMERIC_LOCATOR','40017 进水溶解氧',1,3,16,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (11,'NUMERIC_LOCATOR','40019 进水溶解氧温度',1,3,18,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (12,'NUMERIC_LOCATOR','40021 进水PH值',1,3,20,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (13,'NUMERIC_LOCATOR','40023 进水PH值温度',1,3,22,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (14,'NUMERIC_LOCATOR','40025 进水电导率',1,3,24,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (15,'NUMERIC_LOCATOR','40027 进水电导率温度',1,3,26,8,-1,'2026-05-13 10:47:08',NULL,NULL),
                                   (16,'NUMERIC_LOCATOR','40029 一号泵电流',1,3,28,8,-1,'2026-05-13 10:47:08',NULL,'in.pump_1.current'),
                                   (17,'NUMERIC_LOCATOR','40031 二号泵电流',1,3,30,8,-1,'2026-05-13 10:47:08',NULL,'in.pump_2.current'),
                                   (18,'NUMERIC_LOCATOR','40033 三号泵电流',1,3,32,8,-1,'2026-05-13 10:47:08',NULL,'in.pump_3.current'),
                                   (19,'NUMERIC_LOCATOR','40035 四号泵电流',1,3,34,8,-1,'2026-05-13 10:47:08',NULL,'in.pump_4.current'),
                                   (20,'NUMERIC_LOCATOR','40037 入库闸门1号开度',1,3,36,8,-1,'2026-05-13 10:47:08',NULL,'in.gate_1.opening'),
                                   (21,'NUMERIC_LOCATOR','40039 入库闸门1号左荷重',1,3,38,8,-1,'2026-05-13 10:47:08',NULL,'in.gate_1.left_load'),
                                   (22,'NUMERIC_LOCATOR','40041 入库闸门1号右荷重',1,3,40,8,-1,'2026-05-13 10:47:08',NULL,'in.gate_1.right_load'),
                                   (23,'NUMERIC_LOCATOR','40043 入库闸门2号开度',1,3,42,8,-1,'2026-05-13 10:47:08',NULL,'in.gate_2.opening'),
                                   (24,'NUMERIC_LOCATOR','40045 入库闸门2号左荷重',1,3,44,8,-1,'2026-05-13 10:47:08',NULL,'in.gate_2.left_load'),
                                   (25,'NUMERIC_LOCATOR','40047 入库闸门2号右荷重',1,3,56,8,-1,'2026-05-13 10:47:08',NULL,'in.gate_2.right_load');
/*!40000 ALTER TABLE `register_locator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `register_template`
--

DROP TABLE IF EXISTS `register_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `register_template` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `name` varchar(255) DEFAULT NULL,
                                     `version` bigint DEFAULT NULL,
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_template`
--

LOCK TABLES `register_template` WRITE;
/*!40000 ALTER TABLE `register_template` DISABLE KEYS */;
INSERT INTO `register_template` VALUES
                                    (4,'模板1',1),
                                    (5,'模板1',1);
/*!40000 ALTER TABLE `register_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_locator`
--

DROP TABLE IF EXISTS `template_locator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `template_locator` (
                                    `template_id` bigint DEFAULT NULL,
                                    `locator_id` bigint DEFAULT NULL,
                                    KEY `FK_TEMPLATE_LOCATOR_ON_LOCATOR` (`locator_id`),
                                    KEY `FK_TEMPLATE_LOCATOR_ON_TEMPLATE` (`template_id`),
                                    CONSTRAINT `FK_TEMPLATE_LOCATOR_ON_LOCATOR` FOREIGN KEY (`locator_id`) REFERENCES `register_locator` (`id`),
                                    CONSTRAINT `FK_TEMPLATE_LOCATOR_ON_TEMPLATE` FOREIGN KEY (`template_id`) REFERENCES `register_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_locator`
--

LOCK TABLES `template_locator` WRITE;
/*!40000 ALTER TABLE `template_locator` DISABLE KEYS */;
INSERT INTO `template_locator` VALUES
                                   (4,1),
                                   (4,3),
                                   (5,3);
/*!40000 ALTER TABLE `template_locator` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-14 17:15:38
