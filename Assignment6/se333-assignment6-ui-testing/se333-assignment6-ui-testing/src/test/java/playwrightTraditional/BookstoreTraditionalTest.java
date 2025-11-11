package playwrightTraditional;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookstoreTraditionalTest {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private static final String BASE_URL = "https://depaul.bncollege.com/";
    private static final String PRODUCT_NAME =
            "JBL Quantum True Wireless Noise Cancelling Gaming Earbuds- Black";
    private static final String EXPECTED_PRICE = "$149.98";

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("videos/"))
                        .setRecordVideoSize(1280, 720)
        );

        page = context.newPage();
        page.context().clearCookies();
    }

    @AfterAll
    static void teardown() {
        if (page != null && !page.isClosed()) {
            page.close();
        }
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    // 1. Bookstore: search + filters + add to cart
    @Test
    @Order(1)
    @DisplayName("specification-based: Bookstore search and add JBL earbuds to cart")
    void testBookstoreSearchAndAddToCart() {
        page.navigate(BASE_URL);
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Search for earbuds
        Locator searchInput = page.locator("input[placeholder*='Search'], input[id*='search'], input[name*='search']").first();
        assertTrue(searchInput.isVisible(), "Search box should be visible");
        searchInput.fill("earbuds");
        searchInput.press("Enter");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Filters
        clickFilter("Brand", "JBL");
        clickFilter("Color", "Black");
        clickFilter("Price", "Over $50");

        // Click JBL product
        Locator productLink = page.getByText("JBL Quantum True Wireless Noise Cancelling Gaming",
                new Page.GetByTextOptions().setExact(false)).first();
        assertTrue(productLink.isVisible(), "Expected JBL Quantum earbuds link to be visible");
        productLink.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        // Product assertions
        assertTrue(page.getByText(PRODUCT_NAME, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Product name should be visible");
        assertTrue(page.getByText(EXPECTED_PRICE, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Product price should be visible");
        assertTrue(page.getByText("SKU", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "SKU label should be visible");

        // Add to Cart
        Locator addToCart = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add to Cart"));
        if (!addToCart.isVisible()) {
            addToCart = page.getByText("Add to Cart", new Page.GetByTextOptions().setExact(false));
        }
        assertTrue(addToCart.first().isVisible(), "Add to Cart button should be visible");
        addToCart.first().click();

        // Wait for cart update
        page.waitForTimeout(4000);

        // Assert "1 Item(s)" in cart
        Locator cartIndicator = page.getByText("1 Item", new Page.GetByTextOptions().setExact(false)).first();
        assertTrue(cartIndicator.isVisible(), "Cart should show 1 item");
        cartIndicator.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // 2. Cart Page
    @Test
    @Order(2)
    @DisplayName("specification-based: Shopping cart content and pricing")
    void testShoppingCartPage() {
        assertTrue(page.getByText("Your Shopping Cart", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Should be on 'Your Shopping Cart' page");

        assertTrue(page.getByText(PRODUCT_NAME, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Product should be listed in cart");
        assertTrue(page.getByText(EXPECTED_PRICE, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Price should match");

        // Select FAST In-Store Pickup (best-effort selector)
        Locator fastPickup = page.getByText("FAST In-Store Pickup", new Page.GetByTextOptions().setExact(false)).first();
        if (fastPickup.isVisible()) fastPickup.click();

        // Basic sidebar checks
        assertTrue(page.getByText("Subtotal", new Page.GetByTextOptions().setExact(false)).first().isVisible());
        assertTrue(page.getByText("Handling", new Page.GetByTextOptions().setExact(false)).first().isVisible());
        assertTrue(page.getByText("Estimated Total", new Page.GetByTextOptions().setExact(false)).first().isVisible());

        // Promo code attempt (if field exists)
        Locator promo = page.locator("input[placeholder*='Promo'], input[id*='promo'], input[name*='promo']").first();
        if (promo.isVisible()) {
            promo.fill("TEST");
            page.getByText("APPLY", new Page.GetByTextOptions().setExact(false))
                    .first().click();
            page.waitForTimeout(2000);
        }

        // Proceed to checkout
        page.getByText("PROCEED TO CHECKOUT", new Page.GetByTextOptions().setExact(false))
                .first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // 3. Create Account Page
    @Test
    @Order(3)
    @DisplayName("structural-based: Create Account page and Proceed as Guest")
    void testCreateAccountPage() {
        assertTrue(page.getByText("Create Account", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Create Account heading should be visible");
        page.getByText("Proceed as Guest", new Page.GetByTextOptions().setExact(false))
                .first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // 4. Contact Information
    @Test
    @Order(4)
    @DisplayName("specification-based: Fill Contact Information")
    void testContactInformationPage() {
        assertTrue(page.getByText("Contact Information", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Contact Information page should be visible");

        page.fill("input[name*='first']", "TestFirst");
        page.fill("input[name*='last']", "TestLast");
        page.fill("input[type='email']", "test@example.com");
        page.fill("input[type='tel']", "3125550000");

        page.getByText("CONTINUE", new Page.GetByTextOptions().setExact(false))
                .first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // 5. Pickup Information
    @Test
    @Order(5)
    @DisplayName("structural-based: Pickup Information & item summary")
    void testPickupInformationPage() {
        assertTrue(page.getByText("Pickup Information", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Pickup Information should be visible");

        assertTrue(page.getByText("I’ll pick them up", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Pickup person selection should be visible");

        assertTrue(page.getByText(PRODUCT_NAME, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Product should still be listed");
        assertTrue(page.getByText(EXPECTED_PRICE, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Price should still match");

        page.getByText("CONTINUE", new Page.GetByTextOptions().setExact(false))
                .first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // 6. Payment Information
    @Test
    @Order(6)
    @DisplayName("specification-based: Payment Information summary")
    void testPaymentInformationPage() {
        assertTrue(page.getByText("Payment Information", new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Payment Information page should be visible");

        assertTrue(page.getByText(PRODUCT_NAME, new Page.GetByTextOptions().setExact(false))
                .first().isVisible(), "Product listed on Payment page");

        // Navigate back to cart
        Locator backToCart = page.getByText("Back to Cart", new Page.GetByTextOptions().setExact(false)).first();
        if (backToCart.isVisible()) {
            backToCart.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
        }
    }

    // 7. Empty the cart
    @Test
    @Order(7)
    @DisplayName("structural-based: Remove item and verify empty cart")
    void testEmptyCart() {
        Locator remove = page.getByText("Remove", new Page.GetByTextOptions().setExact(false)).first();
        if (remove.isVisible()) {
            remove.click();
            page.waitForTimeout(3000);
        }

        boolean empty =
                page.getByText("Your cart is empty", new Page.GetByTextOptions().setExact(false)).first().isVisible()
                        || page.getByText("no items in your cart", new Page.GetByTextOptions().setExact(false)).first().isVisible();

        assertTrue(empty, "Cart should be empty after removing product");
    }

    // Helper to apply a filter
    private void clickFilter(String filterLabel, String optionLabel) {
        Locator filter = page.getByText(filterLabel, new Page.GetByTextOptions().setExact(false)).first();
        if (filter.isVisible()) {
            filter.click();
            page.waitForTimeout(500);
        }
        Locator option = page.getByText(optionLabel, new Page.GetByTextOptions().setExact(false)).first();
        assertTrue(option.isVisible(), "Filter option not visible: " + optionLabel);
        option.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
