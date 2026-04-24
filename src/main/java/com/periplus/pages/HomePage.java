package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {
    public HomePage(WebDriver driver, TestConfig config) {
        super(driver, config);
    }

    public HomePage open() {
        openPath("/");
        return this;
    }

    public SearchResultsPage search(String keyword) {
        WebElement searchBox = visible(By.cssSelector("input[name='filter_name']"));
        searchBox.clear();
        searchBox.sendKeys(keyword);
        searchBox.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/product/Search"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='/p/']"))));
        waitForPageReady();

        return new SearchResultsPage(driver, config);
    }
}
