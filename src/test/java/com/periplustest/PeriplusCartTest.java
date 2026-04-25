package com.periplustest;

import com.periplus.pages.CartPage;
import com.periplus.pages.HomePage;
import com.periplus.pages.LoginPage;
import com.periplus.pages.ProductPage;
import com.periplus.utils.CartTestData;
import com.periplus.utils.CartTestData.Product;
import com.periplus.utils.DriverFactory;
import com.periplus.utils.TestConfig;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class PeriplusCartTest {
    private WebDriver driver;
    private TestConfig config;

    @BeforeMethod
    public void setUp() {
        config = TestConfig.fromEnvironment();
        driver = DriverFactory.createChromeDriver(config);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void tc01ShouldAddProductWithQtyTwoFromProductPage() {
        loginAndOpenCleanCart();

        String productName = addProduct(CartTestData.ATOMIC_HABITS, 2);

        CartPage cartPage = new CartPage(driver, config).open();
        Assert.assertFalse(cartPage.isEmpty(), "Cart should not be empty after adding a product.");
        Assert.assertTrue(cartPage.containsProduct(productName), "Cart should contain product: " + productName);
        Assert.assertEquals(cartPage.firstItemQuantity(), 2, "Cart quantity should match product-page quantity.");
    }

    @Test
    public void tc02ShouldIncreaseCartTotalWhenQuantityIncreases() {
        loginAndOpenCleanCart();
        addProduct(CartTestData.ATOMIC_HABITS, 1);

        CartPage cartPage = new CartPage(driver, config).open();
        long totalBefore = cartPage.totalPrice();

        cartPage.increaseFirstItemQuantity().update();

        Assert.assertEquals(cartPage.firstItemQuantity(), 2, "First cart item quantity should increase to 2.");
        Assert.assertTrue(cartPage.totalPrice() > totalBefore, "Cart total should increase after quantity is increased.");
    }

    @Test
    public void tc03ShouldBlockCheckoutWhenCartIsEmpty() {
        CartPage cartPage = new CartPage(driver, config).open().removeAllItems().open();

        Assert.assertTrue(cartPage.isEmpty(), "Cart should be empty for this test.");
        Assert.assertFalse(cartPage.canProceedToCheckout(), "Checkout action should not be available for an empty cart.");
    }

    @Test
    public void tc04ShouldUpdateTotalWhenMultipleProductQuantitiesChange() {
        loginAndOpenCleanCart();
        List<Product> products = CartTestData.multipleProducts();

        for (Product product : products) {
            addProduct(product, 1);
        }

        CartPage cartPage = new CartPage(driver, config).open();
        Assert.assertTrue(cartPage.itemRowCount() >= 2, "Cart should contain multiple product rows.");

        long totalBefore = cartPage.totalPrice();
        cartPage.setEachItemQuantity(2).update();

        Assert.assertTrue(cartPage.itemQuantities().stream().allMatch(quantity -> quantity == 2),
                "Every visible cart item should have quantity 2.");
        Assert.assertTrue(cartPage.totalPrice() > totalBefore, "Total should increase after all item quantities are changed.");
    }

    @Test
    public void tc05ShouldRecalculateTotalWhenQuantityChanges() {
        loginAndOpenCleanCart();
        addProduct(CartTestData.ATOMIC_HABITS, 1);

        CartPage cartPage = new CartPage(driver, config).open();
        long oneItemTotal = cartPage.totalPrice();

        cartPage.setFirstItemQuantity(3).update();
        long threeItemTotal = cartPage.totalPrice();

        Assert.assertEquals(cartPage.firstItemQuantity(), 3, "Quantity should update to 3.");
        Assert.assertTrue(threeItemTotal >= oneItemTotal * 3,
                "Total should be recalculated based on the updated quantity.");
    }

    @Test
    public void tc06ShouldNavigateToCheckoutFromNonEmptyCart() {
        loginAndOpenCleanCart();
        addProduct(CartTestData.ATOMIC_HABITS, 1);

        CartPage cartPage = new CartPage(driver, config).open();
        Assert.assertTrue(cartPage.canProceedToCheckout(), "Checkout action should be available when cart has products.");

        cartPage.proceedToCheckout();
        Assert.assertTrue(
            driver.getCurrentUrl().contains("/checkout/checkout")
                || driver.getCurrentUrl().contains("route=checkout/shipping_address")
                || driver.getCurrentUrl().contains("/account/Login"),
                "Proceeding from cart should navigate to checkout or a login-protected checkout route.");
    }

    @Test
    public void tc07ShouldRejectExcessiveProductQuantity() {
        loginAndOpenCleanCart();
        openProduct(CartTestData.ATOMIC_HABITS);

        int excessiveQuantity = 9999;
        ProductPage productPage = new ProductPage(driver, config);
        String quantityAlert = productPage.setLargeQuantityAndCaptureAlert(excessiveQuantity);
        int enteredQuantity = productPage.enteredQuantity();
        productPage.addToCart();
        boolean quantityWarningShown = productPage.showsQuantityWarning();
        String addToCartAlert = productPage.getLastAddToCartAlert();

        CartPage cartPage = new CartPage(driver, config).open();
        int cartCount = cartPage.cartCount();
        int cartQuantity = cartPage.firstItemQuantity();
        boolean notAddedToCart = cartCount == 0;
        boolean qtySanitizedOnProduct = enteredQuantity > 0 && enteredQuantity < excessiveQuantity;
        boolean qtySanitizedInCart = cartCount > 0 && cartQuantity < excessiveQuantity;
        boolean limitAlertShown = containsLimitSignal(quantityAlert) || containsLimitSignal(addToCartAlert);
        boolean rejected = quantityWarningShown || limitAlertShown || qtySanitizedOnProduct || qtySanitizedInCart || notAddedToCart;

        Assert.assertTrue(rejected,
                "Excessive product quantity should be rejected or limited by the cart. "
                        + "warning=" + quantityWarningShown
                        + ", qtyAlert='" + quantityAlert + "'"
                        + ", addAlert='" + addToCartAlert + "'"
                        + ", enteredQty=" + enteredQuantity
                        + ", cartCount=" + cartCount
                        + ", cartQty=" + cartQuantity);
    }

    @Test
    public void tc08ShouldPersistCartAfterLogoutAndLogin() {
        loginAndOpenCleanCart();

        String productName = addProduct(CartTestData.ATOMIC_HABITS, 1);

        CartPage initialCartPage = new CartPage(driver, config).open();
        Assert.assertTrue(initialCartPage.containsProduct(productName),
                "Cart should contain the product before logout.");
        int quantityBeforeLogout = initialCartPage.firstItemQuantity();
        Assert.assertTrue(quantityBeforeLogout >= 1,
                "Product quantity should be at least 1 before logout.");

        // logout then login again.
        driver.get(config.baseUrl() + "/account/Logout");
        LoginPage loginPage = new LoginPage(driver, config)
                .open()
                .login(config.email(), config.password());
        Assert.assertTrue(loginPage.isAuthenticated(),
                "Re-login should restore an authenticated session.");

        CartPage persistedCartPage = new CartPage(driver, config).open();
        Assert.assertTrue(persistedCartPage.containsProduct(productName),
                "Cart should still contain the product after logout and login.");
        int quantityAfterRelogin = persistedCartPage.firstItemQuantity();
        Assert.assertTrue(quantityAfterRelogin >= quantityBeforeLogout,
            "Cart quantity should persist (or increase due to site-side merge) after logout/login. before="
                + quantityBeforeLogout + ", after=" + quantityAfterRelogin);
    }

    private boolean containsLimitSignal(String text) {
        if (isBlank(text)) {
            return false;
        }
        String normalized = text.toLowerCase();
        return normalized.contains("maximum")
                || normalized.contains("max")
                || normalized.contains("not available")
                || normalized.contains("sorry");
    }

    private void loginAndOpenCleanCart() {
        requireConfiguredCredentials();

        LoginPage loginPage = null;
        RuntimeException lastLoginException = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                loginPage = new LoginPage(driver, config)
                        .open()
                        .login(config.email(), config.password());
                break;
            } catch (RuntimeException exception) {
                lastLoginException = exception;
                if (attempt == 2) {
                    throw exception;
                }
                driver.get(config.baseUrl() + "/account/Login");
            }
        }

        String loginFailureDetails = loginPage == null
                ? (lastLoginException == null ? "No login details were captured." : lastLoginException.getMessage())
                : loginPage.errorMessage().orElse("No visible error message was found.");

        Assert.assertTrue(
                loginPage != null && loginPage.isAuthenticated(),
                "Login did not create an authenticated session. " + loginFailureDetails);

        new CartPage(driver, config).open().removeAllItems();
    }

    private String addProduct(Product product, int quantity) {
        String productName = openProduct(product);
        new ProductPage(driver, config)
                .setQuantity(quantity)
                .addToCart();
        return productName;
    }

    private String openProduct(Product product) {
        return new HomePage(driver, config)
                .open()
                .search(product.searchTerm())
                .openProduct(product.expectedTitle());
    }

    private void requireConfiguredCredentials() {
        if (isBlank(config.email()) || isBlank(config.password())) {
            throw new SkipException(
                    "Set PERIPLUS_EMAIL and PERIPLUS_PASSWORD, or -Dperiplus.email and -Dperiplus.password, before running cart tests.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
