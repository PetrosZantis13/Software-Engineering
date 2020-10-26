package auctionhouse;

import java.util.ArrayList;
import java.util.HashSet;

public class Auction {

    private Money currentBid;
    private String currentBidder;
    // private int auctionID;
    private String auctioneerName;
    private String auctioneerAddress;
    private int lotNumber;
    // private boolean isOpen;
    private Money increment;
    private HashSet<String> usersInvolved;

    public Auction(String auctioneerName, String auctioneerAddress, HashSet<String> usersInvolved, int lotNumber, Money increment) {

        this.auctioneerName = auctioneerName;
        this.auctioneerAddress = auctioneerAddress; 
        this.lotNumber = lotNumber;
        this.increment = increment;
        this.usersInvolved = usersInvolved;

    }

    public Money getCurrentBid() {
        return currentBid;
    }

    public String getCurrentBidder() {
        return currentBidder;
    }

    public int getLotNumber() {
        return lotNumber;
    }

    public String getAuctioneerName() {
        return auctioneerName;
    }
    
    public String getAuctioneerAddress() {
        return auctioneerAddress;
    }

    public Money getIncrement() {
        return increment;
    }

    public HashSet<String> getUsersInvolved() {
        return usersInvolved;
    }

    public void setCurrentBid(Money bid) {
        currentBid = bid;
    }

    public void setCurrentBidder(String bidderName) {
        currentBidder = bidderName;
    }

    public void setIncrement(Money increment) {
        this.increment = increment;
    }

}