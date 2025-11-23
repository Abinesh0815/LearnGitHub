package Tests;

import Utilities.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import static Base.Setup.driver;
import static Modules.Login_Module.logout;
import static Modules.Login_Module.performLogin;

/**
 * LoginTests Class - Test Cases for Login Functionality
 * 
 * PURPOSE: Contains all test methods related to login functionality.
 * This is where actual test scenarios are written and executed.
 * 
 * WHY THIS CLASS:
 * - Separates test code from business logic (Login_Module)
 * - Uses TestNG framework for test execution
 * - Uses DataProvider to run tests with multiple data sets
 */
public class LoginTests
{

    /**
     * Helper method to perform initial click on username field
     * 
     * WHAT THIS DOES:
     * Clicks on the first EditText field (usually username field) to focus it
     * 
     * WHY THIS EXISTS:
     * - Some apps need the field to be focused before entering text
     * - Prepares the UI for login input
     * - Can be used to dismiss any initial popups/overlays
     * 
     * WHY STATIC: Called from test method, static makes it accessible
     * WHY NULL CHECK: Prevents NullPointerException if driver not initialized
     */
    public static void miniLogin(){
        if (driver != null) {
            // Find the first EditText element (username field) using XPath
            // XPath "//android.widget.EditText" finds any EditText in the app
            WebElement username = driver.findElement(By.xpath("//android.widget.EditText"));
            
            // Click to focus the field (may trigger keyboard or prepare for input)
            username.click();
            System.out.println("mini login");
        } else {
            // Throw clear error if driver not ready
            throw new RuntimeException("Driver is not initialized. Cannot perform miniLogin.");
        }
    }
    
    /**
     * Test Case: Verify Login Functionality
     * 
     * WHAT THIS TEST DOES:
     * 1. Performs initial setup (miniLogin - click username field)
     * 2. Checks if test should run (based on Excel data)
     * 3. Performs login with credentials from test data
     * 4. Logs out after login
     * 
     * WHY THESE STEPS:
     * - miniLogin(): Prepares UI for input
     * - checkexecution(): Skips test if marked "NO" in Excel (test control)
     * - performLogin(): Main login operation
     * - logout(): Cleanup - returns app to initial state
     * 
     * @Test annotation: Marks this as a TestNG test method
     * dataProvider = "Testdata": Gets test data from Utils.getData() method
     * dataProviderClass = Utils.class: Specifies where DataProvider is defined
     * 
     * @param data Hashtable containing test data (username, password, Runmode, etc.)
     * @param m Method object (used to get test method name for Excel sheet lookup)
     * 
     * WHY DATA PROVIDER:
     * - Allows running same test with multiple data sets
     * - Test data stored in Excel (easy to maintain)
     * - Each row in Excel = one test execution
     */
    @Test(dataProviderClass = Utils.class, dataProvider = "Testdata")
    public void TC_01_VerifyLogin(Hashtable<String, String> data, Method m) throws InterruptedException, IOException {
        // STEP 1: Initial UI preparation - click username field
        // WHY: Some apps need field focus before input
        miniLogin();
        
        // STEP 2: Check if this test should run
        // WHY: Allows enabling/disabling tests via Excel without code changes
        // m.getName() = "TC_01_VerifyLogin" (used to find row in Excel)
        // data = test data for this execution
        Utils.checkexecution(m.getName(), data);
        
        // STEP 3: Perform login with credentials from test data
        // WHY: Separates test logic from login implementation
        // data.get("username") = gets username value from Excel row
        // data.get("password") = gets password value from Excel row
        performLogin(data.get("username"), data.get("password"));
        
        // STEP 4: Logout to clean up and return to initial state
        // WHY: Ensures test doesn't leave app in logged-in state
        logout();
    }


}
