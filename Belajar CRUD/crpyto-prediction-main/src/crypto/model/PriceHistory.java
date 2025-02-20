package crypto.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class PriceHistory {
	private IntegerProperty id;
	private IntegerProperty coinId;
	private DoubleProperty price;
	private DoubleProperty volume;
	private ObjectProperty<LocalDateTime> timestamp;

	public PriceHistory(int id, int coinId, double price, double volume, LocalDateTime timestamp) {
		this.id = new SimpleIntegerProperty(id);
		this.coinId = new SimpleIntegerProperty(coinId);
		this.price = new SimpleDoubleProperty(price);
		this.volume = new SimpleDoubleProperty(volume);
		this.timestamp = new SimpleObjectProperty<>(timestamp);
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

	public double getPrice() {
		return price.get();
	}

	public DoubleProperty priceProperty() {
		return price;
	}

	public double getVolume() {
		return volume.get();
	}

	public DoubleProperty volumeProperty() {
		return volume;
	}

	public LocalDateTime getTimestamp() {
		return timestamp.get();
	}

	public ObjectProperty<LocalDateTime> timestampProperty() {
		return timestamp;
	}
}
