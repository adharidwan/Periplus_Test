package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Optional;

public class LoginPage extends BasePage {
    public LoginPage(WebDriver driver, TestConfig config) {
        super(driver, config);
    }

    public LoginPage open() {
        openPath("/account/Login");
        return this;
    }

    public LoginPage login(String emailAddress, String passwordText) {
        WebElement email = visible(By.name("email"));
        email.clear();
        email.sendKeys(emailAddress);

        WebElement password = visible(By.name("password"));
        password.clear();
        password.sendKeys(passwordText);

        String loginPageUrl = safeCurrentUrl();
        WebElement loginButton = visible(By.cssSelector("#button-login, input[type='submit'][value='Login']"));
        try {
            loginButton.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
        }

        wait.until(webDriver ->
            hasUrlChangedFrom(loginPageUrl)
                        || elementIsStale(loginButton)
                        || visibleErrorText().isPresent());
        waitForPageReady();

        return this;
    }

    public boolean isAuthenticated() {
        openPath("/account/Your-Account");
        return !currentUrlContains("/account/login");
    }

    public Optional<String> errorMessage() {
        return visibleErrorText();
    }

    private boolean elementIsStale(WebElement element) {
        try {
            element.isEnabled();
            return false;
        } catch (StaleElementReferenceException ignored) {
            return true;
        }
    }

    private String safeCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private boolean hasUrlChangedFrom(String previousUrl) {
        if (isBlank(previousUrl)) {
            return false;
        }

        try {
            String currentUrl = driver.getCurrentUrl();
            return !isBlank(currentUrl) && !currentUrl.equals(previousUrl);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
