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
                                 `register_template_id` bigint DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uc_modbus_device_register_template` (`register_template_id`),
                                 UNIQUE KEY `modbus_device_ip_port_index` (`use_ip`,`use_port`),
                                 CONSTRAINT `FK_MODBUS_DEVICE_ON_REGISTER_TEMPLATE` FOREIGN KEY (`register_template_id`) REFERENCES `register_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modbus_device`
--

LOCK TABLES `modbus_device` WRITE;
/*!40000 ALTER TABLE `modbus_device` DISABLE KEYS */;
INSERT INTO `modbus_device` VALUES
                                (1,'测试-01',NULL,'192.168.1.5',508,NULL,'2026-05-07 17:29:11','2026-05-09 16:40:57',4),
                                (5,'测试-02',NULL,'192.168.1.22',503,NULL,'2026-05-11 17:22:43','2026-05-11 17:22:43',NULL);
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
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_locator`
--

LOCK TABLES `register_locator` WRITE;
/*!40000 ALTER TABLE `register_locator` DISABLE KEYS */;
INSERT INTO `register_locator` VALUES
                                   (1,NULL,'温度2',1,3,2,2,-1,'2026-05-07 17:23:52','2026-05-11 11:25:11'),
                                   (2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-09 09:52:52','2026-05-11 11:10:31');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_template`
--

LOCK TABLES `register_template` WRITE;
/*!40000 ALTER TABLE `register_template` DISABLE KEYS */;
INSERT INTO `register_template` VALUES
    (4,'模板1',1);
/*!40000 ALTER TABLE `register_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_locator`
--

DROP TABLE IF EXISTS `template_locator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `template_locator` (
                                    `locators_id` bigint NOT NULL,
                                    `template_id` bigint NOT NULL,
                                    KEY `fk_temloc_on_register_locator` (`locators_id`),
                                    KEY `fk_temloc_on_register_template` (`template_id`),
                                    CONSTRAINT `fk_temloc_on_register_locator` FOREIGN KEY (`locators_id`) REFERENCES `register_locator` (`id`),
                                    CONSTRAINT `fk_temloc_on_register_template` FOREIGN KEY (`template_id`) REFERENCES `register_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_locator`
--

LOCK TABLES `template_locator` WRITE;
/*!40000 ALTER TABLE `template_locator` DISABLE KEYS */;
INSERT INTO `template_locator` VALUES
                                   (1,4),
                                   (2,4);
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

-- Dump completed on 2026-05-11 17:23:21
