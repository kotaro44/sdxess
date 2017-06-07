-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 03-06-2017 a las 00:29:43
-- Versión del servidor: 5.6.21
-- Versión de PHP: 5.6.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `db_sdxess`
--
CREATE DATABASE IF NOT EXISTS `db_sdxess` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `db_sdxess`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sdxess_subs_active`
--

DROP TABLE IF EXISTS `sdxess_subs_active`;
CREATE TABLE IF NOT EXISTS `sdxess_subs_active` (
  `account_number` varchar(8) NOT NULL,
  `start_time` datetime NOT NULL,
  `expiry_time` datetime NOT NULL,
  `payment_id` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `sdxess_subs_active`
--

INSERT INTO `sdxess_subs_active` (`account_number`, `start_time`, `expiry_time`, `payment_id`) VALUES
('A0000001', '2017-06-03 00:28:41', '2017-06-03 00:28:41', 0);

--
-- Disparadores `sdxess_subs_active`
--
DROP TRIGGER IF EXISTS `log_subscription`;
DELIMITER //
CREATE TRIGGER `log_subscription` BEFORE INSERT ON `sdxess_subs_active`
 FOR EACH ROW insert into `sdxess_subs_log` SET `account_number` = NEW.`account_number`, `start_time` = NEW.`start_time`,`expiry_time` = NEW.`expiry_time`,`payment_id` = NEW.`payment_id`
//
DELIMITER ;
DROP TRIGGER IF EXISTS `log_subscription_update`;
DELIMITER //
CREATE TRIGGER `log_subscription_update` AFTER UPDATE ON `sdxess_subs_active`
 FOR EACH ROW insert into `sdxess_subs_log` SET `account_number` = old.`account_number`, `start_time` = NEW.`start_time`,`expiry_time` = NEW.`expiry_time`,`payment_id` = NEW.`payment_id`
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sdxess_subs_log`
--

DROP TABLE IF EXISTS `sdxess_subs_log`;
CREATE TABLE IF NOT EXISTS `sdxess_subs_log` (
`id` int(15) unsigned zerofill NOT NULL,
  `account_number` varchar(8) NOT NULL,
  `start_time` datetime NOT NULL,
  `expiry_time` datetime NOT NULL,
  `payment_id` int(11) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `sdxess_subs_log`
--

INSERT INTO `sdxess_subs_log` (`id`, `account_number`, `start_time`, `expiry_time`, `payment_id`) VALUES
(000000000000001, 'A0000001', '2017-06-03 00:28:41', '2017-06-03 00:28:41', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sdxess_users`
--

DROP TABLE IF EXISTS `sdxess_users`;
CREATE TABLE IF NOT EXISTS `sdxess_users` (
`user_id` int(15) unsigned zerofill NOT NULL,
  `account_number` varchar(8) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(32) NOT NULL,
  `first_name` varchar(35) NOT NULL,
  `last_name` varchar(35) NOT NULL,
  `country` varchar(3) NOT NULL,
  `address` text NOT NULL,
  `registration_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `sdxess_users`
--

INSERT INTO `sdxess_users` (`user_id`, `account_number`, `username`, `password`, `first_name`, `last_name`, `country`, `address`, `registration_date`, `status`) VALUES
(000000000000001, 'A0000001', 'sdxess@ji8.net', '68808c1cac56078870fbdcf33d9fddd2', 'SDXess', 'Admin', 'CN', 'Haidian District, Beijing', '2017-06-03 00:28:41', 2);

--
-- Disparadores `sdxess_users`
--
DROP TRIGGER IF EXISTS `initial_subscription`;
DELIMITER //
CREATE TRIGGER `initial_subscription` AFTER INSERT ON `sdxess_users`
 FOR EACH ROW insert into `sdxess_subs_active` SET `account_number` = NEW.`account_number`, `start_time` = NOW(),`expiry_time` = NOW(),`payment_id` = 0
//
DELIMITER ;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `sdxess_subs_active`
--
ALTER TABLE `sdxess_subs_active`
 ADD PRIMARY KEY (`account_number`);

--
-- Indices de la tabla `sdxess_subs_log`
--
ALTER TABLE `sdxess_subs_log`
 ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `sdxess_users`
--
ALTER TABLE `sdxess_users`
 ADD PRIMARY KEY (`account_number`), ADD UNIQUE KEY `user_id` (`user_id`), ADD UNIQUE KEY `account_number` (`account_number`), ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `sdxess_subs_log`
--
ALTER TABLE `sdxess_subs_log`
MODIFY `id` int(15) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT de la tabla `sdxess_users`
--
ALTER TABLE `sdxess_users`
MODIFY `user_id` int(15) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
