package crypto.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Prediction {
	private IntegerProperty id;
	private IntegerProperty coinId;
	private DoubleProperty predictedPrice;
	private DoubleProperty confidenceLevel;
	private ObjectProperty<LocalDate> predictionDate;
	private StringProperty coinName;

	public Prediction(int id, int coinId, double predictedPrice, double confidenceLevel, LocalDate predictionDate,
			String coinName) {
		this.id = new SimpleIntegerProperty(id);
		this.coinId = new SimpleIntegerProperty(coinId);
		this.predictedPrice = new SimpleDoubleProperty(predictedPrice);
		this.confidenceLevel = new SimpleDoubleProperty(confidenceLevel);
		this.predictionDate = new SimpleObjectProperty<>(predictionDate);
		this.coinName = new SimpleStringProperty(coinName);
	}

	public int getId() {
		return id.get();
	}

	public IntegerProperty idProperty() {
		return id;
	}

	public int getCoinId() {
		return coinId.get();
	}

	public IntegerProperty coinIdProperty() {
		return coinId;
	}

	public double getPredictedPrice() {
		return predictedPrice.get();
	}

	public DoubleProperty predictedPriceProperty() {
		return predictedPrice;
	}

	public double getConfidenceLevel() {
		return confidenceLevel.get();
	}

	public DoubleProperty confidenceLevelProperty() {
		return confidenceLevel;
	}

	public LocalDate getPredictionDate() {
		return predictionDate.get();
	}

	public ObjectProperty<LocalDate> predictionDateProperty() {
		return predictionDate;
	}

	public String getCoinName() {
		return coinName.get();
	}

	public StringProperty coinNameProperty() {
		return coinName;
	}
}
