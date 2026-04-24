package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductPage extends BasePage {
    public ProductPage(WebDriver driver, TestConfig config) {
        super(driver, config);
    }

    public ProductPage setQuantity(int quantity) {
        WebElement quantityInput = visible(By.cssSelector("input[id^='qty_'], input[name^='qty_'], input[name='quantity']"));
        scrollIntoView(quantityInput);
        setFieldValue(quantityInput, String.valueOf(quantity));
        return this;
    }

    public ProductPage addToCart() {
        int cartCountBefore = cartCount();

        WebElement addToCart = visible(By.cssSelector("button.btn-add-to-cart"));
        scrollIntoView(addToCart);
        addToCart.click();

        wait.until(driver -> successMessageVisible() || showsQuantityWarning() || cartCount() > cartCountBefore);
        return this;
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
        return driver.findElements(By.cssSelector("#cart_total")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(this::parseInteger)
                .orElse(0);
    }

    private int parseInteger(String rawValue) {
        String digits = rawValue == null ? "" : rawValue.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }

}
