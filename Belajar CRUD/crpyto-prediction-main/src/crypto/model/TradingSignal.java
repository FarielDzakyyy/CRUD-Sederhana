package crypto.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class TradingSignal {
	private IntegerProperty id;
	private IntegerProperty coinId;
	private StringProperty signalType;
	private DoubleProperty strength;
	private ObjectProperty<LocalDateTime> generatedAt;
	private StringProperty coinName;

	public TradingSignal(int id, int coinId, String signalType, double strength, LocalDateTime generatedAt,
			String coinName) {
		this.id = new SimpleIntegerProperty(id);
		this.coinId = new SimpleIntegerProperty(coinId);
		this.signalType = new SimpleStringProperty(signalType);
		this.strength = new SimpleDoubleProperty(strength);
		this.generatedAt = new SimpleObjectProperty<>(generatedAt);
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

	public String getSignalType() {
		return signalType.get();
	}

	public StringProperty signalTypeProperty() {
		return signalType;
	}

	public double getStrength() {
		return strength.get();
	}

	public DoubleProperty strengthProperty() {
		return strength;
	}

	public LocalDateTime getGeneratedAt() {
		return generatedAt.get();
	}

	public ObjectProperty<LocalDateTime> generatedAtProperty() {
		return generatedAt;
	}

	public String getCoinName() {
		return coinName.get();
	}

	public StringProperty coinNameProperty() {
		return coinName;
	}
}
