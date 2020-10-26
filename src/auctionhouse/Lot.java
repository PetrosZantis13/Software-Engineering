package auctionhouse;

import java.util.ArrayList;

public class Lot {

    private String sellerName;
    private int lotNumber;
    private String description;
    private Money reservePrice;
    private LotStatus status;
    private ArrayList<String> interestedBuyers;
    
    public Lot (String sellerName, 
            int lotNumber, 
            String description,
            Money reservePrice) {
        	   		
    	    this.sellerName = sellerName;
    	    this.description = description;
    	    this.lotNumber = lotNumber;
    	    this.reservePrice = reservePrice;
    	    status = LotStatus.UNSOLD ;
    	    interestedBuyers = new ArrayList<>();
    }
	
    public String getSellerName() {
        return sellerName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLotNumber() {
        return lotNumber;
    }
    
    public Money getReservePrice() {
        return reservePrice;
    }
    
    public LotStatus getLotStatus() {
        return status;
    }
    
    public void setLotStatus(LotStatus status) {
        this.status = status;
    }
    
    public ArrayList<String> getInterestedBuyers() {
    	return interestedBuyers;
    }
    
    public void addInterestedBuyer(String buyerAddress) {
        interestedBuyers.add(buyerAddress);
    }
}