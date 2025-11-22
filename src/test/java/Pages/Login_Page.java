package Pages;

import Base.Setup;

/**
 * Login_Page Class - Page Object Model (POM) for Login Page
 * 
 * PURPOSE: This class stores all element locators for the Login page.
 * It follows the Page Object Model design pattern.
 * 
 * WHAT IS PAGE OBJECT MODEL (POM):
 * - Separates element locators from test logic
 * - Each page/screen has its own class with element locators
 * - Tests use these locators instead of hardcoding them
 * 
 * WHY THIS CLASS EXISTS:
 * - Centralizes all login page locators in one place
 * - If UI changes, update locators here (not in multiple test files)
 * - Makes tests more readable (Login_Page.username vs "//android.widget.EditText")
 * - Reusable across multiple test classes
 * - Easier maintenance (one place to update)
 * 
 * HOW IT WORKS:
 * - This class extends Setup (inherits driver and other utilities)
 * - Each field stores a locator (XPath, ID, Accessibility ID, etc.)
 * - Test classes import these locators and use them via Actions class
 * 
 * LOCATOR TYPES USED:
 * - XPath: "//android.widget.EditText" (finds elements by XML path)
 * - Accessibility ID: Button content-desc (recommended for mobile)
 * 
 * BEST PRACTICES:
 * - Use Accessibility ID when possible (most reliable for mobile)
 * - XPath should be last resort (slower, more fragile)
 * - Keep locators simple and specific
 * - Update locators if app UI changes
 * 
 * IMPORTANT NOTES:
 * - Some locators are empty (password, LoginButton, Logoutbutton)
 * - These MUST be filled with correct locators for tests to work
 * - Empty locators will cause runtime errors
 */
public class Login_Page extends Setup
{
    /**
     * Username field locator
     * 
     * WHAT THIS IS:
     * - XPath locator to find the username input field
     * - Currently finds first EditText element in the app
     * 
     * LOCATOR TYPE: XPath
     * LOCATOR VALUE: "//android.widget.EditText"
     * 
     * HOW IT WORKS:
     * - "//" = search anywhere in the XML hierarchy
     * - "android.widget.EditText" = find EditText widget type
     * - This finds the FIRST EditText element (usually username field)
     * 
     * USED IN: Login_Module.performLogin() method
     * 
     * NOTE: This is a generic locator. If app has multiple EditText fields,
     * consider making it more specific (e.g., by index, content-desc, etc.)
     * Example: "//android.widget.EditText[@index='0']" or use Accessibility ID
     */
    public static String username = "//android.widget.EditText";
    
    /**
     * Password field locator
     * 
     * WHAT THIS IS:
     * - Locator to find the password input field
     * - Currently EMPTY - needs to be filled
     * 
     * STATUS: ❌ EMPTY - MUST BE FILLED
     * 
     * WHY EMPTY:
     * - Password field locator not yet identified
     * - Needs to be found using Appium Inspector or UI Automator Viewer
     * 
     * HOW TO FILL:
     * 1. Open Appium Inspector or UI Automator Viewer
     * 2. Navigate to login page
     * 3. Find password field element
     * 4. Get its locator (prefer Accessibility ID, then XPath)
     * 5. Update this field with the locator
     * 
     * RECOMMENDED LOCATOR TYPES (in order):
     * 1. Accessibility ID: "passwordField" (if content-desc exists)
     * 2. Resource ID: "com.app.package:id/password" (if ID exists)
     * 3. XPath: "//android.widget.EditText[@index='1']" (if multiple EditTexts)
     * 4. XPath: "//android.widget.EditText[@password='true']" (if password attribute exists)
     * 
     * EXAMPLE:
     * public static String password = "passwordField";  // Accessibility ID
     * OR
     * public static String password = "//android.widget.EditText[@index='1']";  // XPath
     * 
     * USED IN: Login_Module.performLogin() method
     * 
     * ⚠️ WARNING: Empty locator will cause IllegalArgumentException when used
     */
    public static String password = "";
    
    /**
     * Continue button locator
     * 
     * WHAT THIS IS:
     * - XPath locator to find the Continue button
     * - Uses content-desc attribute (Accessibility ID equivalent)
     * 
     * LOCATOR TYPE: XPath with content-desc attribute
     * LOCATOR VALUE: "//android.widget.Button[@content-desc='Continue']"
     * 
     * HOW IT WORKS:
     * - "//android.widget.Button" = find Button widget
     * - "[@content-desc='Continue']" = where content-desc equals "Continue"
     * - This is more specific than just finding any button
     * 
     * WHY THIS APPROACH:
     * - Uses content-desc (Accessibility ID) which is more reliable
     * - Wrapped in XPath for flexibility
     * - Specific enough to find correct button
     * 
     * ALTERNATIVE (BETTER):
     * If button has content-desc, use Accessibility ID directly:
     * public static String Continue = "Continue";  // Then use "ACCESSIBILITYID" in Actions
     * 
     * USED IN: Potentially in login flow (if Continue button exists before login)
     */
    public static String Continue = "//android.widget.Button[@content-desc=\'Continue\']";
    
    /**
     * Login button locator
     * 
     * WHAT THIS IS:
     * - Locator to find the Login/Submit button
     * - Currently EMPTY - needs to be filled
     * 
     * STATUS: ❌ EMPTY - MUST BE FILLED
     * 
     * WHY EMPTY:
     * - Login button locator not yet identified
     * - Needs to be found using Appium Inspector
     * 
     * HOW TO FILL:
     * 1. Open Appium Inspector
     * 2. Navigate to login page
     * 3. Find the Login/Submit button
     * 4. Get its Accessibility ID (content-desc) - PREFERRED
     * 5. Or get XPath if no Accessibility ID
     * 6. Update this field
     * 
     * RECOMMENDED LOCATOR TYPES (in order):
     * 1. Accessibility ID: "loginButton" or "btnLogin" (if content-desc exists)
     * 2. Resource ID: "com.app.package:id/loginButton"
     * 3. XPath by text: "//android.widget.Button[@text='Login']"
     * 4. XPath by content-desc: "//android.widget.Button[@content-desc='Login']"
     * 
     * EXAMPLE:
     * public static String LoginButton = "loginButton";  // Accessibility ID
     * OR
     * public static String LoginButton = "//android.widget.Button[@text='Login']";  // XPath
     * 
     * USED IN: Login_Module.performLogin() method
     * USAGE: click("ACCESSIBILITYID", LoginButton) or click("XPATH", LoginButton)
     * 
     * ⚠️ WARNING: Empty locator will cause IllegalArgumentException when used
     */
    public static String LoginButton = "";
    
    /**
     * Logout button locator
     * 
     * WHAT THIS IS:
     * - Locator to find the Logout button
     * - Currently EMPTY - needs to be filled
     * 
     * STATUS: ❌ EMPTY - MUST BE FILLED
     * 
     * WHY EMPTY:
     * - Logout button locator not yet identified
     * - Logout functionality not yet implemented
     * 
     * HOW TO FILL:
     * 1. Navigate to logged-in state in app
     * 2. Open Appium Inspector
     * 3. Find Logout button (may be in menu, settings, or main screen)
     * 4. Get its locator
     * 5. Update this field
     * 
     * RECOMMENDED LOCATOR TYPES:
     * 1. Accessibility ID: "logoutButton" or "btnLogout"
     * 2. XPath by text: "//android.widget.Button[@text='Logout']"
     * 3. XPath in menu: "//android.view.MenuItem[@text='Logout']"
     * 
     * EXAMPLE:
     * public static String Logoutbutton = "logoutButton";  // Accessibility ID
     * OR
     * public static String Logoutbutton = "//android.widget.Button[@text='Logout']";  // XPath
     * 
     * USED IN: Login_Module.logout() method (when implemented)
     * 
     * ⚠️ WARNING: Empty locator will cause error if logout() is called
     * 
     * NOTE: Logout button location varies by app:
     * - Some apps: Main screen button
     * - Some apps: Menu → Logout
     * - Some apps: Settings → Logout
     * - Some apps: Profile/Side menu → Logout
     */
    public static String Logoutbutton = "";
}
