package crypto.dashboard;

import crypto.DBConnection;
import crypto.model.CryptoCoin;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Dashboard {
	private static LineChart<String, Number> priceChart;
	private static BarChart<String, Number> volumeChart;
	private static VBox portfolioCard;
	private static VBox predictionsPanel;
	private static VBox signalsPanel;
	private static ObservableList<CryptoCoin> coinList = FXCollections.observableArrayList();

	public static void show(StackPane contentArea) {
		VBox content = new VBox(20);
		content.getStyleClass().add("dashboard-content");
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		Label headerLabel = new Label("Crypto Dashboard");
		headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00ff88;");

		portfolioCard = createPortfolioSummaryCard();

		HBox chartsContainer = new HBox(20);
		priceChart = createPriceChart();
		volumeChart = createVolumeChart();
		chartsContainer.getChildren().addAll(priceChart, volumeChart);
		

		predictionsPanel = createPredictionsPanel();
		signalsPanel = createTradingSignalsPanel();

		content.getChildren().addAll(
				headerLabel,
				portfolioCard,
				chartsContainer,
				predictionsPanel,
				signalsPanel);

		contentArea.getChildren().clear();
		contentArea.getChildren().add(content);

		loadCoinData();
		startDataUpdates();
	}

	private static void loadCoinData() {
		coinList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM crypto_coins")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				coinList.add(new CryptoCoin(
						rs.getInt("id"),
						rs.getString("symbol"),
						rs.getString("name"),
						rs.getDouble("owned_amount"),
						rs.getDouble("current_value")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static VBox createPortfolioSummaryCard() {
		VBox card = new VBox(10);
		card.setStyle("-fx-background-color: #333333; -fx-padding: 15; -fx-background-radius: 5;");

		// Calculate portfolio metrics from database
		double totalValue = calculateTotalPortfolioValue();
		double dayChange = calculateDayChange();
		int totalCoins = getTotalCoins();

		GridPane grid = new GridPane();
		grid.setHgap(50);
		grid.setVgap(10);

		// Create labels with IDs
		Label totalValueLabel = createMetricValue(String.format("$%.2f", totalValue));
		totalValueLabel.setId("totalValue");

		Label dayChangeLabel = createMetricValue(String.format("%.2f%%", dayChange));
		dayChangeLabel.setId("dayChange");

		Label totalCoinsLabel = createMetricValue(String.valueOf(totalCoins));
		totalCoinsLabel.setId("totalCoins");

		grid.addRow(0,
				createMetricLabel("Total Value:"),
				createMetricLabel("24h Change:"),
				createMetricLabel("Total Coins:"));

		grid.addRow(1, totalValueLabel, dayChangeLabel, totalCoinsLabel);

		card.getChildren().addAll(
				createHeaderLabel("Portfolio Summary"),
				grid);
		return card;
	}

	private static BarChart<String, Number> createVolumeChart() {
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();

		BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
		volumeChart = chart; // Store reference
		chart.setTitle("Trading Volume");
		chart.setStyle("-fx-text-fill: white;");

		// Load volume data for each coin
		for (CryptoCoin coin : coinList) {
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName(coin.getSymbol());

			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(
							"SELECT volume, timestamp FROM price_history " +
									"WHERE coin_id = ? ORDER BY timestamp DESC LIMIT 24")) {
				stmt.setInt(1, coin.getId());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String time = rs.getTimestamp("timestamp").toLocalDateTime()
							.format(DateTimeFormatter.ofPattern("HH:mm"));
					series.getData().add(new XYChart.Data<>(time, rs.getDouble("volume")));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			chart.getData().add(series);
		}

		return chart;
	}

	private static double calculateDayChange() {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT AVG((current_value - " + "(SELECT price FROM price_history ph WHERE ph.coin_id = c.id "
								+ "AND timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR) "
								+ "ORDER BY timestamp ASC LIMIT 1)) / current_value * 100) as avg_change "
								+ "FROM crypto_coins c")) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getDouble("avg_change");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.0;
	}

	private static int getTotalCoins() {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM crypto_coins")) {
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private static void updatePortfolioData() {
		double totalValue = calculateTotalPortfolioValue();
		double dayChange = calculateDayChange();
		int totalCoins = getTotalCoins();

		Label totalValueLabel = (Label) portfolioCard.lookup("#totalValue");
		Label dayChangeLabel = (Label) portfolioCard.lookup("#dayChange");
		Label totalCoinsLabel = (Label) portfolioCard.lookup("#totalCoins");

		if (totalValueLabel != null) {
			totalValueLabel.setText(String.format("$%.2f", totalValue));
		}
		if (dayChangeLabel != null) {
			dayChangeLabel.setText(String.format("%.2f%%", dayChange));
		}
		if (totalCoinsLabel != null) {
			totalCoinsLabel.setText(String.valueOf(totalCoins));
		}
	}

	private static double calculateTotalPortfolioValue() {
		double totalValue = 0.0;

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT c.owned_amount, ph.price " +
								"FROM crypto_coins c " +
								"LEFT JOIN (SELECT coin_id, price " +
								"          FROM price_history ph1 " +
								"          WHERE (coin_id, timestamp) IN " +
								"                (SELECT coin_id, MAX(timestamp) " +
								"                 FROM price_history " +
								"                 GROUP BY coin_id)) ph " +
								"ON c.id = ph.coin_id")) {

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				double ownedAmount = rs.getDouble("owned_amount");
				double currentPrice = rs.getDouble("price");
				totalValue += ownedAmount * currentPrice;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return totalValue;
	}

	private static void updateVolumeSeries(Connection conn, XYChart.Series<String, Number> series) {
		String symbol = series.getName();
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT volume, timestamp FROM price_history ph " +
						"JOIN crypto_coins c ON ph.coin_id = c.id " +
						"WHERE c.symbol = ? " +
						"ORDER BY timestamp DESC LIMIT 1")) {
			stmt.setString(1, symbol);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String time = rs.getTimestamp("timestamp").toLocalDateTime()
						.format(DateTimeFormatter.ofPattern("HH:mm"));
				series.getData().add(new XYChart.Data<>(time, rs.getDouble("volume")));
				if (series.getData().size() > 24) {
					series.getData().remove(0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void updatePredictions() {
		VBox predictionContent = (VBox) predictionsPanel.lookup("#predictionContent");
		predictionContent.getChildren().clear();

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT p.*, c.symbol FROM predictions p " +
								"JOIN crypto_coins c ON p.coin_id = c.id " +
								"WHERE prediction_date = CURDATE() " +
								"ORDER BY confidence_level DESC LIMIT 3")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				HBox predictionRow = new HBox(20);
				predictionRow.getChildren().addAll(
						createLabel(rs.getString("symbol")),
						createLabel(String.format("$%.2f", rs.getDouble("predicted_price"))),
						createLabel(String.format("%.1f%%", rs.getDouble("confidence_level"))));
				predictionContent.getChildren().add(predictionRow);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void updateSignals() {
		ListView<HBox> signalsList = (ListView<HBox>) signalsPanel.lookup("#signalsList");
		signalsList.getItems().clear();

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT s.*, c.symbol FROM trading_signals s " +
								"JOIN crypto_coins c ON s.coin_id = c.id " +
								"WHERE generated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
								"ORDER BY generated_at DESC")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				HBox signalRow = new HBox(20);
				String signalType = rs.getString("signal_type");
				String style = switch (signalType) {
					case "BUY" -> "-fx-text-fill: #00ff88;";
					case "SELL" -> "-fx-text-fill: #ff6b6b;";
					default -> "-fx-text-fill: #ffffff;";
				};

				Label typeLabel = createLabel(signalType);
				typeLabel.setStyle(style);

				signalRow.getChildren().addAll(
						createLabel(rs.getString("symbol")),
						typeLabel,
						createLabel(String.format("%.1f%%", rs.getDouble("strength"))));
				signalsList.getItems().add(signalRow);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void startDataUpdates() {
		Timeline timeline = new Timeline(
				new javafx.animation.KeyFrame(
						javafx.util.Duration.seconds(30),
						event -> {
							updatePortfolioData();
							updateCharts();
							updatePredictions();
							updateSignals();
						}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	private static LineChart<String, Number> createPriceChart() {
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();

		LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
		priceChart = chart;
		chart.setTitle("Price Trends");
		chart.setStyle("-fx-text-fill: white;");
		chart.getStyleClass().add("chart");

		// Load real-time price data for each coin
		for (CryptoCoin coin : coinList) {
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName(coin.getSymbol() + "/USD");

			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement("SELECT price, timestamp FROM price_history "
							+ "WHERE coin_id = ? ORDER BY timestamp DESC LIMIT 24")) {
				stmt.setInt(1, coin.getId());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String time = rs.getTimestamp("timestamp").toLocalDateTime()
							.format(DateTimeFormatter.ofPattern("HH:mm"));
					series.getData().add(new XYChart.Data<>(time, rs.getDouble("price")));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			chart.getData().add(series);
		}

		return chart;
	}

	private static VBox createPredictionsPanel() {
		VBox panel = new VBox(10);
		panel.setStyle("-fx-background-color: #333333; -fx-padding: 15; -fx-background-radius: 5;");

		panel.getChildren().add(createHeaderLabel("Latest Predictions"));

		// Load latest predictions from database
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT p.*, c.symbol FROM predictions p " + "JOIN crypto_coins c ON p.coin_id = c.id "
								+ "WHERE prediction_date = CURDATE() " + "ORDER BY confidence_level DESC LIMIT 3")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				HBox predictionRow = new HBox(20);
				predictionRow.getChildren().addAll(createLabel(rs.getString("symbol")),
						createLabel(String.format("$%.2f", rs.getDouble("predicted_price"))),
						createLabel(String.format("%.1f%%", rs.getDouble("confidence_level"))));
				panel.getChildren().add(predictionRow);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return panel;
	}

	private static VBox createTradingSignalsPanel() {
		VBox panel = new VBox(10);
		panel.setStyle("-fx-background-color: #333333; -fx-padding: 15; -fx-background-radius: 5;");

		panel.getChildren().add(createHeaderLabel("Active Trading Signals"));

		ListView<HBox> signalsList = new ListView<>();
		signalsList.setStyle("-fx-background-color: transparent;");
		signalsList.setPrefHeight(200);

		// Load active signals from database
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT s.*, c.symbol FROM trading_signals s "
						+ "JOIN crypto_coins c ON s.coin_id = c.id "
						+ "WHERE generated_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " + "ORDER BY generated_at DESC")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				HBox signalRow = new HBox(20);
				String signalType = rs.getString("signal_type");
				String style = switch (signalType) {
					case "BUY" -> "-fx-text-fill: #00ff88;";
					case "SELL" -> "-fx-text-fill: #ff6b6b;";
					default -> "-fx-text-fill: #ffffff;";
				};

				Label typeLabel = createLabel(signalType);
				typeLabel.setStyle(style);

				signalRow.getChildren().addAll(createLabel(rs.getString("symbol")), typeLabel,
						createLabel(String.format("%.1f%%", rs.getDouble("strength"))));
				signalsList.getItems().add(signalRow);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		panel.getChildren().add(signalsList);
		return panel;
	}

	private static void updateCharts() {
		try (Connection conn = DBConnection.getConnection()) {
			for (XYChart.Series<String, Number> series : priceChart.getData()) {
				updatePriceSeries(conn, series);
			}
			for (XYChart.Series<String, Number> series : volumeChart.getData()) {
				updateVolumeSeries(conn, series);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void updatePriceSeries(Connection conn, XYChart.Series<String, Number> series) {
		String symbol = series.getName().split("/")[0];
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT price, timestamp FROM price_history ph " + "JOIN crypto_coins c ON ph.coin_id = c.id "
						+ "WHERE c.symbol = ? " + "ORDER BY timestamp DESC LIMIT 1")) {
			stmt.setString(1, symbol);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String time = rs.getTimestamp("timestamp").toLocalDateTime()
						.format(DateTimeFormatter.ofPattern("HH:mm"));
				series.getData().add(new XYChart.Data<>(time, rs.getDouble("price")));
				if (series.getData().size() > 24) {
					series.getData().remove(0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Label createHeaderLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00ff88;");
		return label;
	}

	private static Label createLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
		return label;
	}

	private static Label createMetricLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
		return label;
	}

	private static Label createMetricValue(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
		return label;
	}
}
