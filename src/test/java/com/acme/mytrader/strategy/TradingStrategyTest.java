package com.acme.mytrader.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceListener;
import com.acme.mytrader.price.PriceSource;
import com.acme.mytrader.price.monitor.BuyBelowPrice;

/**
 * this class will be used for 'You need to listen to price updates from PriceSource and act accordingly'
 */
@RunWith(MockitoJUnitRunner.class)
public class TradingStrategyTest {

	private static final String TEST_STOCK_2_MONITOR = "IBM";
	private static final double TEST_PRICE_2_MONITOR = 55;
	private static final String TEST_OTHER_STOCK_2_MONITOR = "GOOG";
	private static final double TEST_OTHER_PRICE_2_MONITOR = 23;
	private static final int TEST_VOLUME_2_MONITOR = 55;

	/**
	 * third party implementation
	 */
	@Mock
	private PriceSource priceSource;

	/**
	 * third party implementation
	 */
	@Mock
	private ExecutionService executionService;

	/**
	 * Using List&lt;PriceListener&gt; instead of Map&lt;String, List&lt;PriceListener&gt;&gt;
	 * to traverse every PriceListner with little effort 
	 */
	private List<PriceListener> priceSourcePriceListeners;

	@Before
	public void setUp() {

		// mock third implementation of PriceSource
		priceSourcePriceListeners = new ArrayList<>();

		// mock price listener insertion
		doAnswer(invocation -> {
			Object arg0 = invocation.getArgument(0);

			// arg0 is always PriceListener 

			priceSourcePriceListeners.add((PriceListener) arg0);
			return null;
		}).when(priceSource).addPriceListener(any(PriceListener.class));
	}

	// skipping add and remove PriceListener tests for PriceSource as it could number of implementation

	// skipping test of executionService.buy(...) throwing runtime exception

	// All subsequence tests will only be about handling price updates from PriceSource

	/**
	 * Test malformed stock price update from PriceSource is handled without throwing exception
	 */
	@Test
	public void testPriceSourceSendingMalformedPriceUpdate() {
		BuyBelowPrice buyAtPriceListener = spy(new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService));
		BuyBelowPrice buyAtPriceListener2 = spy(new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService));
		BuyBelowPrice buyAtPriceListener3 = spy(new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService));

		priceSource.addPriceListener(buyAtPriceListener);
		priceSource.addPriceListener(buyAtPriceListener2);
		priceSource.addPriceListener(buyAtPriceListener3);

		List<PriceListener> listeners = priceSourcePriceListeners;

		// share IBM price update of 54

		listeners.forEach(listener -> listener.priceUpdate(null, TEST_PRICE_2_MONITOR -0.1));

		verify(buyAtPriceListener, times(0)).getSecurity(); // Confirms canCheckPriceFor wasn't invoked

		// share GOOG price update of 28

		listeners.forEach(listener -> listener.priceUpdate(TEST_STOCK_2_MONITOR, Double.NaN));

		verify(buyAtPriceListener, times(0)).getSecurity(); // Confirms canCheckPriceFor wasn't invoked
	}

	/**
	 * Test Price Listener triggers buy only one the stock it is monitoring
	 */
	@Test
	public void testBuysVolumeWhenMonitoredStockDropsBelowPrice() {
		BuyBelowPrice buyAtPriceListener = spy(new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService));
		BuyBelowPrice otherBuyAtPriceListener = spy(new BuyBelowPrice(TEST_OTHER_STOCK_2_MONITOR, TEST_OTHER_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService));

		priceSource.addPriceListener(buyAtPriceListener);
		priceSource.addPriceListener(otherBuyAtPriceListener);

		List<PriceListener> listeners = priceSourcePriceListeners;

		// share IBM price update of 54

		listeners.forEach(listener -> listener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -1));

		verify(executionService, times(1)).buy(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -1, TEST_VOLUME_2_MONITOR);

		// share GOOG price update of 28

		listeners.forEach(listener -> listener.priceUpdate(TEST_OTHER_STOCK_2_MONITOR, TEST_OTHER_PRICE_2_MONITOR + 5));

		verify(executionService, times(0)).buy(TEST_OTHER_STOCK_2_MONITOR, TEST_OTHER_PRICE_2_MONITOR + 5, TEST_VOLUME_2_MONITOR);

		// share IBM price update of 55

		listeners.forEach(listener -> listener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR));

		verify(executionService, times(0)).buy(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR);

		// share GOOG price update of 22

		listeners.forEach(listener -> listener.priceUpdate(TEST_OTHER_STOCK_2_MONITOR, TEST_OTHER_PRICE_2_MONITOR -1));

		verify(executionService, times(1)).buy(TEST_OTHER_STOCK_2_MONITOR, TEST_OTHER_PRICE_2_MONITOR -1, TEST_VOLUME_2_MONITOR);
	}
}
