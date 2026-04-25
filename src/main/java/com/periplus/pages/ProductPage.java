package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

public class ProductPage extends BasePage {
    private static final By ADD_TO_CART_BUTTON = By.cssSelector("button.btn-add-to-cart[onclick*='willAddtoCart'], button.btn-add-to-cart");
    private static final By QUANTITY_INPUT = By.cssSelector("input[id^='qty_'], input[name^='qty_'], input[name='quantity']");
    private static final By PLUS_BUTTON = By.cssSelector("button[data-type='plus'][name^='plus['], button[data-type='plus'].btn-number");
    private static final By PRODUCT_ID_INPUT = By.cssSelector("input[name='product_id']");
    private static final By CART_BADGE = By.cssSelector("#cart_total, #cart_total_mobile, #cart-total span, .cart-total");

    private String lastAddToCartAlert = "";

    public ProductPage(WebDriver driver, TestConfig config) {
        super(driver, config);
    }

    public ProductPage setQuantity(int quantity) {
        WebElement quantityInput = visible(QUANTITY_INPUT);
        scrollIntoView(quantityInput);
        setFieldValue(quantityInput, String.valueOf(quantity));
        return this;
    }

    public ProductPage addToCart() {
        lastAddToCartAlert = "";
        int cartCountBefore = cartCount();

        WebElement addToCart = visible(ADD_TO_CART_BUTTON);
        scrollIntoView(addToCart);
        try {
            addToCart.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCart);
        }

        lastAddToCartAlert = acceptAlertIfPresent();
        if (isBlank(lastAddToCartAlert)) {
            lastAddToCartAlert = waitAndAcceptAlert(3);
        }

        if (isBlank(lastAddToCartAlert) && cartCount() <= cartCountBefore) {
            callWillAddToCartFallback();
            String fallbackAlert = waitAndAcceptAlert(3);
            if (!isBlank(fallbackAlert)) {
                lastAddToCartAlert = fallbackAlert;
            }
        }

        waitForCartCountIncrease(cartCountBefore, 8);

        wait.until(driver -> successMessageVisible()
                || showsQuantityWarning()
                || cartCount() > cartCountBefore
                || !isBlank(lastAddToCartAlert));
        return this;
    }

    public String getLastAddToCartAlert() {
        return lastAddToCartAlert;
    }

    public String setLargeQuantityAndCaptureAlert(int quantity) {
        setQuantity(quantity);
        String alert = acceptAlertIfPresent();
        if (!isBlank(alert)) {
            return alert;
        }

        // Some products show limit alerts only after interaction with plus control.
        List<WebElement> plusButtons = driver.findElements(PLUS_BUTTON);
        if (!plusButtons.isEmpty() && plusButtons.get(0).isDisplayed()) {
            WebElement plus = plusButtons.get(0);
            try {
                plus.click();
            } catch (RuntimeException ignored) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", plus);
            }
        }

        return waitAndAcceptAlert(3);
    }

    public int enteredQuantity() {
        return driver.findElements(QUANTITY_INPUT).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(input -> parseInteger(input.getAttribute("value")))
                .orElse(0);
    }

    public boolean showsQuantityWarning() {
        String body = visible(By.tagName("body")).getText().toLowerCase();
        return body.contains("desired qty is not available")
                || body.contains("qty is not available")
                || body.contains("quantity is not available");
    }

    private boolean successMessageVisible() {
        String successXpath = "//*[contains(translate(normalize-space(.), "
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'success add to cart')]";

        return driver.findElements(By.xpath(successXpath)).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    private int cartCount() {
        return driver.findElements(CART_BADGE).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(this::parseInteger)
                .orElse(0);
    }

    private void callWillAddToCartFallback() {
        String productId = driver.findElements(PRODUCT_ID_INPUT).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(element -> element.getAttribute("value"))
                .orElse("");
        if (isBlank(productId)) {
            return;
        }
        ((JavascriptExecutor) driver).executeScript(
                "if (typeof willAddtoCart === 'function') { willAddtoCart(arguments[0]); }",
                productId);
    }

    private void waitForCartCountIncrease(int before, int timeoutSeconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(d -> cartCount() > before);
        } catch (RuntimeException ignored) {
            // Keep going with other success signals (success text, warning, alert).
        }
    }

    private int parseInteger(String rawValue) {
        String digits = rawValue == null ? "" : rawValue.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }

}
