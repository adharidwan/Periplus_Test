package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Locale;
import java.util.Optional;

public class SearchResultsPage extends BasePage {
    public SearchResultsPage(WebDriver driver, TestConfig config) {
        super(driver, config);
    }

    public String openProduct(String expectedProduct) {
        WebElement productLink = findExpectedProductLink(expectedProduct)
                .orElseGet(() -> visible(By.cssSelector(".product-content h3 a[href*='/p/'], a[href*='/p/']")));
        String productName = textOrExpectedProduct(productLink, expectedProduct);

        scrollIntoView(productLink);
        productLink.click();

        wait.until(webDriver -> currentUrlContains("/p/"));
        waitForPageReady();

        return productName;
    }

    private Optional<WebElement> findExpectedProductLink(String expectedProduct) {
        String productXpath = "//h3/a[contains(translate(normalize-space(.), "
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), "
                + xpathLiteral(expectedProduct.toLowerCase(Locale.ROOT)) + ")]";

        return driver.findElements(By.xpath(productXpath)).stream()
                .filter(WebElement::isDisplayed)
                .findFirst();
    }

    private String textOrExpectedProduct(WebElement productLink, String expectedProduct) {
        String text = productLink.getText();
        if (isBlank(text)) {
            return expectedProduct;
        }
        return text.trim();
    }
}
