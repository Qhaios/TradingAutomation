package com.acme.mytrader.price.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.monitor.BuyBelowPrice;

@RunWith(MockitoJUnitRunner.class)
public class BuyBelowPriceTest {
	
	private static final String TEST_STOCK_2_MONITOR = "IBM";
	private static final String TEST_OTHER_STOCK_2_MONITOR = "GOOG";
	private static final double TEST_PRICE_2_MONITOR = 55;
	private static final int TEST_VOLUME_2_MONITOR = 55;
	
	@Mock
	private ExecutionService executionService;

	private BuyBelowPrice buyAtPriceListener;
	
	@Before
	public void setUp() {
		buyAtPriceListener = spy(new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService));
	}
    
    /**
     * Test Price Listener Constructor handles malformed arguments
     */
	@Test
    public void testCheckOfNullorNaNListenerCriteria() {
		// Checks if the stock is null, if so should throw NullPointerException
		
		try {
			new BuyBelowPrice(null, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService);
			
			fail("Should have thrown NullPointerException");
		} catch(NullPointerException ex) {
			assertEquals("Security was null", ex.getMessage());
		}
		
		// Checks if the price is not a number, if so should throw ArithmeticException
		
		try {
			new BuyBelowPrice(TEST_STOCK_2_MONITOR, Double.NaN, TEST_VOLUME_2_MONITOR, executionService);
			
			fail("Should have thrown ArithmeticException");
		} catch(ArithmeticException ex) {
			assertEquals("Price is not a number", ex.getMessage());
		}
		
		// Checks if the volume is an negative integer, if so throw return ArithmeticException
		
		try {
			new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, -1, executionService);
			
			fail("Should have thrown ArithmeticException");
		} catch(ArithmeticException ex) {
			assertEquals("Volume shouldn't be less than 0", ex.getMessage());
		}
		
		// Checks if the executionService is null, if so throw return NullPointerException
		
		try {
			new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, -1, executionService);
			
			fail("Should have thrown ArithmeticException");
		} catch(ArithmeticException ex) {
			assertEquals("Volume shouldn't be less than 0", ex.getMessage());
		}
		
		// Checks if when the arguments are value, it throws no exception
    	
		try {
			new BuyBelowPrice(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR, TEST_VOLUME_2_MONITOR, executionService);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
    }
    
    /**
     * Test Price Listener's stock update checker handles every potential argument correctly
     */
	@Test
    public void testInvocationOfCanCheckPriceFor() {
		
		// Checks canCheckPriceFor handles null without throwing an exception
		
		try {
			buyAtPriceListener.canCheckPriceFor(null);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
		
		// Checks canCheckPriceFor handles empty strings without throwing an exception
    	
		try {
			buyAtPriceListener.canCheckPriceFor("");
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
		
		// Checks canCheckPriceFor handles different stock without throwing an exception
    	
		try {
			buyAtPriceListener.canCheckPriceFor(TEST_STOCK_2_MONITOR);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
		
		// Checks canCheckPriceFor handles monitored stock without throwing an exception
    	
		try {
			buyAtPriceListener.canCheckPriceFor(TEST_STOCK_2_MONITOR);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
    }
    
    /**
     * Test Price Listener's buy order executor handles every potential argument correctly
     */
	@Test
    public void testInvocationOfBuyAtPrice() {
		
		// Checks canCheckPriceFor handles null without throwing an exception
		
		try {
			buyAtPriceListener.buyAtPrice(Double.NaN);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
		
		// Checks canCheckPriceFor handles empty strings without throwing an exception
    	
		try {
			buyAtPriceListener.canCheckPriceFor("");
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
		
		// Checks canCheckPriceFor handles different stock without throwing an exception
    	
		try {
			buyAtPriceListener.canCheckPriceFor(TEST_STOCK_2_MONITOR);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
		
		// Checks canCheckPriceFor handles monitored stock without throwing an exception
    	
		try {
			buyAtPriceListener.canCheckPriceFor(TEST_STOCK_2_MONITOR);
		} catch(Exception ex) {
			fail("Shouldn't throw any exceptions");
		}
    }
    
    /**
     * Test Price Listener skips checks if the stock (a.k.a security) or stock price are null
     */
	@Test
    public void testSkipsOnMalformedPriceUpdates() {
		// share price update
    	
    	// Null stock
    	buyAtPriceListener.priceUpdate(null, TEST_PRICE_2_MONITOR -0.1);
    	
    	verify(buyAtPriceListener, times(0)).canCheckPriceFor(any(String.class));
    	
    	// share another price update
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, Double.NaN);
    	
    	verify(buyAtPriceListener, times(0)).canCheckPriceFor(any(String.class));
    }
    
    /**
     * Test Price Listener Monitors Prices for a specific stock
     */
    @Test
    public void testMonitorsPriceUpdates() {
    	// share price update
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -0.1);
    	
    	verify(buyAtPriceListener, times(1)).canCheckPriceFor(TEST_STOCK_2_MONITOR);
    	
    	// share another price update
    	
    	buyAtPriceListener.priceUpdate(TEST_OTHER_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR + 5);
    	
    	verify(buyAtPriceListener, times(1)).canCheckPriceFor(TEST_OTHER_STOCK_2_MONITOR);
    }
    
    /**
     * Test Price Listener triggers buy only one the stock it is monitoring
     */
    @Test
    public void testBuysOnlyForSpecificStock() {
    	// share price update for other stock
    	
    	buyAtPriceListener.priceUpdate(TEST_OTHER_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -0.1);
    	
    	verify(buyAtPriceListener, never()).buyAtPrice(any(Double.class));
    	
    	// share price update
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -0.1);
    	
    	verify(buyAtPriceListener, times(1)).buyAtPrice(any(Double.class));
    }
    
    /**
     * Test Price Listener triggers buy only when the price is below the monitored price
     */
    @Test
    public void testBuysOnlyWhenStockPriceUpdateBelowMonitoredPrice() {
    	// don't buy when update stock price is the same as the monitored one
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR);
    	
    	verify(buyAtPriceListener, never()).buyAtPrice(any(Double.class));
    	
    	// don't buy when update stock price is above the monitored one
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR + 5);
    	
    	verify(buyAtPriceListener, never()).buyAtPrice(any(Double.class));
    	
    	// buy when update stock price is below the monitored one
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -0.1);
    	
    	verify(buyAtPriceListener, times(1)).buyAtPrice(any(Double.class));
    }
    
    /**
     * Test Price Listener only buys a specific set volume at a time (when triggered)
     */
    @Test
    public void testBuysSpecificVolumeWhenTriggered() {
    	// share price update
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -1);
    	
    	verify(executionService, times(1)).buy(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -1, TEST_VOLUME_2_MONITOR);
    	
    	// share price update
    	
    	buyAtPriceListener.priceUpdate(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -0.5);
    	
    	verify(executionService, times(1)).buy(TEST_STOCK_2_MONITOR, TEST_PRICE_2_MONITOR -0.5, TEST_VOLUME_2_MONITOR);
    }
    
   // Skipping test to trigger buy with no ExecutionService provided, as PriceListener's constructor required a valid ExecutioinService
}
