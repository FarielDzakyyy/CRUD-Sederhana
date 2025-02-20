-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jan 06, 2025 at 01:13 PM
-- Server version: 8.0.30
-- PHP Version: 8.2.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `crypto_prediction`
--

-- --------------------------------------------------------

--
-- Table structure for table `crypto_coins`
--

CREATE TABLE `crypto_coins` (
  `id` int NOT NULL,
  `symbol` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `owned_amount` decimal(18,8) DEFAULT '0.00000000',
  `current_value` decimal(18,2) DEFAULT '0.00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `crypto_coins`
--

INSERT INTO `crypto_coins` (`id`, `symbol`, `name`, `owned_amount`, `current_value`) VALUES
(1, 'BTC', 'Bitcoin', '25.00000000', '5000.00');

-- --------------------------------------------------------

--
-- Table structure for table `predictions`
--

CREATE TABLE `predictions` (
  `id` int NOT NULL,
  `coin_id` int NOT NULL,
  `predicted_price` decimal(18,2) NOT NULL,
  `confidence_level` decimal(5,2) NOT NULL,
  `prediction_date` date NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `predictions`
--

INSERT INTO `predictions` (`id`, `coin_id`, `predicted_price`, `confidence_level`, `prediction_date`, `created_at`) VALUES
(2, 1, '0.00', '90.06', '2025-01-07', '2025-01-06 17:12:51'),
(3, 1, '1052.17', '90.90', '2025-01-08', '2025-01-06 17:14:11');

-- --------------------------------------------------------

--
-- Table structure for table `price_history`
--

CREATE TABLE `price_history` (
  `id` int NOT NULL,
  `coin_id` int NOT NULL,
  `price` decimal(18,2) NOT NULL,
  `volume` decimal(18,2) NOT NULL,
  `timestamp` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `price_history`
--

INSERT INTO `price_history` (`id`, `coin_id`, `price`, `volume`, `timestamp`) VALUES
(1, 1, '1200.00', '1.00', '2025-01-01 00:00:00'),
(2, 1, '1100.00', '1.00', '2025-01-04 00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `trading_signals`
--

CREATE TABLE `trading_signals` (
  `id` int NOT NULL,
  `coin_id` int NOT NULL,
  `signal_type` enum('BUY','SELL','HOLD') NOT NULL,
  `strength` decimal(5,2) NOT NULL,
  `generated_at` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `trading_signals`
--

INSERT INTO `trading_signals` (`id`, `coin_id`, `signal_type`, `strength`, `generated_at`) VALUES
(1, 1, 'SELL', '50.00', '2025-01-06 16:44:33'),
(2, 1, 'HOLD', '50.00', '2025-01-06 17:14:37');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `crypto_coins`
--
ALTER TABLE `crypto_coins`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `predictions`
--
ALTER TABLE `predictions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `coin_id` (`coin_id`);

--
-- Indexes for table `price_history`
--
ALTER TABLE `price_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `coin_id` (`coin_id`);

--
-- Indexes for table `trading_signals`
--
ALTER TABLE `trading_signals`
  ADD PRIMARY KEY (`id`),
  ADD KEY `coin_id` (`coin_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `crypto_coins`
--
ALTER TABLE `crypto_coins`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `predictions`
--
ALTER TABLE `predictions`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `price_history`
--
ALTER TABLE `price_history`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `trading_signals`
--
ALTER TABLE `trading_signals`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `predictions`
--
ALTER TABLE `predictions`
  ADD CONSTRAINT `predictions_ibfk_1` FOREIGN KEY (`coin_id`) REFERENCES `crypto_coins` (`id`);

--
-- Constraints for table `price_history`
--
ALTER TABLE `price_history`
  ADD CONSTRAINT `price_history_ibfk_1` FOREIGN KEY (`coin_id`) REFERENCES `crypto_coins` (`id`);

--
-- Constraints for table `trading_signals`
--
ALTER TABLE `trading_signals`
  ADD CONSTRAINT `trading_signals_ibfk_1` FOREIGN KEY (`coin_id`) REFERENCES `crypto_coins` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
