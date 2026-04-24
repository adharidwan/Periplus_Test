# Periplus Shopping Cart Test Suite

Automated TestNG + Selenium tests for the Periplus shopping cart flow, built with Maven.

## What is included
- Page-object based UI tests for cart behavior
- Test data and driver setup under `src/main/java/com/periplus`
- Test cases selected through `testng.xml`

## Requirements
- Java 23
- Maven 3.9+ on your `PATH`
- Google Chrome installed
- A registered Periplus test account

## Test Credentials
Set these before running the suite:
- `PERIPLUS_EMAIL`
- `PERIPLUS_PASSWORD`

Optional overrides supported by the code:
- `PERIPLUS_BASE_URL`
- `PERIPLUS_SEARCH_TERM`
- `PERIPLUS_EXPECTED_PRODUCT`
- `PERIPLUS_HEADLESS`
- `PERIPLUS_TIMEOUT_SECONDS`

## Install Guide

### Windows
1. Install Java 23.
   - You can use Temurin, Oracle JDK, or any compatible JDK.
2. Install Maven.
   - If you use Chocolatey:
     `choco install maven`
   - If you use Scoop:
     `scoop install maven`
3. Verify installation:
   ```powershell
   java -version
   mvn -version
   ```
4. Install or update Google Chrome.

### macOS
1. Install Java 23.
   - If you use Homebrew:
     `brew install --cask temurin`
2. Install Maven.
   - If you use Homebrew:
     `brew install maven`
3. Verify installation:
   ```bash
   java -version
   mvn -version
   ```
4. Install or update Google Chrome.

### Linux
1. Install Java 23.
   - Debian/Ubuntu example:
     `sudo apt install openjdk-23-jdk`
   - Fedora example:
     `sudo dnf install java-23-openjdk-devel`
2. Install Maven.
   - Debian/Ubuntu example:
     `sudo apt install maven`
   - Fedora example:
     `sudo dnf install maven`
3. Verify installation:
   ```bash
   java -version
   mvn -version
   ```
4. Install or update Google Chrome.

## Run Guide

### Windows PowerShell
```powershell
$env:PERIPLUS_EMAIL = "your-test-email@example.com"
$env:PERIPLUS_PASSWORD = "your-test-password"
$env:PERIPLUS_SEARCH_TERM = "atomic habits"
$env:PERIPLUS_EXPECTED_PRODUCT = "Atomic Habits"
mvn clean test
```

### macOS and Linux bash/zsh
```bash
export PERIPLUS_EMAIL="your-test-email@example.com"
export PERIPLUS_PASSWORD="your-test-password"
export PERIPLUS_SEARCH_TERM="atomic habits"
export PERIPLUS_EXPECTED_PRODUCT="Atomic Habits"
mvn clean test
```

### Run a single suite
The Maven Surefire plugin is configured to use [testng.xml](testng.xml), so this command runs the cart suite:
```bash
mvn clean test
```

### Run in headless mode
Set the flag before running:
- Windows PowerShell:
  ```powershell
  $env:PERIPLUS_HEADLESS = "true"
  mvn clean test
  ```
- macOS/Linux:
  ```bash
  export PERIPLUS_HEADLESS="true"
  mvn clean test
  ```

### Change timeout or base URL
You can override configuration with system properties:
```bash
mvn clean test -Dperiplus.baseUrl=https://www.periplus.com -Dperiplus.timeoutSeconds=30
```

## Project Structure
| Path | Purpose |
| --- | --- |
| `src/main/java/com/periplus/pages` | Page objects and reusable UI actions |
| `src/main/java/com/periplus/utils` | Driver setup, config, test data, and helpers |
| `src/test/java/com/periplustest` | TestNG test cases |
| `testng.xml` | Test suite and selected test method list |

## Test Cases
| ID | Test Method | Name | Objective | Preconditions | Data | Steps | Expected Result |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TC-SC-001 | `tc01AddFromProductPageQtyTwoShouldAppearAsTwoInCart` | Product detail quantity sync | Confirm the quantity typed before adding a book is carried into the cart | Registered user is authenticated and the cart has been cleared | Search: `atomic habits`<br>Expected title: `Atomic Habits`<br>Quantity: `2` | 1. Sign in<br>2. Clear existing cart items<br>3. Search for the configured book<br>4. Open its detail page<br>5. Enter quantity `2`<br>6. Add it to the cart<br>7. Review the cart quantity field | The same book is listed in the cart with quantity `2` |
| TC-SC-002 | `tc02IncreaseCartQtyShouldIncreaseTotalPrice` | Cart quantity increment | Check that raising an item quantity from the cart changes both quantity and payable amount | Registered user has a clean cart and one added book | Search: `atomic habits`<br>Start quantity: `1`<br>Updated quantity: `2` | 1. Add one copy of the book<br>2. Capture the cart total<br>3. Increase the item quantity<br>4. Submit cart update<br>5. Compare the new total | Quantity is updated to `2` and the cart total is higher than before |
| TC-SC-003 | `tc03EmptyCartShouldNotAllowCheckout` | Checkout hidden for empty cart | Ensure the cart page does not expose checkout when there are no items | Cart has no remaining products | Empty cart | 1. Open shopping cart<br>2. Remove every visible cart item<br>3. Reload the cart page<br>4. Inspect available cart actions | Empty-cart message is shown and checkout action is unavailable |
| TC-SC-004 | `tc04MultipleProductsQtyChangeShouldUpdateTotalPrice` | Bulk cart quantity update | Validate that several cart rows can be updated together and the total responds | Registered user is logged in and cart starts empty | Configured product set from `CartTestData`<br>Target quantity: `2` each | 1. Add each configured product once<br>2. Open the cart<br>3. Set every visible item quantity to `2`<br>4. Submit update<br>5. Verify row quantities and total | Cart has multiple rows, every row shows quantity `2`, and the total increases |
| TC-SC-005 | `tc05QuantityChangeShouldRecalculateTotalCorrectly` | Single-line total recalculation | Verify the cart recalculates after changing one product to a larger quantity | Registered user has one product in cart | Search: `atomic habits`<br>New quantity: `3` | 1. Add one copy<br>2. Record the one-item total<br>3. Change quantity to `3`<br>4. Update cart<br>5. Check quantity and recalculated total | Quantity becomes `3` and the total reflects the larger quantity |
| TC-SC-006 | `tc06ProceedToCheckoutFromCart` | Checkout entry point | Confirm a populated cart can enter the checkout flow | Registered user is logged in and cart contains a product | Product: `Atomic Habits` | 1. Add one product<br>2. Open cart<br>3. Verify checkout control is present<br>4. Click checkout | Browser moves to checkout or to an account-protected checkout page |
| TC-SC-007 | `tc07ExcessiveQuantityShouldNotBeAccepted` | Stock limit guard | Check that an unrealistic quantity cannot be accepted without restriction | Registered user is logged in and cart starts empty | Product: `Atomic Habits`<br>Attempted quantity: `9999` | 1. Open product page<br>2. Enter `9999` as quantity<br>3. Attempt to add to cart<br>4. Inspect warning/cart count | Site blocks, warns, or limits the excessive quantity |

## Test Result
| ID | Name | Expected Result | Actual Result | Status | Comments |
| --- | --- | --- | --- | --- | --- |
| TC-SC-001 | Product detail quantity sync | Cart shows the selected quantity from product detail | Pending execution | Not Run | Automated in `PeriplusCartTest` |
| TC-SC-002 | Cart quantity increment | Quantity and total increase after cart update | Pending execution | Not Run | Automated in `PeriplusCartTest` |
| TC-SC-003 | Checkout hidden for empty cart | Empty cart has no checkout action | Pending execution | Not Run | Automated in `PeriplusCartTest` |
| TC-SC-004 | Bulk cart quantity update | All visible cart rows update to quantity `2` | Pending execution | Not Run | Automated in `PeriplusCartTest` |
| TC-SC-005 | Single-line total recalculation | Total reflects quantity `3` after update | Pending execution | Not Run | Automated in `PeriplusCartTest` |
| TC-SC-006 | Checkout entry point | Cart can enter checkout flow when populated | Pending execution | Not Run | Automated in `PeriplusCartTest` |
| TC-SC-007 | Stock limit guard | Excessive quantity is blocked, warned, or limited | Pending execution | Not Run | Automated in `PeriplusCartTest` |

## Test Selection
Each scenario is listed in `testng.xml` under `<methods>`. To run only selected cases, keep the needed `<include>` rows and comment or remove the others.
