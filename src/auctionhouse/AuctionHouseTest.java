/**
 * 
 */
package auctionhouse;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class AuctionHouseTest {

    private static final double BUYER_PREMIUM = 10.0;
    private static final double COMMISSION = 15.0;
    private static final Money INCREMENT = new Money("10.00");
    private static final String HOUSE_ACCOUNT = "AH A/C";
    private static final String HOUSE_AUTH_CODE = "AH-auth";

    private AuctionHouse house;
    private MockMessagingService messagingService;
    private MockBankingService bankingService;

    /*
     * Utility methods to help shorten test text.
     */
    private static void assertOK(Status status) {
        assertEquals(Status.Kind.OK, status.kind);
    }

    private static void assertError(Status status) {
        assertEquals(Status.Kind.ERROR, status.kind);
    }

    private static void assertSale(Status status) {
        assertEquals(Status.Kind.SALE, status.kind);
    }

    private static void assertNotSale(Status status) {
        assertEquals(Status.Kind.NO_SALE, status.kind);
    }

    private static void assertPendingSale(Status status) {
        assertEquals(Status.Kind.SALE_PENDING_PAYMENT, status.kind);
    }

    /*
     * Logging functionality
     */

    // Convenience field. Saves on getLogger() calls when logger object needed.
    private static Logger logger;

    // Update this field to limit logging.
    public static Level loggingLevel = Level.ALL;

    private static final String LS = System.lineSeparator();

    @BeforeClass
    public static void setupLogger() {

        logger = Logger.getLogger("auctionhouse");
        logger.setLevel(loggingLevel);

        // Ensure the root handler passes on all messages at loggingLevel and above
        // (i.e. more severe)
        Logger rootLogger = Logger.getLogger("");
        Handler handler = rootLogger.getHandlers()[0];
        handler.setLevel(loggingLevel);
    }

    private String makeBanner(String testCaseName) {
        return LS + "#############################################################" + LS + "TESTCASE: " + testCaseName
                + LS + "#############################################################";
    }

    @Before
    public void setup() {
        messagingService = new MockMessagingService();
        bankingService = new MockBankingService();

        house = new AuctionHouseImp(new Parameters(BUYER_PREMIUM, COMMISSION, INCREMENT, HOUSE_ACCOUNT, HOUSE_AUTH_CODE,
                messagingService, bankingService));

    }

    /*
     * Setup story running through all the test cases.
     * 
     * Story end point is made controllable so that tests can check story prefixes
     * and branch off in different ways.
     */
    private void runStory(int endPoint) {
        assertOK(house.registerSeller("SellerY", "@SellerY", "SY A/C"));
        assertOK(house.registerSeller("SellerZ", "@SellerZ", "SZ A/C"));
        if (endPoint == 1)
            return;

        assertOK(house.addLot("SellerY", 2, "Painting", new Money("200.00")));
        assertOK(house.addLot("SellerY", 1, "Bicycle", new Money("80.00")));
        assertOK(house.addLot("SellerZ", 5, "Table", new Money("100.00")));
        if (endPoint == 2)
            return;

        assertOK(house.registerBuyer("BuyerA", "@BuyerA", "BA A/C", "BA-auth"));
        assertOK(house.registerBuyer("BuyerB", "@BuyerB", "BB A/C", "BB-auth"));
        assertOK(house.registerBuyer("BuyerC", "@BuyerC", "BC A/C", "BC-auth"));
        if (endPoint == 3)
            return;

        assertOK(house.noteInterest("BuyerA", 1));
        assertOK(house.noteInterest("BuyerA", 5));
        assertOK(house.noteInterest("BuyerB", 1));
        assertOK(house.noteInterest("BuyerB", 2));
        if (endPoint == 4)
            return;

        assertOK(house.openAuction("Auctioneer1", "@Auctioneer1", 1));

        messagingService.expectAuctionOpened("@BuyerA", 1);
        messagingService.expectAuctionOpened("@BuyerB", 1);
        messagingService.expectAuctionOpened("@SellerY", 1);
        messagingService.verify();
        if (endPoint == 5)
            return;

        Money m70 = new Money("70.00");
        assertOK(house.makeBid("BuyerA", 1, m70));

        messagingService.expectBidReceived("@BuyerB", 1, m70);
        messagingService.expectBidReceived("@Auctioneer1", 1, m70);
        messagingService.expectBidReceived("@SellerY", 1, m70);
        messagingService.verify();
        if (endPoint == 6)
            return;

        Money m100 = new Money("100.00");
        assertOK(house.makeBid("BuyerB", 1, m100));

        messagingService.expectBidReceived("@BuyerA", 1, m100);
        messagingService.expectBidReceived("@Auctioneer1", 1, m100);
        messagingService.expectBidReceived("@SellerY", 1, m100);
        messagingService.verify();
        if (endPoint == 7)
            return;

        assertSale(house.closeAuction("Auctioneer1", 1));
        messagingService.expectLotSold("@BuyerA", 1);
        messagingService.expectLotSold("@BuyerB", 1);
        messagingService.expectLotSold("@SellerY", 1);
        messagingService.verify();

        bankingService.expectTransfer("BB A/C", "BB-auth", "AH A/C", new Money("110.00"));
        bankingService.expectTransfer("AH A/C", "AH-auth", "SY A/C", new Money("85.00"));
        bankingService.verify();
        if (endPoint == 8)
            return;

        assertError(house.makeBid("BuyerA", 2, m70));
        if (endPoint == 9)
            return;

        assertError(house.openAuction("Auctioneer1", "@Auctioneer1", 1));
        if (endPoint == 10)
            return;

        assertError(house.openAuction("Auctioneer1", "@Auctioneer1", 7));
        if (endPoint == 11)
            return;

        house.openAuction("Auctioneer1", "@Auctioneer1", 5);
        Money m10 = new Money("10.00");
        assertOK(house.makeBid("BuyerA", 5, m10));
        if (endPoint == 12)
            return;

        assertError(house.makeBid("BuyerA", 1, m10));
        if (endPoint == 13)
            return;

        assertOK(house.makeBid("BuyerA", 5, m70));
        assertError(house.makeBid("BuyerA", 5, m10));
        if (endPoint == 14)
            return;

        assertError(house.noteInterest("BuyerB", 7));
        if (endPoint == 15)
            return;

        assertError(house.addLot("SellerX", 2, "Painting", new Money("200.00")));
        if (endPoint == 16)
            return;

        assertOK(house.addLot("SellerZ", 3, "Chair", new Money("90.00")));
        assertOK(house.noteInterest("BuyerA", 3));
        assertOK(house.openAuction("Auctioneer1", "@Auctioneer1", 3));
        assertOK(house.makeBid("BuyerA", 3, new Money("20.00")));
        assertNotSale(house.closeAuction("Auctioneer1", 3));
        if (endPoint == 17)
            return;

        assertOK(house.registerBuyer("BuyerD", "@BuyerD", "BD A/C", "BD-auth"));
        bankingService.setBadAccount("BD A/C");
        assertOK(house.addLot("SellerZ", 4, "Phone", new Money("110.00")));
        assertOK(house.noteInterest("BuyerD", 4));
        assertOK(house.openAuction("Auctioneer1", "@Auctioneer1", 4));
        assertOK(house.makeBid("BuyerD", 4, new Money("111.00")));
        assertPendingSale(house.closeAuction("Auctioneer1", 4));
    }

    @Test
    public void testEmptyCatalogue() {
        logger.info(makeBanner("emptyLotStore"));

        List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);

    }

    @Test
    public void testRegisterSeller() {
        logger.info(makeBanner("testRegisterSeller"));
        runStory(1);
    }

    @Test
    public void testRegisterSellerDuplicateNames() {
        logger.info(makeBanner("testRegisterSellerDuplicateNames"));
        runStory(1);
        assertError(house.registerSeller("SellerY", "@SellerZ", "SZ A/C"));
    }

    @Test
    public void testAddLot() {
        logger.info(makeBanner("testAddLot"));
        runStory(2);
    }

    @Test
    public void testViewCatalogue() {
        logger.info(makeBanner("testViewCatalogue"));
        runStory(2);

        List<CatalogueEntry> expectedCatalogue = new ArrayList<CatalogueEntry>();
        expectedCatalogue.add(new CatalogueEntry(1, "Bicycle", LotStatus.UNSOLD));
        expectedCatalogue.add(new CatalogueEntry(2, "Painting", LotStatus.UNSOLD));
        expectedCatalogue.add(new CatalogueEntry(5, "Table", LotStatus.UNSOLD));

        List<CatalogueEntry> actualCatalogue = house.viewCatalogue();

        assertEquals(expectedCatalogue, actualCatalogue);
    }

    @Test
    public void testRegisterBuyer() {
        logger.info(makeBanner("testRegisterBuyer"));
        runStory(3);
    }

    @Test
    public void testNoteInterest() {
        logger.info(makeBanner("testNoteInterest"));
        runStory(4);
    }

    @Test
    public void testOpenAuction() {
        logger.info(makeBanner("testOpenAuction"));
        runStory(5);
    }

    @Test
    public void testMakeBid() {
        logger.info(makeBanner("testMakeBid"));
        runStory(7);
    }

    @Test
    public void testCloseAuctionWithSale() {
        logger.info(makeBanner("testCloseAuctionWithSale"));
        runStory(8);
    }

    @Test
    public void testRegisterBuyerDuplicateNames() {
        logger.info(makeBanner("testRegisterBuyerDuplicateNames"));
        runStory(3);
        assertError(house.registerBuyer("BuyerA", "@BuyerA", "BA A/C", "BA-auth"));
    }

    @Test
    public void testMakeBidNotInterestedBuyer() {
        logger.info(makeBanner("testMakeBidNotInterestedBuyer"));
        runStory(9);
    }

    @Test
    public void testOpenAuctionWrongLotStatus() {
        logger.info(makeBanner("testOpenAuctionWrongLotStatus"));
        runStory(10);
    }

    @Test
    public void testOpenAuctionNonExistingLot() {
        logger.info(makeBanner("testOpenAuctionNonExistsingLot"));
        runStory(11);
    }

    @Test
    public void testMakeFirstBid() {
        logger.info(makeBanner("testMakeFirstBid"));
        runStory(12);
    }

    @Test
    public void testMakeBidWrongLotStatus() {
        logger.info(makeBanner("testMakeBidWrongLotStatus"));
        runStory(13);
    }

    @Test
    public void testMakeUnsuccessfullBid() {
        logger.info(makeBanner("testMakeUnsuccessfullBid"));
        runStory(14);
    }

    @Test
    public void testNoteInterestNonExistingLot() {
        logger.info(makeBanner("testNoteInterestNonExistingLot"));
        runStory(15);
    }

    @Test
    public void testAddExistingLot() {
        logger.info(makeBanner("testAddExistingLot"));
        runStory(16);
    }

    @Test
    public void testCloseAuctionWithoutSale() {
        logger.info(makeBanner("testCloseAuctionWithoutSale"));
        runStory(17);
    }

    @Test
    public void testPendingPayment() {
        logger.info(makeBanner("testPendingPayment"));
        runStory(18);
    }

}