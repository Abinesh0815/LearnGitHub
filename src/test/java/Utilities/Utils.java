package Utilities;

import Base.Setup;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;

import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static Utilities.Constants.configfilepath;

public class Utils extends Setup {

    public static String screenshotName;
    public static int totalimagescaptured;

    /**
     * Checks if a test case should be executed based on Excel configuration
     * 
     * WHAT THIS METHOD DOES:
     * 1. Checks if test is enabled in "TestSuite" sheet (Y/N)
     * 2. Checks if current data row is enabled (Y/N in data Hashtable)
     * 
     * WHY THIS EXISTS:
     * - Allows enabling/disabling tests without code changes
     * - Can skip specific test data rows (useful for debugging)
     * - Centralizes test execution control
     * 
     * HOW IT WORKS:
     * - TestSuite sheet: Controls entire test (TC_01_VerifyLogin = Y/N)
     * - Data row Runmode: Controls individual data set execution
     * 
     * @param testcasename Name of test method (e.g., "TC_01_VerifyLogin")
     * @param data Hashtable containing test data including "Runmode" key
     * 
     * @throws SkipException if test should be skipped (TestNG handles this gracefully)
     */
    public static void checkexecution(String testcasename, Hashtable<String, String> data) {
        // CHECK 1: Is this test case enabled in TestSuite sheet?
        // WHY: Allows disabling entire test without commenting code
        // Example: Set "TC_01_VerifyLogin" Runmode = "N" in Excel to skip
        if (!Utils.isTestRunnable(testcasename, excel)) {
            throw new SkipException("Skipping the test " + testcasename.toUpperCase() + " as the Run mode is NO");
        }

        // CHECK 2: Is this specific data row enabled?
        // WHY: Can skip problematic data sets while keeping test enabled
        // Example: Skip row 3 data but run rows 1, 2, 4
        if (!data.get("Runmode").equals("Y")) {
            throw new SkipException("Skipping the test case as the Run mode for data set is NO");
        }
    }

    public static boolean isTestRunnable(String testName, ExcelReader excel) {
        // Define the sheet name where test case data is stored.
        String sheetName = "TestSuite";
        // Get the total number of rows in the sheet.
        int rows = excel.getRowCount(sheetName);

        // Iterate through the rows starting from row 2 (assuming row 1 contains headers).
        for (int rNum = 2; rNum <= rows; rNum++) {
            // Get the test case ID from the "TCID" column.
            String testCase = excel.getCellData(sheetName, "TCID", rNum);

            // Check if the current row corresponds to the given test case name.
            if (testCase.equalsIgnoreCase(testName)) {
                // Get the run mode from the "Runmode" column.
                String runmode = excel.getCellData(sheetName, "Runmode", rNum);

                // Check if the run mode is "Y" (indicating that the test case should be run).
                if (runmode.equalsIgnoreCase("Y")) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        // If the test case is not found in the sheet or not marked as "Y," return false.
        return false;
    }


    /**
     * DataProvider method - Supplies test data to TestNG test methods
     * 
     * WHAT THIS METHOD DOES:
     * 1. Gets test method name (e.g., "TC_01_VerifyLogin")
     * 2. Uses method name as Excel sheet name
     * 3. Reads all rows from that sheet (row 1 = headers, row 2+ = data)
     * 4. Converts each row into a Hashtable (key = column header, value = cell data)
     * 5. Returns 2D array where each row = one test execution
     * 
     * WHY THIS EXISTS:
     * - TestNG DataProvider pattern - allows running test with multiple data sets
     * - Separates test data from test code (data in Excel, not hardcoded)
     * - Each Excel row = one test execution
     * 
     * HOW IT WORKS:
     * - Test method name "TC_01_VerifyLogin" → looks for Excel sheet "TC_01_VerifyLogin"
     * - Row 1: Headers (username, password, Runmode, etc.)
     * - Row 2+: Test data (each row = one test run)
     * - Returns: [[{username: "user1", password: "pass1"}], [{username: "user2", password: "pass2"}]]
     * 
     * @param m Method object - TestNG provides this automatically
     * @return 2D Object array - each inner array contains one Hashtable with test data
     * 
     * EXAMPLE EXCEL STRUCTURE:
     * Sheet: TC_01_VerifyLogin
     * Row 1: username | password | Runmode
     * Row 2: admin    | admin123 | Y
     * Row 3: user1    | pass1    | Y
     * 
     * RESULT: Test runs twice - once with admin/admin123, once with user1/pass1
     */
    @DataProvider(name = "Testdata")
    public Object[][] getData(Method m) {

        // Get test method name (e.g., "TC_01_VerifyLogin")
        // WHY: Excel sheet name must match test method name
        String sheetName = m.getName();
        
        // Get total rows and columns in the Excel sheet
        // WHY: Need to know how much data to read
        int rows = excel.getRowCount(sheetName);
        int cols = excel.getColumnCount(sheetName);

        // Create 2D array: [rows-1][1]
        // WHY rows-1: Row 1 is headers, so data rows = total rows - 1
        // WHY [1]: Each row contains one Hashtable object
        Object[][] data = new Object[rows - 1][1];

        Hashtable<String, String> table = null;

        // Loop through data rows (start at row 2, row 1 is headers)
        for (int rowNum = 2; rowNum <= rows; rowNum++) {

            // Create new Hashtable for this row
            // WHY: Each row becomes one Hashtable (key-value pairs)
            table = new Hashtable<String, String>();

            // Loop through all columns in this row
            for (int colNum = 0; colNum < cols; colNum++) {

                // Build Hashtable: key = header (row 1, col colNum), value = data (row rowNum, col colNum)
                // Example: table.put("username", "admin") where "username" is from row 1, "admin" from row 2
                table.put(excel.getCellData(sheetName, colNum, 1), excel.getCellData(sheetName, colNum, rowNum));
                
                // Store Hashtable in data array
                // rowNum - 2: Convert row number to array index (row 2 → index 0, row 3 → index 1, etc.)
                data[rowNum - 2][0] = table;
            }

        }

        // Return 2D array - TestNG will call test method once for each inner array
        return data;
    }


    /**
     * Retrieves the name/ID of the connected Android device
     * 
     * WHAT THIS METHOD DOES:
     * 1. Executes "adb devices" command (Android Debug Bridge)
     * 2. Parses output to find device ID
     * 3. Returns first connected device ID
     * 
     * WHY THIS EXISTS:
     * - Appium needs device name/ID to know which device to connect to
     * - Automatically detects connected device (no manual configuration)
     * - Used in Setup.java to set deviceName capability
     * 
     * HOW IT WORKS:
     * - "adb devices" output format: "device_id    device"
     * - Example: "emulator-5554    device"
     * - Finds line ending with "device" (means device is connected and authorized)
     * - Extracts device ID (first part before tab)
     * 
     * @return Device ID (e.g., "emulator-5554") or null if no device found
     * 
     * WHY NULL CHECK IN Constants.java:
     * - If no device connected, returns null
     * - Constants.java provides fallback "Android Device" to prevent crash
     */
    public static String getDeviceName() {
        try {
            // Execute "adb devices" command to list connected devices
            // WHY: ADB is Android's command-line tool for device communication
            Process process = Runtime.getRuntime().exec("adb devices");
            
            // Read command output line by line
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            // Parse each line of output
            while ((line = reader.readLine()) != null) {
                // Look for line ending with "device" (means device is connected and ready)
                // WHY: "device" status means device is authorized and ready for commands
                // Other statuses: "unauthorized", "offline", etc.
                if (line.endsWith("device")) {
                    // Split line by tab character
                    // Format: "device_id    device"
                    String[] parts = line.split("\\t");
                    if (parts.length > 1) {
                        // Extract device ID (first part)
                        String deviceName = parts[0].trim();
                        return deviceName;  // Return first connected device
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return null if no device found or error occurred
        return null;
    }

    /**
     * Generates a formatted date string based on the given format.
     *
     * @param format the format string specifying the desired date format
     * @return the formatted date string
     * @throws DateTimeException if an error occurs while formatting the date
     */
    public static String generateFormattedDate(String format) {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Extract the last two digits of the year
        int lastTwoDigitsOfYear = currentDate.getYear() % 100;

        // Format the date in the required format "dd-MM-yy"
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern(format));

        return formattedDate;
    }

    /**
     * Generates a formatted date and time string.
     *
     * @return The formatted date and time string in the format "yyyy-MM-dd_hh:mm:ss_a".
     */
    public static String generatedateandtime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh:mm:ss_a");
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    /**
     * Loads configuration properties from Config.properties file
     * 
     * WHAT THIS METHOD DOES:
     * 1. Reads Config.properties file
     * 2. Loads all key-value pairs
     * 3. Converts relative paths to absolute paths
     * 4. Returns HashMap for easy access
     * 
     * WHY THIS EXISTS:
     * - Centralizes all configuration (app paths, timeouts, server URLs, etc.)
     * - Makes it easy to change settings without code changes
     * - Used throughout framework (Setup, listeners, etc.)
     * 
     * HOW IT WORKS:
     * - Reads Config.properties file
     * - For keys containing "path": Prepends project root directory
     *   Example: "Apppath = \\src\\test\\resources\\Apps\\Redipae.apk"
     *   Becomes: "C:\\Users\\...\\RedipaeMobile2025\\src\\test\\resources\\Apps\\Redipae.apk"
     * - Other keys: Used as-is
     * 
     * @return HashMap with all configuration values
     * @throws RuntimeException if file cannot be loaded
     * 
     * WHY ABSOLUTE PATHS:
     * - Relative paths can break if working directory changes
     * - Absolute paths always work regardless of where code runs from
     */
    public static HashMap<String,String>  loadProperties() throws FileNotFoundException {

        Properties properties = new Properties();
        FileInputStream fileInputStream = null;

        Map<String, String> propertiesMap = null;
        try {
            // Open Config.properties file for reading
            fileInputStream = new FileInputStream(configfilepath);
            
            // Load all properties from file into Properties object
            properties.load(fileInputStream);

            String value = "";

            // Convert Properties to HashMap for easier access
            propertiesMap = new HashMap<>();
            
            // Loop through all property keys
            for (String key : properties.stringPropertyNames()) {
                value = properties.getProperty(key);
                
                // SPECIAL HANDLING: Convert relative paths to absolute paths
                // WHY: If key contains "path", it's a file path that needs project root prepended
                // Example: "Apppath" → prepend user.dir (project root)
                if (key.contains("path")) {
                    // Append project root directory to relative path
                    // System.getProperty("user.dir") = project root directory
                    // Example: "C:\\Users\\...\\RedipaeMobile2025" + "\\src\\test\\resources\\Apps\\Redipae.apk"
                    value = System.getProperty("user.dir") + properties.getProperty(key);
                }
                
                // Store key-value pair in HashMap
                propertiesMap.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Always close file stream (even if error occurs)
            // WHY: Prevents resource leaks
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // VALIDATION: Ensure properties were loaded successfully
        // WHY: Prevents NullPointerException if file read failed
        if (propertiesMap == null) {
            throw new RuntimeException("Failed to load properties from config file: " + configfilepath);
        }
        return (HashMap<String, String>) propertiesMap;
    }

    /**
     * Captures a screenshot of the current screen.
     *
     * @throws IOException if an I/O error occurs while capturing the screenshot.
     */
    public static void captureScreenshot() throws IOException {

        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        Date d = new Date();
        screenshotName ="Screenshot_"+ d.toString().replace(":", "_").replace(" ", "_") + ".jpg";
        FileUtils.copyFile(scrFile, new File(props.get("Screenshotpath") + screenshotName));
    }

    /**
     * Checks if the given value exists in the page source.
     *
     * @param value the value to search for in the page source
     * @return true if the value exists in the page source, false otherwise
     * @throws NullPointerException if the page source is null
     */
    public static boolean sourceExists(String value) {

        return driver.getPageSource().contains(value);
    }

}
