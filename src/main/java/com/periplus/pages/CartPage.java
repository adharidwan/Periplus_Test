package com.periplus.pages;

import com.periplus.utils.Money;
import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartPage extends BasePage {
    private static final By UPDATE_BUTTON = By.cssSelector("input[type='submit'][value='Update'], button[title='Update'], button.btn-update, button.btn-cart-update");
    private String lastAlertMessage = "";

    public CartPage(WebDriver driver, TestConfig config) {
        super(driver, config);
    }

    public CartPage open() {
        openPath("/checkout/cart");
        return this;
    }

    public boolean isEmpty() {
        return bodyText().contains("your shopping cart is empty");
    }

    public boolean containsProduct(String productName) {
        return bodyText().contains(productName.toLowerCase(Locale.ROOT));
    }

    public int cartCount() {
        return driver.findElements(By.cssSelector("#cart_total")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(this::integerFromText)
                .orElse(0);
    }

    public int firstItemQuantity() {
        return quantityInputs().stream()
                .findFirst()
                .map(input -> integerFromText(input.getAttribute("value")))
                .orElse(0);
    }

    public List<Integer> itemQuantities() {
        return quantityInputs().stream()
                .map(input -> integerFromText(input.getAttribute("value")))
                .toList();
    }

    public int itemRowCount() {
        return quantityInputs().size();
    }

    public long totalPrice() {
        List<WebElement> totals = driver.findElements(By.cssSelector(".total-amount .right ul li span, span#sub_total"));
        return totals.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .mapToLong(Money::rupiahValue)
                .reduce((first, second) -> second)
                .orElse(0);
    }

    public CartPage setFirstItemQuantity(int quantity) {
        WebElement input = quantityInputs().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No cart quantity input was found."));
        setQuantity(input, quantity);
        return this;
    }

    public CartPage setEachItemQuantity(int quantity) {
        for (WebElement input : new ArrayList<>(quantityInputs())) {
            setQuantity(input, quantity);
        }
        return this;
    }

    public CartPage increaseFirstItemQuantity() {
        WebElement input = quantityInputs().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No cart quantity input was found."));
        setQuantity(input, integerFromText(input.getAttribute("value")) + 1);
        return this;
    }

    public CartPage update() {
        long totalBefore = totalPrice();
        clickUpdate();
        lastAlertMessage = acceptAlertIfPresent();
        if (isBlank(lastAlertMessage)) {
            lastAlertMessage = waitAndAcceptAlert(3);
        }
        waitForPageReady();
        wait.until(driver -> totalPrice() != totalBefore || !quantityInputs().isEmpty() || isEmpty());
        return this;
    }

    public boolean tryUpdateFirstItemQuantity(int quantity) {
        setFirstItemQuantity(quantity);
        update();
        return firstItemQuantity() == quantity;
    }

    public String lastAlertMessage() {
        return lastAlertMessage;
    }

    public CartPage removeAllItems() {
        List<String> removeUrls = driver.findElements(By.cssSelector("a[href*='checkout/cart?remove=']")).stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getAttribute("href"))
                .distinct()
                .toList();

        for (String removeUrl : removeUrls) {
            driver.get(removeUrl);
            waitForPageReady();
        }
        return this;
    }

    public boolean canProceedToCheckout() {
        return driver.findElements(By.cssSelector("a[onclick*='beginCheckout'], a[href*='checkout/checkout']")).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public void proceedToCheckout() {
        WebElement checkout = visible(By.cssSelector("a[onclick*='beginCheckout'], a[href*='checkout/checkout']"));
        scrollIntoView(checkout);
        checkout.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/checkout/checkout"),
                ExpectedConditions.urlContains("route=checkout/shipping_address"),
                ExpectedConditions.urlContains("/account/Login")));
        waitForPageReady();
    }

    private String bodyText() {
        return visible(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
    }

    private List<WebElement> quantityInputs() {
        return driver.findElements(By.cssSelector("input[name^='quantity[']")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private void setQuantity(WebElement input, int quantity) {
        scrollIntoView(input);
        setFieldValue(input, String.valueOf(quantity));
    }

    private void clickUpdate() {
        WebElement updateButton = visible(UPDATE_BUTTON);
        scrollIntoView(updateButton);
        try {
            updateButton.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateButton);
        }
    }

    private int integerFromText(String text) {
        String digits = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }
}
