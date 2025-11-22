package Base;

import Utilities.ExcelReader;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import javax.mail.MessagingException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;

import static Listeners.Redipae_Listeners.fileName;
import static Utilities.Constants.Devicename;
import static Utilities.MailConfig.sendMailReport;
import static Utilities.Utils.loadProperties;

/**
 * Setup Class - Base class for Mobile Automation Framework
 * 
 * PURPOSE: This class handles all the initialization and cleanup activities for mobile automation testing.
 * It sets up the Appium server, Android driver, loads configuration, and manages test data.
 * 
 * WHY THIS CLASS EXISTS:
 * - Centralizes all setup/teardown logic in one place
 * - Ensures proper initialization order (properties -> logging -> Excel -> Appium -> Driver)
 * - Provides reusable static variables accessible throughout the test framework
 * - Manages Appium server lifecycle (start/stop)
 */
public class Setup {

    // Static driver instance - shared across all test classes
    // WHY STATIC: Allows all test classes to access the same driver instance without passing it around
    public static AndroidDriver driver;
    
    // Appium server service instance - manages the local Appium server
    // WHY NEEDED: Appium server must be running before we can connect to mobile devices
    public static AppiumDriverLocalService service;
    
    // Desired capabilities - configuration for the mobile device and app
    // WHY NEEDED: Tells Appium which device, app, and settings to use for automation
    public static DesiredCapabilities capabilities;
    
    // Excel reader instance - for reading test data from Excel files
    // WHY NEEDED: Test data is stored in Excel, this object reads it
    public static ExcelReader excel;
    
    // Properties map - stores all configuration values from Config.properties file
    // WHY NEEDED: Centralized configuration management (app paths, timeouts, etc.)
    public static HashMap<String, String> props;
    
    // Device model name - stores the connected device model
    // WHY NEEDED: Useful for reporting and debugging which device was used
    public static String deviceModel;

    /**
     * @BeforeSuite - TestNG annotation that runs ONCE before all tests in the suite
     * alwaysRun = true - Ensures this runs even if previous setup fails
     * 
     * WHAT THIS METHOD DOES:
     * 1. Loads configuration properties from Config.properties file
     * 2. Initializes logging system (Log4j)
     * 3. Loads Excel test data file
     * 4. Starts local Appium server on port 4723
     * 5. Sets up desired capabilities (device, app info)
     * 6. Creates Android driver connection to the device
     * 7. Sets implicit wait timeout for element finding
     * 
     * WHY THIS ORDER:
     * - Properties must load first (everything depends on config)
     * - Logging must initialize early (to log any errors)
     * - Excel loads before tests run (tests need data)
     * - Appium server must start before driver creation (driver connects to server)
     * - Capabilities must be set before driver creation (driver needs config)
     * - Driver is created last (it needs everything else ready)
     */
    @BeforeSuite(alwaysRun = true)
    public void StartApp() throws Exception {

        try {
            // STEP 1: Load all properties from Config.properties file
            // WHY FIRST: All other steps need configuration values (paths, timeouts, etc.)
            props = loadProperties();

            // STEP 2: Initialize logging system using Log4j
            // WHY: So we can log errors and debug information throughout test execution
            PropertyConfigurator.configure(props.get("Logpropertiesfilepath"));

            // STEP 3: Load Excel file containing test data
            // WHY: Test methods use data from Excel (usernames, passwords, etc.)
            // Loaded here instead of static block to ensure props are loaded first
            excel = new ExcelReader(props.get("Datafilepath"));

            // STEP 4: Build and start Appium server
            // WHY: Appium server acts as a bridge between our code and the mobile device
            // It must be running before we can create a driver connection
            service = new AppiumServiceBuilder()
                    .withAppiumJS(new File(props.get("Server")))  // Path to Appium main.js file
                    .withIPAddress("127.0.0.1")  // Localhost IP address
                    .usingPort(4723)  // Default Appium port
                    .build();

            // Start the Appium server - this launches the server process
            service.start();

            // STEP 5: Set desired capabilities (device and app configuration)
            // WHY: Driver needs to know which device, app, and settings to use
            setDesiredCapabilites();

            // STEP 6: Create Android driver instance
            // WHY: This is the main object we use to interact with the mobile app
            // It connects to Appium server, which then communicates with the device
            driver = new AndroidDriver(new URL(props.get("Serverurl")), capabilities);

            // STEP 7: Extract device model name from driver capabilities
            // WHY: Useful for reporting and debugging - know which device was tested
            // Null check prevents crash if capability doesn't exist
            Object deviceModelCap = driver.getCapabilities().getCapability("deviceModel");
            deviceModel = deviceModelCap != null ? deviceModelCap.toString() : "Unknown Device";

            // STEP 8: Set implicit wait timeout
            // WHY: Tells driver to wait up to X seconds when searching for elements
            // Prevents immediate failures if element takes time to appear
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(
                    Integer.parseInt(props.get("Implicitywaittimeout"))
            ));

        } catch (Exception ex) {
            // Wrap any exception in RuntimeException with clear message
            // WHY: Makes it easier to identify what went wrong during setup
            throw new RuntimeException("Error starting the app", ex);
        }
    }

    /**
     * @AfterSuite - TestNG annotation that runs ONCE after all tests complete
     * alwaysRun = true - Ensures cleanup happens even if tests fail
     * 
     * WHAT THIS METHOD DOES:
     * 1. Closes the Android driver (disconnects from device)
     * 2. Stops the Appium server (frees up resources)
     * 3. Sends email report if enabled in config
     * 4. Opens test report in browser automatically
     * 
     * WHY THIS ORDER:
     * - Driver must close first (cleanest shutdown)
     * - Server stops after driver (no longer needed)
     * - Email sent before opening browser (non-blocking)
     * - Browser opens last (user sees results)
     */
    @AfterSuite(alwaysRun = true)
    public static void TearDown() {
        try {
            // Close driver connection to device
            closeDriver();
            
            // Stop Appium server to free resources
            stopAppiumService();
            
            // Send email report if EmailMode is enabled in Config.properties
            handleEmailReport();
            
            // Wait 2 seconds for report file to be fully written
            Thread.sleep(2000);
            
            // Automatically open the test report in default browser
            openReportInBrowser();
        } catch (Exception ex) {
            throw new RuntimeException("Error during test teardown", ex);
        }
    }

    /**
     * Closes the Android driver and disconnects from the device
     * WHY NULL CHECK: Prevents NullPointerException if driver was never initialized
     * WHY driver.quit() not close(): quit() closes all windows and ends session properly
     */
    static void closeDriver() {
        if (driver != null) {
            driver.quit();  // Properly closes driver and releases resources
        }
    }

    /**
     * Stops the Appium server process
     * WHY NULL CHECK: Prevents error if server was never started
     * WHY isRunning() CHECK: Only stop if actually running (avoids errors)
     */
    static void stopAppiumService() {
        if (service != null && service.isRunning()) {
            service.stop();  // Stops the Appium server process
        }
    }

    /**
     * Handles sending email report if enabled in configuration
     * WHY MULTIPLE NULL CHECKS: 
     * - props might be null if setup failed
     * - EmailMode key might not exist in properties
     * - trim() removes any whitespace from config value
     * WHY CONDITIONAL: Email sending is optional (can be disabled in config)
     */
    static void handleEmailReport() throws MessagingException, FileNotFoundException {
        if (props != null && props.get("EmailMode") != null && props.get("EmailMode").trim().equalsIgnoreCase("true")) {
            sendMailReport();  // Send email with test report attached
        }
    }

    /**
     * Opens the ExtentReports HTML file in default browser
     * WHY NULL CHECKS: Prevents errors if props or fileName are not initialized
     * WHY Desktop.getDesktop().browse(): Java's built-in way to open files in default application
     */
    static void openReportInBrowser() {
        if (props != null && fileName != null) {
            File extentReport = new File(props.get("TestReportspath") + fileName);
            try {
                Desktop.getDesktop().browse(extentReport.toURI());  // Opens HTML report in browser
            } catch (IOException ex) {
                ex.printStackTrace();  // Log error but don't fail teardown
            }
        }
    }

    /**
     * Sets up all desired capabilities for Appium
     * 
     * WHAT ARE CAPABILITIES:
     * Capabilities are key-value pairs that tell Appium how to configure the automation session.
     * They specify which device, app, and automation settings to use.
     * 
     * WHY THIS METHOD:
     * - Centralizes all capability configuration in one place
     * - Makes it easy to modify settings without changing multiple files
     * - Uses Appium 2.0 format (appium: prefix) for better compatibility
     */
    public void setDesiredCapabilites() {

        // Create new DesiredCapabilities object to store all settings
        capabilities = new DesiredCapabilities();

        // ========== BASIC PLATFORM & AUTOMATION SETTINGS ==========
        // Platform name - tells Appium we're testing Android
        capabilities.setCapability("platformName", props.get("platformName"));
        
        // Automation name - uses UIAutomator2 (Android's native automation framework)
        capabilities.setCapability("appium:automationName", props.get("automationName"));
        
        // App package - the unique identifier of the app (like com.example.app)
        capabilities.setCapability("appium:appPackage", props.get("appPackage"));
        
        // App activity - the main activity/entry point of the app
        capabilities.setCapability("appium:appActivity", props.get("appActivity"));

        // ========== PERMISSION & INITIALIZATION SETTINGS ==========
        // autoGrantPermissions: Automatically grant all app permissions (avoids permission popups)
        capabilities.setCapability("appium:autoGrantPermissions", true);
        
        // skipDeviceInitialization: Skip device setup steps (faster startup)
        capabilities.setCapability("appium:skipDeviceInitialization", true);
        
        // skipServerInstallation: Don't reinstall Appium server components (faster)
        capabilities.setCapability("appium:skipServerInstallation", true);
        
        // ignoreUnimportantViews: Ignore invisible UI elements (faster element finding)
        capabilities.setCapability("appium:ignoreUnimportantViews", true);
        
        // skipUnlock: Don't unlock device screen (assumes already unlocked)
        capabilities.setCapability("appium:skipUnlock", true);
        
        // ignoreHiddenApiPolicyError: Ignore Android hidden API restrictions
        capabilities.setCapability("appium:ignoreHiddenApiPolicyError", true);
        
        // noReset: Don't reset app data between tests (keeps app state)
        capabilities.setCapability("appium:noReset", true);

        // ========== TIMEOUT SETTINGS ==========
        // adbExecTimeout: Maximum time to wait for ADB commands (120 seconds)
        // WHY: Some ADB operations can take time, this prevents premature timeouts
        capabilities.setCapability("appium:adbExecTimeout", 120000);
        
        // NEW_COMMAND_TIMEOUT: Time to wait for new commands (100 seconds)
        // WHY: If no commands received for this time, session ends (prevents hanging)
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 100);

        // ========== DEVICE & APP IDENTIFICATION ==========
        // deviceName: Name of the connected Android device (from ADB)
        // WHY: Identifies which physical/emulated device to use
        capabilities.setCapability("appium:deviceName", Devicename);
        
        // app: Path to the APK file to install/launch
        // WHY: Tells Appium which app to test (full path to .apk file)
        capabilities.setCapability("appium:app", props.get("Apppath"));
    }
}
