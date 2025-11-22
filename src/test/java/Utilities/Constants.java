package Utilities;

import static Utilities.Utils.generateFormattedDate;
import static Utilities.Utils.getDeviceName;

/**
 * Constants Class - Stores constant values used throughout the framework
 * 
 * PURPOSE: Centralizes all constant values in one place for easy maintenance.
 * 
 * WHY THIS CLASS EXISTS:
 * - Single source of truth for constant values
 * - Easy to update values without searching through code
 * - Prevents magic strings/numbers scattered in code
 */
public class Constants {

    /**
     * Device name/ID of connected Android device
     * 
     * WHAT THIS DOES:
     * - Calls getDeviceName() to detect connected device via ADB
     * - If no device found (null), uses fallback "Android Device"
     * 
     * WHY NULL CHECK:
     * - getDeviceName() can return null if no device connected
     * - Prevents NullPointerException in Setup.java when setting capabilities
     * - Fallback allows framework to continue (though test will likely fail)
     * 
     * WHY STATIC FINAL:
     * - Static: Available without creating instance
     * - Final: Value shouldn't change after initialization
     * 
     * USED IN: Setup.java - setDesiredCapabilites() method
     */
    public static final String Devicename = getDeviceName() != null ? getDeviceName() : "Android Device";

    /**
     * Path to Config.properties file
     * 
     * WHAT THIS DOES:
     * - Constructs absolute path to Config.properties file
     * - Combines project root + relative path to properties file
     * 
     * WHY ABSOLUTE PATH:
     * - Works regardless of current working directory
     * - Prevents "file not found" errors
     * 
     * WHY STATIC FINAL:
     * - Used throughout framework, shouldn't change
     * 
     * USED IN: Utils.java - loadProperties() method
     */
    public static final String configfilepath = System.getProperty("user.dir") + "/src/test/resources/Properties/Config.properties";

    /**
     * Email body template for test report emails
     * 
     * WHAT THIS DOES:
     * - Template text for email sent after test execution
     * - Includes dynamic date using generateFormattedDate()
     * 
     * WHY THIS EXISTS:
     * - Standardized email format
     * - Easy to update email content in one place
     * 
     * WHY STATIC:
     * - Can be accessed without instance
     * - Used in MailConfig.java when sending reports
     * 
     * NOTE: Date is generated dynamically when email is sent
     */
    public static String body = "Dear Team,\n" +
            "\n" +
            "Please find the attached test automation report for FrameX Mobile executed on "+generateFormattedDate("dd-MM-yy")+" . The test suite covered various scenarios validating the functionalities of FrameX mobile.\n" +
            "\n" +
            "The test suite execution results indicate [summary of test outcomes - overall success, challenges, critical issues, etc.].\n" +
            "\n" +
            "Attached Test Report:\n" +
            "The attached test report provides detailed information on individual test cases, their status, logs, and any errors encountered during execution.\n" +
            "\n" +
            "Please review the attached report for a comprehensive understanding of the test execution results.\n" +
            "\n" +
            "\n" +
            "Thank you,\n" +
            "Fieldlytics QA Team\n";

}
