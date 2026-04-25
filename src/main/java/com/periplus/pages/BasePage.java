package com.periplus.pages;

import com.periplus.utils.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final TestConfig config;

    BasePage(WebDriver driver, TestConfig config) {
        this.driver = driver;
        this.config = config;
        this.wait = new WebDriverWait(driver, config.timeout());
    }

    protected void openPath(String path) {
        driver.get(config.baseUrl() + path);
        waitForPageReady();
    }

    protected void waitForPageReady() {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")
                .equals("complete"));

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".preloader")));
        } catch (RuntimeException ignored) {
            hideKnownOverlays();
        }
    }

    protected WebElement visible(By locator) {
        return wait.until(driver -> driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible element found for " + locator)));
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    protected void setFieldValue(WebElement element, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                        + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                element,
                value);
        dismissAlertIfPresent();
    }

    protected Optional<String> visibleErrorText() {
        List<WebElement> errors = driver.findElements(By.cssSelector(".warning, .alert-danger, .error"));
        return errors.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> !isBlank(text))
                .findFirst();
    }

    protected boolean currentUrlContains(String expectedUrlPart) {
        return driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedUrlPart.toLowerCase(Locale.ROOT));
    }

    protected static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    protected static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        String[] parts = value.split("'");
        StringBuilder literal = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[i]).append("'");
        }
        literal.append(")");
        return literal.toString();
    }

    private void hideKnownOverlays() {
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('.preloader, .modal-backdrop').forEach(function(element) { element.style.display = 'none'; });");
    }

    protected void dismissAlertIfPresent() {
        acceptAlertIfPresent();
    }

    protected String acceptAlertIfPresent() {
        try {
            Alert alert = driver.switchTo().alert();
            String text = alert.getText();
            alert.accept();
            return text == null ? "" : text.trim();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    protected String waitAndAcceptAlert(int timeoutSeconds) {
        try {
            Alert alert = new WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.alertIsPresent());
            String text = alert.getText();
            alert.accept();
            return text == null ? "" : text.trim();
        } catch (TimeoutException ignored) {
            return "";
        } catch (RuntimeException ignored) {
            return "";
        }
    }
}
