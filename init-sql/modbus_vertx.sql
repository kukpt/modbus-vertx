-- MariaDB dump 10.19  Distrib 10.10.2-MariaDB, for Win64 (AMD64)
--
-- Host: 47.104.77.193    Database: modbus_test
-- ------------------------------------------------------
-- Server version	5.7.35-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+08:00' */;
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
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `name` varchar(255) DEFAULT NULL,
                                 `online_state` varchar(255) DEFAULT 'OFFLINE',
                                 `use_ip` varchar(255) DEFAULT NULL,
                                 `use_port` int(11) DEFAULT NULL,
                                 `create_time` datetime DEFAULT NULL,
                                 `update_time` datetime DEFAULT NULL,
                                 `register_template_id` bigint(20) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `modbus_device_ip_port_index` (`use_ip`,`use_port`),
                                 KEY `FK_MODBUS_DEVICE_ON_REGISTERTEMPLATE` (`register_template_id`),
                                 CONSTRAINT `FK_MODBUS_DEVICE_ON_REGISTERTEMPLATE` FOREIGN KEY (`register_template_id`) REFERENCES `register_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modbus_device`
--

LOCK TABLES `modbus_device` WRITE;
/*!40000 ALTER TABLE `modbus_device` DISABLE KEYS */;
INSERT INTO `modbus_device` VALUES
    (1,'测试-01',NULL,'192.168.1.34',503,NULL,NULL,NULL);
/*!40000 ALTER TABLE `modbus_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `register_locator`
--

DROP TABLE IF EXISTS `register_locator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `register_locator` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                    `type` varchar(255) DEFAULT NULL,
                                    `name` varchar(255) DEFAULT NULL,
                                    `slave_id` int(11) DEFAULT NULL,
                                    `register_range` int(11) DEFAULT NULL,
                                    `register_offset` int(11) DEFAULT NULL,
                                    `data_type` int(11) DEFAULT NULL,
                                    `register_bit` int(11) DEFAULT NULL,
                                    `create_time` datetime DEFAULT NULL,
                                    `update_time` datetime DEFAULT NULL,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_locator`
--

LOCK TABLES `register_locator` WRITE;
/*!40000 ALTER TABLE `register_locator` DISABLE KEYS */;
/*!40000 ALTER TABLE `register_locator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `register_template`
--

DROP TABLE IF EXISTS `register_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `register_template` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                     `name` varchar(255) DEFAULT NULL,
                                     `version` bigint(20) DEFAULT NULL,
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `register_template`
--

LOCK TABLES `register_template` WRITE;
/*!40000 ALTER TABLE `register_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `register_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-06  9:47:43
