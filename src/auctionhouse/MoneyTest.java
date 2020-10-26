/**
 * 
 */
package auctionhouse;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class MoneyTest {

    @Test
    public void testAdd() {
        Money val1 = new Money("12.34");
        Money val2 = new Money("0.66");
        Money result = val1.add(val2);
        assertEquals("13.00", result.toString());
    }

    /*
     ***********************************************************************
     * BEGIN MODIFICATION AREA
     ***********************************************************************
     * Add all your JUnit tests for the Money class below.
     */

    @Test
    public void testSubtract() {
        Money val1 = new Money("12.34");
        Money val2 = new Money("0.34");
        Money result = val1.subtract(val2);
        assertEquals("12.00", result.toString());
    }

    @Test
    public void testAddPercent() {
        Money val1 = new Money("12.00");
        double percent = 1.0;
        double percent2 = 200;
        Money result = val1.addPercent(percent);
        Money result2 = val1.addPercent(percent2);
        assertEquals("12.12", result.toString());
        assertEquals("36.00", result2.toString());
    }

    @Test
    public void testCompareTo() {
        Money val1 = new Money("12.45");
        Money val2 = new Money("12.55");
        int result1 = val1.compareTo(val2);
        int result2 = val2.compareTo(val1);
        int result3 = val1.compareTo(val1);
        assertEquals("-1", Integer.toString(result1));
        assertEquals("1", Integer.toString(result2));
        assertEquals("0", Integer.toString(result3));
    }

    @Test
    public void testLessEqual() {
        Money val1 = new Money("2.00");
        Money val2 = new Money("3.00");
        Money val3 = new Money("4.00");
        boolean result1 = val2.lessEqual(val3);
        boolean result2 = val2.lessEqual(val1);
        boolean result3 = val2.lessEqual(val2);
        assertTrue(result1);
        assertFalse(result2);
        assertTrue(result3);
    }

    @Test
    public void testEquals() {
        Money val1 = new Money("2.00");
        Money val2 = new Money("3.00");
        Money val3 = new Money("3.00");
        boolean result1 = val2.equals(val1);
        boolean result2 = val2.equals(val3);
        boolean result3 = val2.equals("3.00");
        assertFalse(result1);
        assertTrue(result2);
        assertFalse(result3);
    }

    /*
     * Put all class modifications above.
     ***********************************************************************
     * END MODIFICATION AREA
     ***********************************************************************
     */

}
