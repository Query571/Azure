-- MySQL dump 10.13  Distrib 8.0.29, for Win64 (x86_64)
--
-- Host: localhost    Database: azx
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `azure_instance_types`
--

DROP TABLE IF EXISTS `azure_instance_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `azure_instance_types` (
  `id` int NOT NULL,
  `vm_size` varchar(45) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `vCore` int DEFAULT NULL,
  `memory_In_GB` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `azure_instance_types`
--

LOCK TABLES `azure_instance_types` WRITE;
/*!40000 ALTER TABLE `azure_instance_types` DISABLE KEYS */;
INSERT INTO `azure_instance_types` VALUES (1,'Standard_A1_v2',1,2),(2,'Standard_A2_v2',2,4),(3,'Standard_A4_v2',4,8),(4,'Standard_A8_v2',8,16),(5,'Standard_A2m_v2',2,16),(6,'Standard_A4m_v2',4,32),(7,'Standard_A8m_v2',8,64),(8,'Standard_B1ls1',1,1),(9,'Standard_B1s',1,1),(10,'Standard_B1ms',1,2),(11,'Standard_B2s',2,4),(12,'Standard_B2ms',2,8),(13,'Standard_B4ms',4,16),(14,'Standard_B8ms',8,32),(15,'Standard_B12ms',12,48),(16,'Standard_B16ms',16,64),(17,'Standard_B20ms',20,80),(18,'Standard_DC1s_v21',1,4),(19,'Standard_DC2s_v2',2,8),(20,'Standard_DC4s_v2',4,16),(21,'Standard_DC8_v2',8,32),(22,'Standard_D1_v2',1,4),(23,'Standard_D2_v2',2,7),(24,'Standard_D3_v2',4,14),(25,'Standard_D4_v2',8,28),(26,'Standard_D5_v2',16,56),(27,'Standard_D2_v3',2,8),(28,'Standard_D4_v3',4,16),(29,'Standard_D8_v3',8,32),(30,'Standard_D16_v3',16,64),(31,'Standard_D32_v3',32,128),(32,'Standard_D48_v3',42,192),(33,'Standard_D64_v3',64,256),(34,'Standard_DS1_v2',1,4),(35,'Standard_DS2_v2',2,7),(36,'Standard_DS3_v2',4,14),(37,'Standard_DS4_v2',8,28),(38,'Standard_DS5_v2',16,56),(39,'Standard_D2s_v3',2,8),(40,'Standard_D4s_v3',4,16),(41,'Standard_D8s_v3',8,32),(42,'Standard_D16s_v3',16,64),(43,'Standard_D32s_v3',32,128),(44,'Standard_D48s_v3',48,192),(45,'Standard_D64s_v3',64,256),(46,'Standard_D2a_v4',4,8),(47,'Standard_D4a_v4',4,16),(48,'Standard_D8a_v4',8,32),(49,'Standard_D16a_v4',16,64),(50,'Standard_D32a_v4',32,128),(51,'Standard_D48a_v4',48,192),(52,'Standard_D64a_v4',64,256),(53,'Standard_D96a_v4',96,384),(54,'Standard_D2as_v4',2,8),(55,'Standard_D4as_v4',4,16),(56,'Standard_D8as_v4',8,32),(57,'Standard_D16as_v4',16,64),(58,'Standard_D32as_v4',32,128),(59,'Standard_D48as_v4',48,192),(60,'Standard_D64as_v4',64,256),(61,'Standard_D96as_v4',96,384),(62,'Standard_D2d_v4',2,8),(63,'Standard_D4d_v4',4,16),(64,'Standard_D8d_v4',8,32),(65,'Standard_D16d_v4',16,64),(66,'Standard_D32d_v4',32,128),(67,'Standard_D48d_v4',48,192),(68,'Standard_D64d_v4',64,256),(69,'Standard_D2_v4',2,8),(70,'Standard_D4_v4',4,16),(71,'Standard_D8_v4',8,32),(72,'Standard_D16_v4',16,64),(73,'Standard_D32_v4',32,128),(74,'Standard_D48_v4',48,192),(75,'Standard_D64_v4',64,256),(76,'Standard_D2s_v4',2,8),(77,'Standard_D4s_v4',4,16),(78,'Standard_D8s_v4',8,32),(79,'Standard_D16s_v4',16,64),(80,'Standard_D32s_v4',32,128),(81,'Standard_D48s_v4',48,192),(82,'Standard_D64s_v4',64,256);
/*!40000 ALTER TABLE `azure_instance_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devops_token`
--

DROP TABLE IF EXISTS `devops_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devops_token` (
  `id` int NOT NULL AUTO_INCREMENT,
  `token` varchar(1145) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devops_token`
--

LOCK TABLES `devops_token` WRITE;
/*!40000 ALTER TABLE `devops_token` DISABLE KEYS */;
INSERT INTO `devops_token` VALUES (1,NULL);
/*!40000 ALTER TABLE `devops_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Admin'),(2,'Auditor'),(3,'Operator ');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(445) DEFAULT NULL,
  `password` text,
  `first_name` text,
  `last_name` text,
  `created_at` text,
  `email` varchar(245) DEFAULT NULL,
  `contact_number` varchar(45) DEFAULT NULL,
  `department` varchar(45) DEFAULT NULL,
  `user_uid` varchar(1145) DEFAULT NULL,
  `disable` tinyint DEFAULT NULL,
  `failed_attempts` int DEFAULT NULL,
  `jwttoken_status` varchar(45) DEFAULT '0',
  `jwt_expire_time` datetime DEFAULT NULL,
  `jwt_create_time` datetime DEFAULT NULL,
  `jwttoken_auth` varchar(11145) DEFAULT NULL,
  `role_id` varchar(45) DEFAULT NULL,
  `subscription_id` varchar(45) DEFAULT NULL,
  `devops_token` varchar(445) DEFAULT NULL,
  `secret_key` varchar(145) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$1BtuJSG2X5XcEXqwozSCOONUYhhIrvycQyoC8HvIrrm6yc9I2zZza','Local','Admin','2020-05-05 10:30:09.0',NULL,NULL,NULL,'7449ca98-c541-46b7-befd-7f6ae9cd0iu7-65e94',0,0,'0',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_roles`
--

DROP TABLE IF EXISTS `users_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_roles` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  KEY `role_fk_idx` (`role_id`),
  KEY `user_fk_idx` (`user_id`),
  CONSTRAINT `role_fk` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`),
  CONSTRAINT `user_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_roles`
--

LOCK TABLES `users_roles` WRITE;
/*!40000 ALTER TABLE `users_roles` DISABLE KEYS */;
INSERT INTO `users_roles` VALUES (1,1);
/*!40000 ALTER TABLE `users_roles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-08-03 16:54:25
