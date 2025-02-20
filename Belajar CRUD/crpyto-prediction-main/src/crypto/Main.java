package crypto;

import crypto.crud.CryptoCoinCRUD;
import crypto.crud.PriceHistoryCRUD;
import crypto.crud.PredictionCRUD;
import crypto.crud.TradingSignalCRUD;
import crypto.dashboard.Dashboard;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
	private StackPane contentArea;

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setStyle("-fx-background-color: #1a1a1a;");

		VBox sidebar = createSidebar();
		root.setLeft(sidebar);

		contentArea = new StackPane();
		contentArea.setStyle("-fx-background-color: #262626;");
		root.setCenter(contentArea);

		Scene scene = new Scene(root, 1280, 800);
		scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

		primaryStage.setTitle("Crypto Price Predictor");
		primaryStage.setScene(scene);
		primaryStage.show();

		// Show dashboard by default
		Dashboard.show(contentArea);
	}

	private VBox createSidebar() {
		VBox sidebar = new VBox(10);
		sidebar.setStyle("-fx-background-color: #0d0d0d; -fx-padding: 10;");
		sidebar.setPrefWidth(250);

		Label title = new Label("CRYPTO PREDICTOR");
		title.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 20px; -fx-font-weight: bold;");

		Button btnDashboard = createNavButton("Dashboard", "dashboard");
		Button btnCoins = createNavButton("My Coins", "coins");
		Button btnPrices = createNavButton("Price History", "chart");
		Button btnPredictions = createNavButton("Predictions", "prediction");
		Button btnSignals = createNavButton("Trading Signals", "signals");

		btnDashboard.setOnAction(e -> Dashboard.show(contentArea));
		btnCoins.setOnAction(e -> CryptoCoinCRUD.show(contentArea));
		btnPrices.setOnAction(e -> PriceHistoryCRUD.show(contentArea));
		btnPredictions.setOnAction(e -> PredictionCRUD.show(contentArea));
		btnSignals.setOnAction(e -> TradingSignalCRUD.show(contentArea));

		sidebar.getChildren().addAll(title, new Separator(), btnDashboard, btnCoins, btnPrices, btnPredictions,
				btnSignals);

		return sidebar;
	}

	private Button createNavButton(String text, String iconName) {
		Button btn = new Button(text);
		btn.getStyleClass().add("nav-button");
		btn.setMaxWidth(Double.MAX_VALUE);
		return btn;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
