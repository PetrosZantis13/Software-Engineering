/**
 * 
 */
package auctionhouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author pbj
 *
 */
public class AuctionHouseImp implements AuctionHouse {

    private static Logger logger = Logger.getLogger("auctionhouse");
    private static final String LS = System.lineSeparator();

    private Parameters parameters;
    private ArrayList<Buyer> registeredBuyers = new ArrayList<>();
    private ArrayList<Seller> registeredSellers = new ArrayList<>();
    private ArrayList<Lot> lots = new ArrayList<>();
    private HashSet<Auction> auctions = new HashSet<>();

    private String startBanner(String messageName) {
        return LS + "-------------------------------------------------------------" + LS + "MESSAGE IN: " + messageName
                + LS + "-------------------------------------------------------------";
    }

    public AuctionHouseImp(Parameters parameters) {
        this.parameters = parameters;
    }

    public Status registerBuyer(String name, String address, String bankAccount, String bankAuthCode) {
        logger.fine(startBanner("registerBuyer " + name));

        Buyer newBuyer = new Buyer(name, address, bankAccount, bankAuthCode);

        logger.fine("Ensuring buyer is a not already registered");
        for (Buyer buyer : registeredBuyers) {
            String bName = buyer.getName();
            if (bName.equals(name)) {
                logger.warning("Buyer already registered");
                return Status.error("Buyer already registered");
            }
        }

        logger.fine("Buyer added to the system");
        registeredBuyers.add(newBuyer);
        return Status.OK();

    }

    public Status registerSeller(String name, String address, String bankAccount) {
        logger.fine(startBanner("registerSeller " + name));

        Seller newSeller = new Seller(name, address, bankAccount);
        logger.fine("Check if seller is already registered");
        for (Seller seller : registeredSellers) {
            String sName = seller.getName();
            if (sName.equals(name)) {
                logger.warning("Seller already registered");
                return Status.error("Seller already registered");
            }
        }
        logger.fine("seller added to sthe sysytem");
        registeredSellers.add(newSeller);
        return Status.OK();
    }

    public Status addLot(String sellerName, int number, String description, Money reservePrice) {
        logger.fine(startBanner("addLot " + sellerName + " " + number));

        Lot newLot = new Lot(sellerName, number, description, reservePrice);

        boolean sellerFound = false;
        logger.fine("Check if seller is exists");
        for (Seller seller : registeredSellers) {
            String sName = seller.getName();
            if (sName.equals(sellerName)) {
                sellerFound = true;
            }
        }
        logger.warning("Seller not found");
        if (!sellerFound) {
            return Status.error("Unregistered Seller");
        }
        logger.fine("Check if lot number is not already used for another lot");
        for (Lot lot : lots) {
            int lNumber = lot.getLotNumber();
            if (lNumber == number) {
                logger.warning("Lot number is already being used");
                return Status.error("Lot number is already used");
            }
        }
        logger.fine("Lot added to the system");
        lots.add(newLot);
        return Status.OK();
    }

    public List<CatalogueEntry> viewCatalogue() {
        logger.fine(startBanner("viewCatalogue"));

        List<CatalogueEntry> catalogue = new ArrayList<CatalogueEntry>();
        logger.fine("Sorted catalogue is created and returned");
        for (Lot lot : lots) {
            int lotNumber = lot.getLotNumber();
            String description = lot.getDescription();
            LotStatus status = lot.getLotStatus();
            if (status.equals(LotStatus.UNSOLD)) {
                catalogue.add(new CatalogueEntry(lotNumber, description, status));
            }
        }

        Collections.sort(catalogue, (a, b) -> (a.lotNumber > b.lotNumber) ? 1 : -1);

        logger.fine("Catalogue: " + catalogue.toString());
        return catalogue;
    }

    public Status noteInterest(String buyerName, int lotNumber) {
        logger.fine(startBanner("noteInterest " + buyerName + " " + lotNumber));

        String messagingAddress = null;

        logger.fine("Check if buyer is registered");
        for (Buyer buyer : registeredBuyers) {
            String bName = buyer.getName();
            if (bName.equals(buyerName)) {
                messagingAddress = buyer.getAddress();
            }
        }
        if (messagingAddress == null) {
            logger.warning("Buyer is not registered");
            return Status.error("Not a registered buyer");
        }
        logger.fine("Find lot required");
        for (Lot lot : lots) {
            int lotID = lot.getLotNumber();
            if (lotID == lotNumber) {
                logger.fine("Note buyer's interest");
                lot.addInterestedBuyer(messagingAddress);
                return Status.OK();
            }
        }
        logger.warning("Lot was not found");
        return Status.error("Non-existent lot");
    }

    public Status openAuction(String auctioneerName, String auctioneerAddress, int lotNumber) {
        logger.fine(startBanner("openAuction " + auctioneerName + " " + lotNumber));

        boolean foundLot = false;
        String sellerName = null;
        String sellerAddress = null;
        HashSet<String> usersInvolved = null;
        MessagingService messagingService = parameters.messagingService;
        Lot currentLot = null;

        logger.fine("Find lot required");
        for (Lot lot : lots) {
            if (lot.getLotNumber() == lotNumber) {
                foundLot = true;
                sellerName = lot.getSellerName();
                usersInvolved = new HashSet<>(lot.getInterestedBuyers());
                currentLot = lot;
            }
        }
        if (foundLot == false) {
            logger.warning("Lot was not found");
            return Status.error("Non-existent lot");
        }

        logger.fine("Check if lot state is UNSOLD");
        if (!currentLot.getLotStatus().equals(LotStatus.UNSOLD)) {
            logger.warning("Lot state is not UNSOLD");
            return Status.error("Lot is not for sale");
        }

        currentLot.setLotStatus(LotStatus.IN_AUCTION);

        for (Seller seller : registeredSellers) {
            if (seller.getName().equals(sellerName)) {
                sellerAddress = seller.getAddress();
            }
        }

        usersInvolved.add(sellerAddress);
        for (String user : usersInvolved) {
            messagingService.auctionOpened(user, lotNumber);
        }
        logger.fine("Messages are sent to interested users and the seller");

        Money bidIncrement = parameters.increment;
        Auction auction = new Auction(auctioneerName, auctioneerAddress, usersInvolved, lotNumber, bidIncrement);
        auctions.add(auction);

        logger.fine("Auction opened");
        return Status.OK();
    }

    public Status makeBid(String buyerName, int lotNumber, Money bid) {
        logger.fine(startBanner("makeBid " + buyerName + " " + lotNumber + " " + bid));

        String buyerAddress = null;
        Auction currentAuction = null;
        Lot currentLot = null;
        ArrayList<String> interested = null;
        HashSet<String> usersInvolved = null;
        MessagingService messagingService = parameters.messagingService;

        for (Auction auction : auctions) {
            int lotID = auction.getLotNumber();
            if (lotID == lotNumber) {
                currentAuction = auction;
                usersInvolved = new HashSet<>(auction.getUsersInvolved());
                usersInvolved.add(currentAuction.getAuctioneerAddress());
            }
        }

        for (Buyer buyer : registeredBuyers) {
            String bname = buyer.getName();
            if (bname.equals(buyerName)) {
                buyerAddress = buyer.getAddress();
                
            }
        }

        for (Lot lot : lots) {
            int lotID = lot.getLotNumber();
            if (lotID == lotNumber) {
                interested = lot.getInterestedBuyers();
                currentLot = lot;
            }
        }

        if (!interested.contains(buyerAddress)) {
            return Status.error("Buyer did not note interest in lot");
        }

        LotStatus lotStatus = currentLot.getLotStatus();
        if (!lotStatus.equals(LotStatus.IN_AUCTION)) {
            return Status.error("Lot is not available for bidding");
        }

        if (!(currentAuction.getCurrentBidder() == null)) {
            Money totalBid = currentAuction.getCurrentBid();
            totalBid.add(parameters.increment);
            if (!totalBid.lessEqual(bid)) {
                return Status.error("Not sufficient bid price");
            }
        }

        for (Auction auction : auctions) {
            if (currentAuction == auction) {
                auction.setCurrentBid(bid);
                auction.setCurrentBidder(buyerName);
            }
        }
        
        usersInvolved.remove(buyerAddress);
        for (String user : usersInvolved) {
            messagingService.bidAccepted(user, lotNumber, bid);
        }
        return Status.OK();
    }

    public Status closeAuction(String auctioneerName, int lotNumber) {
        logger.fine(startBanner("closeAuction " + auctioneerName + " " + lotNumber));

        boolean foundLot = false;
        boolean foundAuction = false;
        LotStatus lotStatus = null;
        Money reservePrice = null;
        Lot theLot = null;
        Money hammerPrice = null;
        String highestBidder = null;
        HashSet<String> usersInvolved = null;
        String sellerBank = null;
        String buyerBank = null;
        String buyerAuthCode = null;
        BankingService bankingService = parameters.bankingService;
        MessagingService messagingService = parameters.messagingService;

        for (Lot lot : lots) {
            if (lot.getLotNumber() == lotNumber) {
                foundLot = true;
                theLot = lot;
                lotStatus = theLot.getLotStatus();
                reservePrice = theLot.getReservePrice();
            }
        }
        if (foundLot == false) {
            return Status.error("Non-existent lot");
        }

        for (Auction auction : auctions) {
            String auctioneer = auction.getAuctioneerName();
            int lotID = auction.getLotNumber();
            if (auctioneer.equals(auctioneerName) && lotID == lotNumber) {
                foundAuction = true;
                hammerPrice = auction.getCurrentBid();
                highestBidder = auction.getCurrentBidder();
                usersInvolved = auction.getUsersInvolved();
            }
        }

        if (!foundAuction) {
            return Status.error("No such current Auction");
        }

        if (!lotStatus.equals(LotStatus.IN_AUCTION)) {
            return Status.error("Lot not in auction");
        }

        if (!reservePrice.lessEqual(hammerPrice)) {
            return (new Status(Status.Kind.NO_SALE));
        }

        String sellerName = theLot.getSellerName();
        for (Seller seller : registeredSellers) {
            if (seller.getName().equals(sellerName)) {
                sellerBank = seller.getBankAccount();
            }
        }

        for (Buyer buyer : registeredBuyers) {
            if (buyer.getName().equals(highestBidder)) {
                buyerBank = buyer.getBankAccount();
                buyerAuthCode = buyer.getBankAuthCode();
            }
        }

        Money buyerAmount = hammerPrice.addPercent(parameters.buyerPremium);
        Money sellerAmount = hammerPrice.addPercent(-parameters.commission);
        Status transfer1 = bankingService.transfer(buyerBank, buyerAuthCode, parameters.houseBankAccount, buyerAmount);
        Status transfer2 = bankingService.transfer(parameters.houseBankAccount, parameters.houseBankAuthCode,
                sellerBank, sellerAmount);
        if (transfer1.kind == Status.Kind.ERROR || transfer2.kind == Status.Kind.ERROR ) {
            theLot.setLotStatus(LotStatus.SOLD_PENDING_PAYMENT);
            for (String user : usersInvolved) {
                messagingService.lotUnsold(user, lotNumber);
            }
            return (new Status(Status.Kind.SALE_PENDING_PAYMENT));
        }
        for (String user : usersInvolved) {
            messagingService.lotSold(user, lotNumber);
        }
        theLot.setLotStatus(LotStatus.SOLD);
        return (new Status(Status.Kind.SALE));
    }
} 
