package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
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

        String loginPageUrl = driver.getCurrentUrl();
        WebElement loginButton = visible(By.cssSelector("#button-login, input[type='submit'][value='Login']"));
        loginButton.click();

        wait.until(webDriver ->
                !webDriver.getCurrentUrl().equals(loginPageUrl)
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
}
