package crypto.crud;

import crypto.DBConnection;
import crypto.model.TradingSignal;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TradingSignalCRUD {
	private static TableView<TradingSignal> table;
	private static ObservableList<TradingSignal> signalList = FXCollections.observableArrayList();
	private static ObservableList<CryptoCoin> coinList = FXCollections.observableArrayList();
	private static final ObservableList<String> SIGNAL_TYPES = FXCollections.observableArrayList("BUY", "SELL", "HOLD");

	public static void show(StackPane contentArea) {
		table = new TableView<>();

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		Label headerLabel = new Label("Trading Signals");
		headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00ff88;");

		TableColumn<TradingSignal, String> colCoin = new TableColumn<>("Coin");
		colCoin.setCellValueFactory(data -> data.getValue().coinNameProperty());
		colCoin.setPrefWidth(100);

		TableColumn<TradingSignal, String> colSignal = new TableColumn<>("Signal");
		colSignal.setCellValueFactory(data -> data.getValue().signalTypeProperty());
		colSignal.setPrefWidth(100);

		TableColumn<TradingSignal, Number> colStrength = new TableColumn<>("Strength (%)");
		colStrength.setCellValueFactory(data -> data.getValue().strengthProperty());
		colStrength.setPrefWidth(100);

		TableColumn<TradingSignal, LocalDateTime> colGenerated = new TableColumn<>("Generated At");
		colGenerated.setCellValueFactory(data -> data.getValue().generatedAtProperty());
		colGenerated.setPrefWidth(200);

		table.getColumns().addAll(colCoin, colSignal, colStrength, colGenerated);
		table.setItems(signalList);
		table.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

		HBox filterBox = new HBox(10);
		ComboBox<CryptoCoin> coinFilter = new ComboBox<>(coinList);
		ComboBox<String> signalTypeFilter = new ComboBox<>(SIGNAL_TYPES);
		Button filterBtn = new Button("Filter");
		filterBox.getChildren().addAll(new Label("Coin:"), coinFilter, new Label("Signal Type:"), signalTypeFilter,
				filterBtn);
		filterBox.setStyle("-fx-text-fill: white;");

		HBox actionBox = new HBox(10);
		Button generateBtn = new Button("Generate Signal");
		Button deleteBtn = new Button("Delete");
		Button analyzeBtn = new Button("Signal Analysis");
		actionBox.getChildren().addAll(generateBtn, deleteBtn, analyzeBtn);

		content.getChildren().addAll(headerLabel, filterBox, table, actionBox);

		generateBtn.setOnAction(e -> showGenerateSignalForm());
		deleteBtn.setOnAction(e -> deleteSignal());
		analyzeBtn.setOnAction(e -> showSignalAnalysis());
		filterBtn.setOnAction(e -> filterSignals(coinFilter.getValue(), signalTypeFilter.getValue()));

		contentArea.getChildren().clear();
		contentArea.getChildren().add(content);

		loadCoinData();
		loadSignalData();
	}

	private static void setupTable() {
		table.getColumns().clear();

		TableColumn<TradingSignal, String> colCoin = new TableColumn<>("Coin");
		colCoin.setCellValueFactory(data -> data.getValue().coinNameProperty());
		colCoin.setPrefWidth(100);

		TableColumn<TradingSignal, String> colSignal = new TableColumn<>("Signal");
		colSignal.setCellValueFactory(data -> data.getValue().signalTypeProperty());
		colSignal.setPrefWidth(100);

		TableColumn<TradingSignal, Number> colStrength = new TableColumn<>("Strength (%)");
		colStrength.setCellValueFactory(data -> data.getValue().strengthProperty());
		colStrength.setPrefWidth(100);

		TableColumn<TradingSignal, LocalDateTime> colGenerated = new TableColumn<>("Generated At");
		colGenerated.setCellValueFactory(data -> data.getValue().generatedAtProperty());
		colGenerated.setPrefWidth(200);

		table.getColumns().addAll(colCoin, colSignal, colStrength, colGenerated);
		table.setItems(signalList);
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

	private static void loadSignalData() {
		signalList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT s.*, c.name as coin_name FROM trading_signals s "
						+ "JOIN crypto_coins c ON s.coin_id = c.id ORDER BY generated_at DESC")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				signalList.add(new TradingSignal(rs.getInt("id"), rs.getInt("coin_id"), rs.getString("signal_type"),
						rs.getDouble("strength"), rs.getTimestamp("generated_at").toLocalDateTime(),
						rs.getString("coin_name")));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error loading signal data: " + e.getMessage());
		}
	}

	private static void showGenerateSignalForm() {
		Stage formStage = new Stage();
		formStage.setTitle("Generate Trading Signal");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #262626;");

		ComboBox<CryptoCoin> coinCombo = new ComboBox<>(coinList);
		ComboBox<String> signalTypeCombo = new ComboBox<>(SIGNAL_TYPES);

		grid.addRow(0, createLabel("Coin:"), coinCombo);
		grid.addRow(1, createLabel("Signal Type:"), signalTypeCombo);

		Button generateButton = new Button("Generate");
		generateButton.getStyleClass().add("action-button");

		generateButton.setOnAction(e -> {
			if (coinCombo.getValue() != null && signalTypeCombo.getValue() != null) {
				generateSignal(coinCombo.getValue(), signalTypeCombo.getValue());
				formStage.close();
			} else {
				showAlert("Error", "Please select both coin and signal type");
			}
		});

		VBox layout = new VBox(20);
		layout.getChildren().addAll(grid, generateButton);
		layout.setPadding(new Insets(10));
		layout.setStyle("-fx-background-color: #262626;");

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(TradingSignalCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		formStage.setScene(scene);
		formStage.show();
	}

	private static void generateSignal(CryptoCoin coin, String signalType) {
		try (Connection conn = DBConnection.getConnection()) {
			double strength = calculateSignalStrength(conn, coin.getId(), signalType);

			try (PreparedStatement stmt = conn
					.prepareStatement("INSERT INTO trading_signals (coin_id, signal_type, strength, generated_at) "
							+ "VALUES (?, ?, ?, ?)")) {
				stmt.setInt(1, coin.getId());
				stmt.setString(2, signalType);
				stmt.setDouble(3, strength);
				stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
				stmt.executeUpdate();
				loadSignalData();
			}
		} catch (SQLException e) {
			showAlert("Error", "Error generating signal: " + e.getMessage());
		}
	}

	private static double calculateSignalStrength(Connection conn, int coinId, String signalType) throws SQLException {
		// Calculate signal strength based on recent price movements
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT price FROM price_history WHERE coin_id = ? ORDER BY timestamp DESC LIMIT 10")) {
			stmt.setInt(1, coinId);
			ResultSet rs = stmt.executeQuery();

			double[] prices = new double[10];
			int i = 0;
			while (rs.next() && i < 10) {
				prices[i++] = rs.getDouble("price");
			}

			if (i == 0)
				return 50.0; // Default strength if no price data

			double recentPrice = prices[0];
			double avgPrice = 0;
			for (int j = 0; j < i; j++) {
				avgPrice += prices[j];
			}
			avgPrice /= i;

			double momentum = (recentPrice - avgPrice) / avgPrice * 100;

			switch (signalType) {
				case "BUY":
					return 50 + Math.min(momentum, 50);
				case "SELL":
					return 50 - Math.max(momentum, -50);
				default:
					return 50.0;
			}
		}
	}

	private static void deleteSignal() {
		TradingSignal selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert("Warning", "Please select a signal to delete");
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm Delete");
		alert.setContentText("Are you sure you want to delete this signal?");

		if (alert.showAndWait().get() == ButtonType.OK) {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement("DELETE FROM trading_signals WHERE id = ?")) {
				stmt.setInt(1, selected.getId());
				stmt.executeUpdate();
				loadSignalData();
			} catch (SQLException e) {
				showAlert("Error", "Error deleting signal: " + e.getMessage());
			}
		}
	}

	private static void showSignalAnalysis() {
		Stage analysisStage = new Stage();
		analysisStage.setTitle("Signal Analysis");

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		// Create signal distribution chart
		PieChart signalDistribution = new PieChart();
		signalDistribution.setTitle("Signal Distribution");

		// Calculate signal distribution
		int buySignals = 0, sellSignals = 0, holdSignals = 0;
		for (TradingSignal signal : signalList) {
			switch (signal.getSignalType()) {
				case "BUY":
					buySignals++;
					break;
				case "SELL":
					sellSignals++;
					break;
				case "HOLD":
					holdSignals++;
					break;
			}
		}

		signalDistribution.getData().addAll(new PieChart.Data("Buy", buySignals),
				new PieChart.Data("Sell", sellSignals), new PieChart.Data("Hold", holdSignals));

		// Add analysis metrics
		GridPane metricsGrid = new GridPane();
		metricsGrid.setHgap(20);
		metricsGrid.setVgap(10);
		metricsGrid.addRow(0, createLabel("Total Signals:"), createLabel(String.valueOf(signalList.size())));
		metricsGrid.addRow(1, createLabel("Average Strength:"),
				createLabel(String.format("%.2f%%", calculateAverageStrength())));

		content.getChildren().addAll(signalDistribution, metricsGrid);

		Scene scene = new Scene(content, 800, 600);
		scene.getStylesheets().add(TradingSignalCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		analysisStage.setScene(scene);
		analysisStage.show();
	}

	private static double calculateAverageStrength() {
		return signalList.stream().mapToDouble(TradingSignal::getStrength).average().orElse(0.0);
	}

	private static void filterSignals(CryptoCoin coin, String signalType) {
		signalList.clear();
		try (Connection conn = DBConnection.getConnection()) {
			String sql = "SELECT s.*, c.name as coin_name FROM trading_signals s "
					+ "JOIN crypto_coins c ON s.coin_id = c.id WHERE 1=1";
			if (coin != null)
				sql += " AND s.coin_id = ?";
			if (signalType != null)
				sql += " AND s.signal_type = ?";
			sql += " ORDER BY generated_at DESC";

			PreparedStatement stmt = conn.prepareStatement(sql);
			int paramIndex = 1;
			if (coin != null)
				stmt.setInt(paramIndex++, coin.getId());
			if (signalType != null)
				stmt.setString(paramIndex, signalType);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				signalList.add(new TradingSignal(rs.getInt("id"), rs.getInt("coin_id"), rs.getString("signal_type"),
						rs.getDouble("strength"), rs.getTimestamp("generated_at").toLocalDateTime(),
						rs.getString("coin_name")));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error filtering signals: " + e.getMessage());
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
