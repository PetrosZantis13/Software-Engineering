/**
 * 
 */
package auctionhouse;

import java.util.logging.Logger;

/**
 * @author pbj
 */
public class Money implements Comparable<Money> {
 
    private static Logger logger = Logger.getLogger("auctionhouse");
    private static final String LS = System.lineSeparator();
    
    private double value;
    
    private String startBanner(String messageName) {
        return LS + "-------------------------------------------------------------" + LS + "MESSAGE IN: " + messageName
                + LS + "-------------------------------------------------------------";
    }
    
    /**
     * Receives a double value in pounds and returns that value rounded to the nearest pence.
     * 
     * @param pounds    a double value in pounds, which is to be converted to pence
     * @return          the long integer rounded to the nearest pence
     */
    private static long getNearestPence(double pounds) {
        return Math.round(pounds * 100.0);
    }
    
    /**
     * Receives a double value in pounds and uses the previous method before it normalises it.
     * That means, the double value is left with 2 decimal places.
     * 
     * @param pounds    a double value in pounds, which is to be normalised
     * @return          the normalised double value, cut to 2 decimal places
     */
    private static double normalise(double pounds) {
        return getNearestPence(pounds)/100.0;
        
    }
 
    /**
     * Constructor of the Money class. Receives a string and parses it to a double.
     * It then converts the parsed value to a normalised one and stores it in the value 
     * argument.
     * 
     * @param pounds    the string that is passed in the constructor,
     *                  which is transformed into a normalised double
     */
    public Money(String pounds) {
        value = normalise(Double.parseDouble(pounds));
    }
    
    /**
     * Constructor of the Money class. Receives a double and then converts 
     * it to a normalised one and stores it in the value argument.
     * 
     * @param pounds    the double that is passed in the constructor,
     *                  which is stored in the value argument.
     */
    private Money(double pounds) {
        value = pounds;
    }
    
    /**
     * Adds amount m of money to the money object that is called upon.
     * 
     * @param m    money amount that is added to object of class money
     * @return     resulting amount when value of m is added with 
     *             current object's value
     */
    public Money add(Money m) {
        logger.fine(startBanner("add " + m));
        
        return new Money(value + m.value);
    }
    
    /**
     * Subtracts amount m of money from the money object that is called upon.
     * 
     * @param m    money amount that is subtracted from object of class money
     * @return     resulting amount when value of m is subtracted from 
     *             current object's value
     */
    public Money subtract(Money m) {
        logger.fine(startBanner("subtract " + m));
        
        return new Money(value - m.value);
    }
 
    /**
     * The parameter of type double represents a percentage to be added
     * to current object's value argument. Percentage is divided by 100 
     * and is then multiplied with value to find the new value. It can also work as
     * subtractPercent, if the parameter is negative.
     *  
     * @param percent   double value of the percentage to be added to value 
     *                  argument of the object
     * @return          the money result of the addition with the percentage
     */
    public Money addPercent(double percent) {
        logger.fine(startBanner("addPercent " + percent));
        
        return new Money(normalise(value * (1 + percent/100.0)));
    }
     
    @Override
    public String toString() {
        return String.format("%.2f", value);    
    }
    
    /**
     * Compares the money m parameter to money object that is called upon.
     * Their values are compared and returns a negative integer, zero or a positive integer
     * when the object's value is less than, equal to, or greater than the value of m.
     * 
     * @param m    money amount that object is compared to
     * @return     negative integer, zero or a positive integer
     *             when the object's value is less than, equal to, or greater than 
     *             the value of m.
     */
    public int compareTo(Money m) {
        logger.fine(startBanner("compareTo " + m));
        
        return Long.compare(getNearestPence(value),  getNearestPence(m.value)); 
    }
    /**
     * Return whether money object is less than or equal to object m, 
     * using the compareTo method.
     * 
     * @param m    money amount that object is compared to
     * @return     boolean value indicating if object's value is less than or equal to m
     */
    public Boolean lessEqual(Money m) {
        logger.fine(startBanner("lessEqual " + m));
        
        return compareTo(m) <= 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        Money oM = (Money) o;
        return compareTo(oM) == 0;       
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(getNearestPence(value));
    }
      

}