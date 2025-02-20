package crypto.model;

import javafx.beans.property.*;

public class CryptoCoin {
	private IntegerProperty id;
	private StringProperty symbol;
	private StringProperty name;
	private DoubleProperty ownedAmount;
	private DoubleProperty currentValue;

	public CryptoCoin(int id, String symbol, String name, double ownedAmount, double currentValue) {
		this.id = new SimpleIntegerProperty(id);
		this.symbol = new SimpleStringProperty(symbol);
		this.name = new SimpleStringProperty(name);
		this.ownedAmount = new SimpleDoubleProperty(ownedAmount);
		this.currentValue = new SimpleDoubleProperty(currentValue);
	}

	public int getId() {
		return id.get();
	}

	public IntegerProperty idProperty() {
		return id;
	}

	public String getSymbol() {
		return symbol.get();
	}

	public StringProperty symbolProperty() {
		return symbol;
	}

	public String getName() {
		return name.get();
	}

	public StringProperty nameProperty() {
		return name;
	}

	public double getOwnedAmount() {
		return ownedAmount.get();
	}

	public DoubleProperty ownedAmountProperty() {
		return ownedAmount;
	}

	public double getCurrentValue() {
		return currentValue.get();
	}

	public DoubleProperty currentValueProperty() {
		return currentValue;
	}

	@Override
	public String toString() {
		return symbol.get() + " - " + name.get();
	}
}
