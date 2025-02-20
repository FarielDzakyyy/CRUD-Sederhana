package crypto.crud;

import crypto.DBConnection;
import crypto.model.CryptoCoin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class CryptoCoinCRUD {
	private static TableView<CryptoCoin> table;
	private static ObservableList<CryptoCoin> coinList = FXCollections.observableArrayList();

	public static void show(StackPane contentArea) {
		table = new TableView<>();

		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setStyle("-fx-background-color: #262626;");

		Label headerLabel = new Label("My Crypto Portfolio");
		headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00ff88;");

		TableColumn<CryptoCoin, String> colSymbol = new TableColumn<>("Symbol");
		colSymbol.setCellValueFactory(data -> data.getValue().symbolProperty());
		colSymbol.setPrefWidth(100);

		TableColumn<CryptoCoin, String> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(data -> data.getValue().nameProperty());
		colName.setPrefWidth(200);

		TableColumn<CryptoCoin, Number> colAmount = new TableColumn<>("Owned Amount");
		colAmount.setCellValueFactory(data -> data.getValue().ownedAmountProperty());
		colAmount.setPrefWidth(150);

		TableColumn<CryptoCoin, Number> colValue = new TableColumn<>("Current Value ($)");
		colValue.setCellValueFactory(data -> data.getValue().currentValueProperty());
		colValue.setPrefWidth(150);

		table.getColumns().addAll(colSymbol, colName, colAmount, colValue);
		table.setItems(coinList);
		table.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

		HBox actionBox = new HBox(10);
		Button addBtn = new Button("Add Coin");
		Button editBtn = new Button("Edit");
		Button deleteBtn = new Button("Delete");

		actionBox.getChildren().addAll(addBtn, editBtn, deleteBtn);

		content.getChildren().addAll(headerLabel, table, actionBox);

		addBtn.setOnAction(e -> showAddForm());
		editBtn.setOnAction(e -> showEditForm());
		deleteBtn.setOnAction(e -> deleteCoin());

		contentArea.getChildren().clear();
		contentArea.getChildren().add(content);

		loadData();
	}

	private static void setupTable() {
		table.getColumns().clear();

		TableColumn<CryptoCoin, String> colSymbol = new TableColumn<>("Symbol");
		colSymbol.setCellValueFactory(data -> data.getValue().symbolProperty());
		colSymbol.setPrefWidth(100);

		TableColumn<CryptoCoin, String> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(data -> data.getValue().nameProperty());
		colName.setPrefWidth(200);

		TableColumn<CryptoCoin, Number> colAmount = new TableColumn<>("Owned Amount");
		colAmount.setCellValueFactory(data -> data.getValue().ownedAmountProperty());
		colAmount.setPrefWidth(150);

		TableColumn<CryptoCoin, Number> colValue = new TableColumn<>("Current Value ($)");
		colValue.setCellValueFactory(data -> data.getValue().currentValueProperty());
		colValue.setPrefWidth(150);

		table.getColumns().addAll(colSymbol, colName, colAmount, colValue);
		table.setItems(coinList);

		// Set table style
		table.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
	}

	private static void loadData() {
		coinList.clear();
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM crypto_coins")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				coinList.add(new CryptoCoin(rs.getInt("id"), rs.getString("symbol"), rs.getString("name"),
						rs.getDouble("owned_amount"), rs.getDouble("current_value")));
			}
		} catch (SQLException e) {
			showAlert("Error", "Error loading data: " + e.getMessage());
		}
	}

	private static void showAddForm() {
		Stage formStage = new Stage();
		formStage.setTitle("Add New Coin");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #262626;");

		TextField symbolField = new TextField();
		TextField nameField = new TextField();
		TextField amountField = new TextField();
		TextField valueField = new TextField();

		styleFormField(symbolField);
		styleFormField(nameField);
		styleFormField(amountField);
		styleFormField(valueField);

		grid.addRow(0, createLabel("Symbol:"), symbolField);
		grid.addRow(1, createLabel("Name:"), nameField);
		grid.addRow(2, createLabel("Amount:"), amountField);
		grid.addRow(3, createLabel("Value ($):"), valueField);

		Button saveButton = new Button("Save");
		saveButton.getStyleClass().add("action-button");

		saveButton.setOnAction(e -> {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(
							"INSERT INTO crypto_coins (symbol, name, owned_amount, current_value) VALUES (?, ?, ?, ?)")) {
				stmt.setString(1, symbolField.getText());
				stmt.setString(2, nameField.getText());
				stmt.setDouble(3, Double.parseDouble(amountField.getText()));
				stmt.setDouble(4, Double.parseDouble(valueField.getText()));
				stmt.executeUpdate();
				loadData();
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
		scene.getStylesheets().add(CryptoCoinCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

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
		CryptoCoin selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert("Warning", "Please select a coin to edit");
			return;
		}

		Stage formStage = new Stage();
		formStage.setTitle("Edit Coin");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #262626;");

		TextField symbolField = new TextField(selected.getSymbol());
		TextField nameField = new TextField(selected.getName());
		TextField amountField = new TextField(String.valueOf(selected.getOwnedAmount()));
		TextField valueField = new TextField(String.valueOf(selected.getCurrentValue()));

		styleFormField(symbolField);
		styleFormField(nameField);
		styleFormField(amountField);
		styleFormField(valueField);

		grid.addRow(0, createLabel("Symbol:"), symbolField);
		grid.addRow(1, createLabel("Name:"), nameField);
		grid.addRow(2, createLabel("Amount:"), amountField);
		grid.addRow(3, createLabel("Value ($):"), valueField);

		Button saveButton = new Button("Update");
		saveButton.getStyleClass().add("action-button");

		saveButton.setOnAction(e -> {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement(
							"UPDATE crypto_coins SET symbol=?, name=?, owned_amount=?, current_value=? WHERE id=?")) {
				stmt.setString(1, symbolField.getText());
				stmt.setString(2, nameField.getText());
				stmt.setDouble(3, Double.parseDouble(amountField.getText()));
				stmt.setDouble(4, Double.parseDouble(valueField.getText()));
				stmt.setInt(5, selected.getId());
				stmt.executeUpdate();
				loadData();
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
		scene.getStylesheets().add(CryptoCoinCRUD.class.getResource("/styles/dark-theme.css").toExternalForm());

		formStage.setScene(scene);
		formStage.show();
	}

	private static void deleteCoin() {
		CryptoCoin selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert("Warning", "Please select a coin to delete");
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm Delete");
		alert.setContentText("Are you sure you want to delete this coin?");

		if (alert.showAndWait().get() == ButtonType.OK) {
			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn.prepareStatement("DELETE FROM crypto_coins WHERE id = ?")) {
				stmt.setInt(1, selected.getId());
				stmt.executeUpdate();
				loadData();
			} catch (SQLException e) {
				showAlert("Error", "Error deleting data: " + e.getMessage());
			}
		}
	}

	private static void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
