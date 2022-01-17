package com.acme.mytrader.price.monitor;

import java.util.Objects;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceListener;

/**
 * A Listener for buying a set number of lots when 
 * stock price goes below the specified price. 
 */
public class BuyBelowPrice implements PriceListener {
	
	private String security;
	private double price;
	private int volume;
	
	private ExecutionService executionService;
	
	/**
	 * Setting the criteria to trigger and execute to buy order
	 * 
	 * @param security is the stock to monitor
	 * @param price is the amount to monitor
	 * @param volume is the set number of lots to buy
	 * @param executionService service that handles the buy orders
	 */
	public BuyBelowPrice(String security, double price, int volume, ExecutionService executionService) {
		Objects.requireNonNull(executionService, "ExecutionService was null");
		Objects.requireNonNull(security, "Security was null");
		requireNonNaN(price, "Price is not a number");
		requireUnsignedNumber(volume, "Volume shouldn't be less than 0");
		
		// volume can't be null or NaN
		
		this.security = security;
		this.price = price;
		this.volume = volume;
		this.executionService = executionService;
	}
	
	@Override
	public void priceUpdate(String security, double price) {
		// Checks if 'security' is not null 
		if (security != null && !Double.isNaN(price)) {
			// Checks if price update is for the 'security' 
			// this listener is listening for
			if (canCheckPriceFor(security)) {
				// Checks if price update is below the
				// price being listened for 
				if (price < getPrice()) {
					buyAtPrice(price);
				}
			}
		}
	}
	
	/**
	 * Checks if the received price update is for a stock it is monitoring
	 * 
	 * @param security
	 * @return
	 */
	protected boolean canCheckPriceFor(String security) {
		return Objects.nonNull(security) && Objects.equals(getSecurity(), security);
	}
	
	/**
	 * Throws ArithmeticException if value is not a number
	 * 
	 * @param value
	 * @param errorMessage
	 */
	private static void requireNonNaN(double value, String errorMessage) {
		if (Double.isNaN(value)) {
			throw new ArithmeticException(errorMessage);
		}
	}
	/**
	 * Throws ArithmeticException if value is negative integer
	 * 
	 * @param value
	 * @param errorMessage
	 */
	private static void requireUnsignedNumber(int value, String errorMessage) {
		if (value < 0) {
			throw new ArithmeticException(errorMessage);
		}
	}
	
	/**
	 * Triggers buy other in ExecutionService
	 * 
	 * @param value
	 * @param errorMessage
	 */
	protected void buyAtPrice(double price) {
		this.executionService.buy(getSecurity(), price, getVolume());
	}

	public String getSecurity() {
		return security;
	}

	public double getPrice() {
		return price;
	}

	public int getVolume() {
		return volume;
	}
}
