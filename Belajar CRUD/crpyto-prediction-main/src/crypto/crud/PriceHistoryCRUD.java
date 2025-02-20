package crypto.crud;

import crypto.DBConnection;
import crypto.model.PriceHistory;
import crypto.model.CryptoCoin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PriceHistoryCRUD {
	private static TableView<PriceHistory> table;
	private static ObservableList<PriceHistory> priceList = FXCollections.observableArrayList();
	private static ObservableList<CryptoCoin> coinList = FXCollections.observableArrayList();

	public static void show(StackPane contentArea) {
		table = new TableView<>();

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		Label headerLabel = new Label("Price History Data");
		headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00ff88;");

		TableColumn<PriceHistory, Number> colPrice = new TableColumn<>("Price ($)");
		colPrice.setCellValueFactory(data -> data.getValue().priceProperty());
		colPrice.setPrefWidth(150);

		TableColumn<PriceHistory, Number> colVolume = new TableColumn<>("Volume");
		colVolume.setCellValueFactory(data -> data.getValue().volumeProperty());
		colVolume.setPrefWidth(150);

		TableColumn<PriceHistory, LocalDateTime> colTimestamp = new TableColumn<>("Timestamp");
		colTimestamp.setCellValueFactory(data -> data.getValue().timestampProperty());
		colTimestamp.setPrefWidth(200);

		table.getColumns().addAll(colPrice, colVolume, colTimestamp);
		table.setItems(priceList);
		table.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

		HBox searchBox = new HBox(10);
		ComboBox<CryptoCoin> coinFilter = new ComboBox<>(coinList);
		DatePicker dateFilter = new DatePicker();
		Button searchBtn = new Button("Search");
		searchBox.getChildren().addAll(new Label("Coin:"), coinFilter, new Label("Date:"), dateFilter, searchBtn);
		searchBox.setStyle("-fx-text-fill: white;");

		HBox actionBox = new HBox(10);
		Button addBtn = new Button("Add Price Data");
		Button editBtn = new Button("Edit");
		Button deleteBtn = new Button("Delete");
		Button analyzeBtn = new Button("Analyze Trend");
		actionBox.getChildren().addAll(addBtn, editBtn, deleteBtn, analyzeBtn);

		content.getChildren().addAll(headerLabel, searchBox, table, actionBox);

		addBtn.setOnAction(e -> showAddForm());
		editBtn.setOnAction(e -> showEditForm());
		deleteBtn.setOnAction(e -> deletePrice());
		analyzeBtn.setOnAction(e -> showAnalysis());
		searchBtn.setOnAction(e -> searchPrices(coinFilter.getValue(), dateFilter.getValue()));

		contentArea.getChildren().clear();
		contentArea.getChildren().add(content);

		loadCoinData();
		loadPriceData();
	}

	private static void setupTable() {
		table.getColumns().clear();

		TableColumn<PriceHistory, Number> colPrice = new TableColumn<>("Price ($)");
		colPrice.setCellValueFactory(data -> data.getValue().priceProperty());
		colPrice.setPrefWidth(150);

		TableColumn<PriceHistory, Number> colVolume = new TableColumn<>("Volume");
		colVolume.setCellValueFactory(data -> data.getValue().volumeProperty());
		colVolume.setPrefWidth(150);

		TableColumn<PriceHistory, LocalDateTime> colTimestamp = new TableColumn<>("Timestamp");
		colTimestamp.setCellValueFactory(data -> data.getValue().timestampProperty());
		colTimestamp.setPrefWidth(200);

		table.getColumns().addAll(colPrice, colVolume, colTimestamp);
		table.setItems(priceList);
		table.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
	}

	private static void loadCoinData() {
		coinList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM crypto_coins")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				coinList.add(new CryptoCoin(rs.getInt("id"), rs.getString("symbol"), rs.getString("name"),
						rs.getDouble("owned_amount"), rs.getDouble("current_value")));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error loading coin data: " + e.getMessage());
		}
	}

	private static void loadPriceData() {
		priceList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM price_history ORDER BY timestamp DESC")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				priceList.add(new PriceHistory(rs.getInt("id"), rs.getInt("coin_id"), rs.getDouble("price"),
						rs.getDouble("volume"), rs.getTimestamp("timestamp").toLocalDateTime()));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error loading price data: " + e.getMessage());
		}
	}

	private static void showAddForm() {
		Stage formStage = new Stage();
		formStage.setTitle("Add Price Data");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #262626;");

		ComboBox<CryptoCoin> coinCombo = new ComboBox<>(coinList);
		TextField priceField = new TextField();
		TextField volumeField = new TextField();
		DatePicker datePicker = new DatePicker();

		styleFormField(priceField);
		styleFormField(volumeField);

		grid.addRow(0, createLabel("Coin:"), coinCombo);
		grid.addRow(1, createLabel("Price ($):"), priceField);
		grid.addRow(2, createLabel("Volume:"), volumeField);
		grid.addRow(3, createLabel("Date:"), datePicker);

		Button saveButton = new Button("Save");
		saveButton.getStyleClass().add("action-button");

		saveButton.setOnAction(e -> {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(
							"INSERT INTO price_history (coin_id, price, volume, timestamp) VALUES (?, ?, ?, ?)")) {
				stmt.setInt(1, coinCombo.getValue().getId());
				stmt.setDouble(2, Double.parseDouble(priceField.getText()));
				stmt.setDouble(3, Double.parseDouble(volumeField.getText()));
				stmt.setTimestamp(4, Timestamp.valueOf(datePicker.getValue().atStartOfDay()));
				stmt.executeUpdate();
				loadPriceData();
				formStage.close();
			} catch (SQLException ex) {
				showAlert("Error", "Error saving data: " + ex.getMessage());
			}
		});

		VBox layout = new VBox(20);
		layout.getChildren().addAll(grid, saveButton);
		layout.setPadding(new Insets(10));
		layout.setStyle("-fx-background-color: #262626;");

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(PriceHistoryCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		formStage.setScene(scene);
		formStage.show();
	}

	private static Label createLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: white;");
		return label;
	}

	private static void styleFormField(TextField field) {
		field.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
	}

	private static void showEditForm() {
		PriceHistory selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert("Warning", "Please select a price record to edit");
			return;
		}

		Stage formStage = new Stage();
		formStage.setTitle("Edit Price Data");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #262626;");

		TextField priceField = new TextField(String.valueOf(selected.getPrice()));
		TextField volumeField = new TextField(String.valueOf(selected.getVolume()));
		DatePicker datePicker = new DatePicker(selected.getTimestamp().toLocalDate());

		styleFormField(priceField);
		styleFormField(volumeField);

		grid.addRow(0, createLabel("Price ($):"), priceField);
		grid.addRow(1, createLabel("Volume:"), volumeField);
		grid.addRow(2, createLabel("Date:"), datePicker);

		Button saveButton = new Button("Update");
		saveButton.getStyleClass().add("action-button");

		saveButton.setOnAction(e -> {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn
							.prepareStatement("UPDATE price_history SET price=?, volume=?, timestamp=? WHERE id=?")) {
				stmt.setDouble(1, Double.parseDouble(priceField.getText()));
				stmt.setDouble(2, Double.parseDouble(volumeField.getText()));
				stmt.setTimestamp(3, Timestamp.valueOf(datePicker.getValue().atStartOfDay()));
				stmt.setInt(4, selected.getId());
				stmt.executeUpdate();
				loadPriceData();
				formStage.close();
			} catch (SQLException ex) {
				showAlert("Error", "Error updating data: " + ex.getMessage());
			}
		});

		VBox layout = new VBox(20);
		layout.getChildren().addAll(grid, saveButton);
		layout.setPadding(new Insets(10));
		layout.setStyle("-fx-background-color: #262626;");

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(PriceHistoryCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		formStage.setScene(scene);
		formStage.show();
	}

	private static void deletePrice() {
		PriceHistory selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert("Warning", "Please select a price record to delete");
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm Delete");
		alert.setContentText("Are you sure you want to delete this price record?");

		if (alert.showAndWait().get() == ButtonType.OK) {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement("DELETE FROM price_history WHERE id = ?")) {
				stmt.setInt(1, selected.getId());
				stmt.executeUpdate();
				loadPriceData();
			} catch (SQLException e) {
				showAlert("Error", "Error deleting data: " + e.getMessage());
			}
		}
	}

	private static void showAnalysis() {
		Stage analysisStage = new Stage();
		analysisStage.setTitle("Price Analysis & Prediction");

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		// Create chart for price visualization
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		LineChart<String, Number> priceChart = new LineChart<>(xAxis, yAxis);
		priceChart.setTitle("Price Trend Analysis");
		priceChart.setStyle("-fx-text-fill: white;");

		XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
		priceSeries.setName("Historical Prices");

		XYChart.Series<String, Number> predictionSeries = new XYChart.Series<>();
		predictionSeries.setName("Predicted Prices");

		// Load historical data
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT price, timestamp FROM price_history ORDER BY timestamp ASC LIMIT 30")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String date = rs.getTimestamp("timestamp").toLocalDateTime()
						.format(DateTimeFormatter.ofPattern("MM-dd"));
				double price = rs.getDouble("price");
				priceSeries.getData().add(new XYChart.Data<>(date, price));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error loading historical data: " + e.getMessage());
		}

		// Calculate predictions using moving average
		if (!priceSeries.getData().isEmpty()) {
			double[] prices = priceSeries.getData().stream().mapToDouble(data -> (double) data.getYValue()).toArray();

			double[] predictions = calculatePredictions(prices);

			// Add predictions to chart
			LocalDateTime lastDate = LocalDateTime.now();
			for (int i = 0; i < 7; i++) {
				lastDate = lastDate.plusDays(1);
				String date = lastDate.format(DateTimeFormatter.ofPattern("MM-dd"));
				predictionSeries.getData().add(new XYChart.Data<>(date, predictions[i]));
			}
		}

		priceChart.getData().addAll(priceSeries, predictionSeries);

		// Add analysis metrics
		GridPane metricsGrid = new GridPane();
		metricsGrid.setHgap(20);
		metricsGrid.setVgap(10);
		metricsGrid.setStyle("-fx-text-fill: white;");

		double[] prices = priceSeries.getData().stream().mapToDouble(data -> (double) data.getYValue()).toArray();

		metricsGrid.addRow(0, createMetricLabel("Average Price:"), createMetricValue(calculateAverage(prices)));
		metricsGrid.addRow(1, createMetricLabel("Price Volatility:"), createMetricValue(calculateVolatility(prices)));
		metricsGrid.addRow(2, createMetricLabel("Trend Direction:"), createMetricValue(determineTrend(prices)));

		content.getChildren().addAll(priceChart, metricsGrid);

		Scene scene = new Scene(content, 800, 600);
		scene.getStylesheets().add(PriceHistoryCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		analysisStage.setScene(scene);
		analysisStage.show();
	}

	private static double[] calculatePredictions(double[] prices) {
		double[] predictions = new double[7];
		int windowSize = 5;

		// Simple Moving Average prediction
		double sum = 0;
		for (int i = prices.length - windowSize; i < prices.length; i++) {
			sum += prices[i];
		}
		double movingAverage = sum / windowSize;

		// Linear regression for trend
		double trend = (prices[prices.length - 1] - prices[prices.length - windowSize]) / windowSize;

		// Generate predictions
		for (int i = 0; i < predictions.length; i++) {
			predictions[i] = movingAverage + (trend * (i + 1));
		}

		return predictions;
	}

	private static Label createMetricLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold;");
		return label;
	}

	private static Label createMetricValue(double value) {
		Label label = new Label(String.format("%.2f", value));
		label.setStyle("-fx-text-fill: white;");
		return label;
	}

	private static Label createMetricValue(String value) {
		Label label = new Label(value);
		label.setStyle("-fx-text-fill: white;");
		return label;
	}

	private static double calculateAverage(double[] prices) {
		return Arrays.stream(prices).average().orElse(0.0);
	}

	private static double calculateVolatility(double[] prices) {
		double avg = calculateAverage(prices);
		double sumSquaredDiff = Arrays.stream(prices).map(price -> Math.pow(price - avg, 2)).sum();
		return Math.sqrt(sumSquaredDiff / prices.length);
	}

	private static String determineTrend(double[] prices) {
		if (prices.length < 2)
			return "Neutral";

		double firstHalfAvg = Arrays.stream(prices, 0, prices.length / 2).average().orElse(0);
		double secondHalfAvg = Arrays.stream(prices, prices.length / 2, prices.length).average().orElse(0);

		if (secondHalfAvg > firstHalfAvg * 1.05)
			return "Bullish ↑";
		if (secondHalfAvg < firstHalfAvg * 0.95)
			return "Bearish ↓";
		return "Neutral →";
	}

	// In PriceHistoryCRUD.java, change the method signature:
	private static void searchPrices(CryptoCoin coin, LocalDate date) {
		if (coin == null) {
			loadPriceData();
			return;
		}

		priceList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT * FROM price_history WHERE coin_id = ? AND DATE(timestamp) = ? ORDER BY timestamp DESC")) {
			stmt.setInt(1, coin.getId());
			stmt.setDate(2, Date.valueOf(date));
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				priceList.add(new PriceHistory(rs.getInt("id"), rs.getInt("coin_id"), rs.getDouble("price"),
						rs.getDouble("volume"), rs.getTimestamp("timestamp").toLocalDateTime()));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error searching data: " + e.getMessage());
		}
	}

	private static void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
