package Utilities;

import Base.Setup;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static Utilities.Utils.sourceExists;

public class Actions extends Setup {

    public static WebDriver wait;
    public static WebElement element;


    /**
     * Enters text into a mobile app element
     * 
     * WHAT THIS METHOD DOES:
     * 1. Validates the locator is not empty
     * 2. Finds the element using specified locator type
     * 3. Clicks the element (focuses it)
     * 4. Clears any existing text
     * 5. Enters the new text
     * 
     * WHY THIS METHOD EXISTS:
     * - Provides unified way to enter text regardless of locator type
     * - Handles all locator types (ID, XPATH, etc.) in one place
     * - Reduces code duplication (don't repeat findElement + sendKeys everywhere)
     * - Centralizes error handling
     * 
     * WHY THESE STEPS (click -> clear -> sendKeys):
     * - Click: Ensures element is focused (some apps need this)
     * - Clear: Removes old text (prevents appending to existing text)
     * - sendKeys: Enters new text
     * 
     * @param attributeName the locator type ("ID", "XPATH", "ACCESSIBILITYID", etc.)
     * @param attributeValue the actual locator value (e.g., "//android.widget.EditText")
     * @param inputText the text to enter into the element
     * 
     * @throws IllegalArgumentException if locator is null or empty
     * @throws Exception if element not found or interaction fails
     */
    public static void enter(String attributeName, String attributeValue, String inputText) {

        try {
            // VALIDATION: Check if locator value is provided
            // WHY: Prevents cryptic errors later - fail fast with clear message
            if (attributeValue == null || attributeValue.trim().isEmpty()) {
                throw new IllegalArgumentException("Attribute value cannot be null or empty for enter method");
            }
            
            // Convert locator type to uppercase for case-insensitive matching
            // WHY: "xpath", "XPATH", "Xpath" all work the same way
            String AN = attributeName.toUpperCase();
            // Switch statement: Selects appropriate locator strategy based on attributeName
            // WHY SWITCH: Cleaner than if-else chain, easier to add new locator types
            switch (AN) {
                // ID locator: Find element by its resource-id (Android) or id attribute
                // WHY: Fast and reliable if element has unique ID
                case "ID" -> {
                    driver.findElement(By.id(attributeValue)).click();      // Focus element
                    driver.findElement(By.id(attributeValue)).clear();      // Clear existing text
                    driver.findElement(By.id(attributeValue)).sendKeys(inputText);  // Enter new text
                }
                
                // NAME locator: Find element by name attribute (rarely used in mobile)
                case "NAME" -> {
                    driver.findElement(By.name(attributeValue)).click();
                    driver.findElement(By.name(attributeValue)).clear();
                    driver.findElement(By.name(attributeValue)).sendKeys(inputText);
                }
                
                // XPATH locator: Find element using XPath expression (most flexible)
                // WHY: Can find elements by hierarchy, attributes, text, etc.
                // Example: "//android.widget.EditText[@index='0']"
                case "XPATH" -> {
                    driver.findElement(By.xpath(attributeValue)).click();
                    driver.findElement(By.xpath(attributeValue)).clear();
                    driver.findElement(By.xpath(attributeValue)).sendKeys(inputText);
                }
                
                // CLASSNAME locator: Find element by its class name
                // WHY: Useful when multiple elements share same class
                case "CLASSNAME" -> {
                    driver.findElement(By.className(attributeValue)).click();
                    driver.findElement(By.className(attributeValue)).clear();
                    driver.findElement(By.className(attributeValue)).sendKeys(inputText);
                }
                
                // CSSSELECTOR: CSS selector (mainly for web, rarely mobile)
                case "CSSSELECTOR" -> {
                    driver.findElement(By.cssSelector(attributeValue)).click();
                    driver.findElement(By.cssSelector(attributeValue)).clear();
                    driver.findElement(By.cssSelector(attributeValue)).sendKeys(inputText);
                }
                
                // TAGNAME: Find element by its tag/type name
                case "TAGNAME" -> {
                    driver.findElement(By.tagName(attributeValue)).click();
                    driver.findElement(By.tagName(attributeValue)).clear();
                    driver.findElement(By.tagName(attributeValue)).sendKeys(inputText);
                }
                
                // ACCESSIBILITYID: Find element by content-desc or accessibility label (RECOMMENDED for mobile)
                // WHY: Most reliable for mobile apps, doesn't break with UI changes
                // Example: "loginButton" (content-desc of button)
                case "ACCESSIBILITYID" -> {
                    driver.findElement(AppiumBy.accessibilityId(attributeValue)).click();
                    driver.findElement(AppiumBy.accessibilityId(attributeValue)).clear();
                    driver.findElement(AppiumBy.accessibilityId(attributeValue)).sendKeys(inputText);
                }
                
                // Default case: Invalid locator type provided
                default -> System.out.println("Invalid attribute name specified: " + attributeName + attributeValue);
            }
        }catch (Exception e) {
            // Error handling: Log error details for debugging
            // WHY: Helps identify what went wrong (element not found, timeout, etc.)
            System.out.println("Error in enter method: " + e.getMessage());
            e.printStackTrace();  // Print full stack trace
        }
    }


    /**
     * Clicks on a mobile app element
     * 
     * WHAT THIS METHOD DOES:
     * 1. Validates the locator is not empty
     * 2. Finds the element using specified locator type
     * 3. Clicks the element
     * 
     * WHY THIS METHOD EXISTS:
     * - Unified way to click elements regardless of locator type
     * - Reduces code duplication
     * - Centralizes error handling
     * 
     * @param attributeName the locator type ("ID", "XPATH", "ACCESSIBILITYID", etc.)
     * @param attributeValue the actual locator value
     * 
     * @throws IllegalArgumentException if locator is null or empty
     * @throws Exception if element not found or click fails
     */
    public static void click(String attributeName, String attributeValue) {
        try {
            // VALIDATION: Ensure locator value is provided
            if (attributeValue == null || attributeValue.trim().isEmpty()) {
                throw new IllegalArgumentException("Attribute value cannot be null or empty for click method");
            }
            
            // Convert to uppercase for case-insensitive matching
            String AN = attributeName.toUpperCase();
            
            // Switch: Select appropriate locator strategy and click element
            switch (AN) {
                case "ID" -> driver.findElement(By.id(attributeValue)).click();
                case "NAME" -> driver.findElement(By.name(attributeValue)).click();
                case "XPATH" -> driver.findElement(By.xpath(attributeValue)).click();
                case "CLASSNAME" -> driver.findElement(By.className(attributeValue)).click();
                case "CSSSELECTOR" -> driver.findElement(By.cssSelector(attributeValue)).click();
                case "TAGNAME" -> driver.findElement(By.tagName(attributeValue)).click();
                case "ACCESSIBILITYID" ->  driver.findElement(AppiumBy.accessibilityId(attributeValue)).click();
            }
        }catch (Exception e) {
            // Log error for debugging
            System.out.println("Error in click method: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Waits for a specific element to be visible on the web page.
     *
     * @param attributeName the attribute name to locate the element (e.g., "ID", "XPATH", "CLASSNAME", "CSSSELECTOR", "ACCESSIBILITYID")
     * @param attributeValue the value of the attribute to locate the element
     * @param seconds the maximum number of seconds to wait for the element to be visible
     * @throws Exception if an error occurs while waiting for the element to be visible
     */
    public static void webdriverWait(String attributeName, String attributeValue, int seconds) {
        try {
            String AN = attributeName.toUpperCase();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
            switch (AN) {
                case "ID" -> {
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(attributeValue)));
                }
                case "XPATH" -> {
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(attributeValue)));
                }
                case "CLASSNAME" -> {
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(attributeValue)));
                }
                case "CSSSELECTOR" -> {
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(attributeValue)));
                }
                case "ACCESSIBILITYID"-> {
                    element = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId(attributeValue)));
                }
                default -> System.out.println("Invalid attribute name specified: " + attributeName + attributeValue);
            }
        } catch (Exception e) {
            System.out.println("Error in webdriverWait method: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if an element with the specified attribute is displayed on the page.
     *
     * @param attributeName the attribute name of the element to be checked (e.g., "ID", "NAME", "XPATH", "CLASSNAME", "CSSSELECTOR", "TAGNAME", "ACCESSIBILITYID")
     * @param attributeValue the value of the attribute of the element to be checked
     * @return true if the element is displayed, false otherwise
     * @throws NoSuchElementException if the element with the specified attribute is not found
     */
    public static boolean isElementDisplayed(String attributeName, String attributeValue) {

        String AN = attributeName.toUpperCase();
        boolean isDisplayed = false;

        try {
            switch (AN) {
                case "ID":
                    isDisplayed = driver.findElement(By.id(attributeValue)).isDisplayed();
                    break;

                case "NAME":
                    isDisplayed = driver.findElement(By.name(attributeValue)).isDisplayed();
                    break;

                case "XPATH":
                    isDisplayed = driver.findElement(By.xpath(attributeValue)).isDisplayed();
                    break;

                case "CLASSNAME":
                    isDisplayed = driver.findElement(By.className(attributeValue)).isDisplayed();
                    break;

                case "CSSSELECTOR":
                    isDisplayed = driver.findElement(By.cssSelector(attributeValue)).isDisplayed();
                    break;

                case "TAGNAME":
                    isDisplayed = driver.findElement(By.tagName(attributeValue)).isDisplayed();
                    break;

                case "ACCESSIBILITYID":
                    isDisplayed = driver.findElement(AppiumBy.accessibilityId(attributeValue)).isDisplayed();
                    break;
            }

        } catch (NoSuchElementException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        }

        return isDisplayed;
    }


    /**
     * Waits for a message to be displayed.
     *
     * @param msg the message to wait for
     * @return true if the message is displayed, false otherwise
     */

    public static boolean waitForMessage(String msg) {
        boolean displayed = sourceExists(msg);
        try {
            while (!displayed) {
                Thread.sleep(1000); // Wait for 1 second before checking again
                displayed = sourceExists(msg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupted status
        }
        return displayed;
    }

}
