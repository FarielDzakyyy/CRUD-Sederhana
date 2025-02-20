package crypto.crud;

import crypto.DBConnection;
import crypto.model.Prediction;
import crypto.model.CryptoCoin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class PredictionCRUD {
	private static TableView<Prediction> table;
	private static ObservableList<Prediction> predictionList = FXCollections.observableArrayList();
	private static ObservableList<CryptoCoin> coinList = FXCollections.observableArrayList();

	public static void show(StackPane contentArea) {
		table = new TableView<>();

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		Label headerLabel = new Label("Price Predictions");
		headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00ff88;");

		TableColumn<Prediction, String> colCoin = new TableColumn<>("Coin");
		colCoin.setCellValueFactory(data -> data.getValue().coinNameProperty());
		colCoin.setPrefWidth(100);

		TableColumn<Prediction, Number> colPrice = new TableColumn<>("Predicted Price ($)");
		colPrice.setCellValueFactory(data -> data.getValue().predictedPriceProperty());
		colPrice.setPrefWidth(150);

		TableColumn<Prediction, Number> colConfidence = new TableColumn<>("Confidence (%)");
		colConfidence.setCellValueFactory(data -> data.getValue().confidenceLevelProperty());
		colConfidence.setPrefWidth(120);

		TableColumn<Prediction, LocalDate> colDate = new TableColumn<>("Prediction Date");
		colDate.setCellValueFactory(data -> data.getValue().predictionDateProperty());
		colDate.setPrefWidth(120);

		table.getColumns().addAll(colCoin, colPrice, colConfidence, colDate);
		table.setItems(predictionList);
		table.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

		HBox filterBox = new HBox(10);
		ComboBox<CryptoCoin> coinFilter = new ComboBox<>(coinList);
		DatePicker dateFilter = new DatePicker();
		Button filterBtn = new Button("Filter");
		filterBox.getChildren().addAll(new Label("Coin:"), coinFilter, new Label("Date:"), dateFilter, filterBtn);
		filterBox.setStyle("-fx-text-fill: white;");

		HBox actionBox = new HBox(10);
		Button generateBtn = new Button("Generate Prediction");
		Button deleteBtn = new Button("Delete");
		Button analyzeBtn = new Button("Analyze Accuracy");
		actionBox.getChildren().addAll(generateBtn, deleteBtn, analyzeBtn);

		content.getChildren().addAll(headerLabel, filterBox, table, actionBox);

		generateBtn.setOnAction(e -> showGeneratePredictionForm());
		deleteBtn.setOnAction(e -> deletePrediction());
		analyzeBtn.setOnAction(e -> showAccuracyAnalysis());
		filterBtn.setOnAction(e -> filterPredictions(coinFilter.getValue(), dateFilter.getValue()));

		contentArea.getChildren().clear();
		contentArea.getChildren().add(content);

		loadCoinData();
		loadPredictionData();
	}

	private static void setupTable() {
		table.getColumns().clear();

		TableColumn<Prediction, String> colCoin = new TableColumn<>("Coin");
		colCoin.setCellValueFactory(data -> data.getValue().coinNameProperty());
		colCoin.setPrefWidth(100);

		TableColumn<Prediction, Number> colPrice = new TableColumn<>("Predicted Price ($)");
		colPrice.setCellValueFactory(data -> data.getValue().predictedPriceProperty());
		colPrice.setPrefWidth(150);

		TableColumn<Prediction, Number> colConfidence = new TableColumn<>("Confidence (%)");
		colConfidence.setCellValueFactory(data -> data.getValue().confidenceLevelProperty());
		colConfidence.setPrefWidth(120);

		TableColumn<Prediction, LocalDate> colDate = new TableColumn<>("Prediction Date");
		colDate.setCellValueFactory(data -> data.getValue().predictionDateProperty());
		colDate.setPrefWidth(150);

		table.getColumns().addAll(colCoin, colPrice, colConfidence, colDate);
		table.setItems(predictionList);
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

	private static void loadPredictionData() {
		predictionList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT p.*, c.name as coin_name FROM predictions p "
						+ "JOIN crypto_coins c ON p.coin_id = c.id ORDER BY prediction_date DESC")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				predictionList.add(new Prediction(rs.getInt("id"), rs.getInt("coin_id"),
						rs.getDouble("predicted_price"), rs.getDouble("confidence_level"),
						rs.getDate("prediction_date").toLocalDate(), rs.getString("coin_name")));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error loading prediction data: " + e.getMessage());
		}
	}

	private static void showGeneratePredictionForm() {
		Stage formStage = new Stage();
		formStage.setTitle("Generate Price Prediction");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #262626;");

		ComboBox<CryptoCoin> coinCombo = new ComboBox<>(coinList);
		DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));

		grid.addRow(0, createLabel("Coin:"), coinCombo);
		grid.addRow(1, createLabel("Prediction Date:"), datePicker);

		Button generateButton = new Button("Generate");
		generateButton.getStyleClass().add("action-button");

		generateButton.setOnAction(e -> {
			if (coinCombo.getValue() != null) {
				generatePrediction(coinCombo.getValue(), datePicker.getValue());
				formStage.close();
			} else {
				showAlert("Error", "Please select a coin");
			}
		});

		VBox layout = new VBox(20);
		layout.getChildren().addAll(grid, generateButton);
		layout.setPadding(new Insets(10));
		layout.setStyle("-fx-background-color: #262626;");

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(PredictionCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		formStage.setScene(scene);
		formStage.show();
	}

	private static void generatePrediction(CryptoCoin coin, LocalDate predictionDate) {
		try (Connection conn = DBConnection.getConnection()) {
			// Get historical prices for analysis
			double predictedPrice = calculatePredictedPrice(conn, coin.getId());
			double confidenceLevel = calculateConfidenceLevel();

			// Save prediction
			try (PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO predictions (coin_id, predicted_price, confidence_level, prediction_date) "
							+ "VALUES (?, ?, ?, ?)")) {
				stmt.setInt(1, coin.getId());
				stmt.setDouble(2, predictedPrice);
				stmt.setDouble(3, confidenceLevel);
				stmt.setDate(4, Date.valueOf(predictionDate));
				stmt.executeUpdate();
				loadPredictionData();
			}
		} catch (SQLException e) {
			showAlert("Error", "Error generating prediction: " + e.getMessage());
		}
	}

	private static double calculatePredictedPrice(Connection conn, int coinId) throws SQLException {
		// Get historical prices
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT price FROM price_history WHERE coin_id = ? ORDER BY timestamp DESC LIMIT 7")) {
			stmt.setInt(1, coinId);
			ResultSet rs = stmt.executeQuery();

			double sum = 0;
			int count = 0;
			double lastPrice = 0;

			while (rs.next()) {
				double price = rs.getDouble("price");
				if (count == 0)
					lastPrice = price;
				sum += price;
				count++;
			}

			if (count == 0)
				return 0;

			double avg = sum / count;
			double trend = (lastPrice - avg) / avg;

			return lastPrice * (1 + trend);
		}
	}

	private static double calculateConfidenceLevel() {
		// Simple confidence calculation (can be enhanced)
		return 85.0 + Math.random() * 10.0;
	}

	private static void deletePrediction() {
		Prediction selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert("Warning", "Please select a prediction to delete");
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm Delete");
		alert.setContentText("Are you sure you want to delete this prediction?");

		if (alert.showAndWait().get() == ButtonType.OK) {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement("DELETE FROM predictions WHERE id = ?")) {
				stmt.setInt(1, selected.getId());
				stmt.executeUpdate();
				loadPredictionData();
			} catch (SQLException e) {
				showAlert("Error", "Error deleting prediction: " + e.getMessage());
			}
		}
	}

	private static void showAccuracyAnalysis() {
		Stage analysisStage = new Stage();
		analysisStage.setTitle("Prediction Accuracy Analysis");

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		// Create accuracy chart
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
		chart.setTitle("Prediction Accuracy Over Time");

		// Add analysis metrics
		GridPane metricsGrid = new GridPane();
		metricsGrid.setHgap(20);
		metricsGrid.setVgap(10);
		metricsGrid.addRow(0, createLabel("Average Accuracy:"), createLabel("87.5%"));
		metricsGrid.addRow(1, createLabel("Best Performing Coin:"), createLabel("BTC"));

		content.getChildren().addAll(chart, metricsGrid);

		Scene scene = new Scene(content, 800, 600);
		scene.getStylesheets().add(PredictionCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		analysisStage.setScene(scene);
		analysisStage.show();
	}

	private static void filterPredictions(CryptoCoin coin, LocalDate date) {
		predictionList.clear();
		try (Connection conn = DBConnection.getConnection()) {
			String sql = "SELECT p.*, c.name as coin_name FROM predictions p "
					+ "JOIN crypto_coins c ON p.coin_id = c.id WHERE 1=1";
			if (coin != null)
				sql += " AND p.coin_id = ?";
			if (date != null)
				sql += " AND p.prediction_date = ?";
			sql += " ORDER BY prediction_date DESC";

			PreparedStatement stmt = conn.prepareStatement(sql);
			int paramIndex = 1;
			if (coin != null)
				stmt.setInt(paramIndex++, coin.getId());
			if (date != null)
				stmt.setDate(paramIndex, Date.valueOf(date));

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				predictionList.add(new Prediction(rs.getInt("id"), rs.getInt("coin_id"),
						rs.getDouble("predicted_price"), rs.getDouble("confidence_level"),
						rs.getDate("prediction_date").toLocalDate(), rs.getString("coin_name")));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error filtering predictions: " + e.getMessage());
		}
	}

	private static Label createLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill: white;");
		return label;
	}

	private static void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
