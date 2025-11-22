package Modules;

import Base.Setup;

import static Pages.Login_Page.LoginButton;
import static Pages.Login_Page.password;
import static Pages.Login_Page.username;
import static Pages.Login_Page.Logoutbutton;
import static Utilities.Actions.click;
import static Utilities.Actions.enter;

/**
 * Login_Module Class - Business Logic Layer for Login Functionality
 * 
 * PURPOSE: This class contains reusable methods for login/logout operations.
 * It acts as a bridge between test classes and page objects.
 * 
 * WHY THIS CLASS EXISTS:
 * - Separates business logic from test code (better organization)
 * - Makes login functionality reusable across multiple test classes
 * - Centralizes login steps (if login flow changes, update one place)
 * - Follows Page Object Model pattern (tests -> modules -> pages -> elements)
 */
public class Login_Module extends Setup {

    /**
     * Performs login operation with provided credentials
     * 
     * WHAT THIS METHOD DOES:
     * 1. Enters username into username field
     * 2. Enters password into password field
     * 3. Clicks the login button
     * 
     * WHY THESE STEPS:
     * - Username first: Standard login flow (username before password)
     * - Password second: Security best practice (password after username)
     * - Click button last: Submits the login form
     * 
     * WHY STATIC: Can be called without creating an instance (convenience)
     * 
     * @param Username the username of the user (from test data)
     * @param Password the password of the user (from test data)
     * 
     * @throws RuntimeException if any step fails (wraps original exception)
     */
    public static void performLogin(String Username, String Password)  {
        try {
            // STEP 1: Enter username into username field
            // "XPATH" = locator type, username = locator value from Login_Page, Username = value to enter
            enter("XPATH", username, Username);
            
            // STEP 2: Enter password into password field
            // "XPATH" = locator type, password = locator value from Login_Page, Password = value to enter
            enter("XPATH", password, Password);
            
            // STEP 3: Click the login button to submit credentials
            // "ACCESSIBILITYID" = locator type, LoginButton = locator value from Login_Page
            click("ACCESSIBILITYID", LoginButton);
            
        } catch (Exception e) {
            // Wrap exception with clear message for better debugging
            // WHY: Original exception might be generic, this adds context
            throw new RuntimeException("Error occurred during login process", e);
        }
    }

    /**
     * Logs out the user from the application
     * 
     * WHAT THIS METHOD DOES:
     * Currently empty - needs implementation based on app's logout flow
     * 
     * WHY EMPTY:
     * - Logout flow varies by app (menu -> logout, button click, etc.)
     * - Placeholder for future implementation
     * 
     * TODO: Implement based on your app's logout mechanism
     * Common patterns:
     * - Click logout button/menu item
     * - Navigate to settings -> logout
     * - Swipe/logout gesture
     * 
     * @throws RuntimeException if logout fails (currently won't throw unless implemented)
     */
    public static void logout() {
        try {
            // TODO: Implement logout functionality
            // Example implementations:
            // click("ACCESSIBILITYID", Logoutbutton);
            // OR: click("XPATH", "//android.widget.Button[@text='Logout']");
            // OR: Navigate to settings and click logout
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during logout process", e);
        }
    }

}
