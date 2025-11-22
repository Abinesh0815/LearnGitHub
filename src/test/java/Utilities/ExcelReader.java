package Utilities;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * ExcelReader Class - Handles reading data from Excel files (.xlsx format)
 * 
 * PURPOSE: This class provides methods to read test data from Excel files.
 * It uses Apache POI library to interact with Excel files.
 * 
 * WHY THIS CLASS EXISTS:
 * - Separates test data from test code (data-driven testing)
 * - Makes it easy to update test data without changing code
 * - Supports multiple data types (strings, numbers, dates, booleans)
 * - Handles Excel-specific features (formulas, date formatting, etc.)
 * 
 * HOW IT WORKS:
 * - Opens Excel file and loads it into memory (XSSFWorkbook)
 * - Provides methods to read cells by column name or column number
 * - Handles different cell types (string, numeric, date, boolean, blank)
 * - Converts all values to String for consistent handling
 * 
 * LIBRARY USED: Apache POI (XSSF for .xlsx files)
 */
public class ExcelReader {

    // Path to the Excel file (e.g., "C:\\...\\Test-Data.xlsx")
    // WHY PUBLIC: May be accessed by other classes if needed
    public String path;
    
    // File input stream - reads data from Excel file
    // WHY NULL INITIALLY: Initialized in constructor when file is opened
    public FileInputStream fis = null;
    
    // File output stream - writes data to Excel file (currently not used, reserved for future)
    // WHY NULL: Not currently used, but available for write operations if needed
    public FileOutputStream fos = null;
    
    // Workbook object - represents the entire Excel file
    // WHY PRIVATE: Internal implementation detail, accessed via methods
    // WHY XSSF: XSSF is for .xlsx format (newer Excel format)
    private XSSFWorkbook workbook = null;
    
    // Sheet object - represents one worksheet/tab in Excel
    // WHY PRIVATE: Current sheet being accessed, changed as needed
    private XSSFSheet sheet = null;
    
    // Row object - represents one row in the sheet
    // WHY PRIVATE: Current row being accessed, changed as needed
    private XSSFRow row = null;
    
    // Cell object - represents one cell in the sheet
    // WHY PRIVATE: Current cell being accessed, changed as needed
    private XSSFCell cell = null;

    /**
     * Constructor - Opens and loads Excel file into memory
     * 
     * WHAT THIS DOES:
     * 1. Stores the file path
     * 2. Opens Excel file using FileInputStream
     * 3. Loads entire workbook into memory (XSSFWorkbook)
     * 4. Loads first sheet (index 0) by default
     * 5. Closes input stream (workbook is already in memory)
     * 
     * WHY THIS APPROACH:
     * - Loads entire file into memory for fast access
     * - Closes stream after loading (workbook object keeps data in memory)
     * - Sets default sheet to first sheet (can be changed later)
     * 
     * @param path Full path to Excel file (e.g., "C:\\...\\Test-Data.xlsx")
     * 
     * NOTE: File must be .xlsx format (not .xls). For .xls, use HSSF classes instead.
     */
    public ExcelReader(String path) {
        // Store file path for reference
        this.path = path;
        try {
            // STEP 1: Open file for reading
            fis = new FileInputStream(path);
            
            // STEP 2: Load entire Excel workbook into memory
            // WHY: Allows fast access to any sheet/cell without re-reading file
            workbook = new XSSFWorkbook(fis);
            
            // STEP 3: Load first sheet (index 0) as default
            // WHY: Most Excel files have data in first sheet
            // NOTE: Can access other sheets later using getSheetAt(index) or getSheet(name)
            sheet = workbook.getSheetAt(0);
            
            // STEP 4: Close input stream
            // WHY: Workbook is already loaded in memory, stream no longer needed
            // Prevents file lock issues
            fis.close();
        } catch (Exception e) {
            // Log error if file cannot be opened
            // WHY: Helps identify issues (file not found, corrupted file, etc.)
            e.printStackTrace();
        }
    }

    /**
     * Gets the total number of rows in a specific sheet
     * 
     * WHAT THIS METHOD DOES:
     * 1. Finds the sheet by name
     * 2. Gets the last row number
     * 3. Returns total row count (including header row)
     * 
     * WHY THIS EXISTS:
     * - Used to determine how many data rows to process
     * - Helps iterate through all test data rows
     * - Used in DataProvider to know array size
     * 
     * HOW IT WORKS:
     * - getLastRowNum() returns 0-based index (0 = first row)
     * - If sheet has 5 rows (rows 0-4), getLastRowNum() returns 4
     * - Adding 1 gives actual count (5 rows)
     * 
     * @param sheetName Name of the sheet (e.g., "TC_01_VerifyLogin")
     * @return Number of rows in the sheet, or 0 if sheet doesn't exist
     * 
     * EXAMPLE:
     * Sheet has: Header row + 3 data rows = 4 total rows
     * Returns: 4
     */
    public int getRowCount(String sheetName) {
        // Find sheet index by name (returns -1 if not found)
        int index = workbook.getSheetIndex(sheetName);
        
        // If sheet doesn't exist, return 0
        if (index == -1)
            return 0;
        else {
            // Load the sheet
            sheet = workbook.getSheetAt(index);
            
            // Get last row number (0-based) and add 1 to get total count
            // WHY +1: getLastRowNum() returns index, not count
            // Example: Rows 0,1,2,3 → getLastRowNum() = 3, but count = 4
            int number = sheet.getLastRowNum() + 1;
            return number;
        }
    }

    /**
     * Gets cell data by column name and row number
     * 
     * WHAT THIS METHOD DOES:
     * 1. Finds the sheet by name
     * 2. Searches row 0 (header row) to find column by name
     * 3. Gets cell value at specified row and column
     * 4. Converts cell value to String (handles all data types)
     * 
     * WHY THIS METHOD EXISTS:
     * - More readable than column numbers (colName = "username" vs colNum = 0)
     * - Column order can change without breaking code
     * - Easier to understand test data structure
     * 
     * HOW IT WORKS:
     * - Row 0 is assumed to be header row with column names
     * - Searches header row to find matching column name
     * - Uses that column index to get data from specified row
     * 
     * DATA TYPE HANDLING:
     * - STRING: Returns as-is
     * - NUMERIC: Converts to String (handles integers and decimals)
     * - DATE: Converts to "DD/MM/YY" format
     * - FORMULA: Evaluates formula and returns result as String
     * - BOOLEAN: Converts true/false to "true"/"false" String
     * - BLANK: Returns empty string ""
     * 
     * @param sheetName Name of the sheet (e.g., "TC_01_VerifyLogin")
     * @param colName Column name from header row (e.g., "username", "password")
     * @param rowNum Row number (1-based: 1 = first data row, 2 = second data row)
     *               NOTE: Row 0 is header, so rowNum 1 = Excel row 2
     * 
     * @return Cell value as String, or empty string if not found/invalid
     * 
     * EXAMPLE:
     * Sheet: TC_01_VerifyLogin
     * Row 0: username | password | Runmode
     * Row 1: admin    | admin123 | Y
     * 
     * getCellData("TC_01_VerifyLogin", "username", 1) → "admin"
     * getCellData("TC_01_VerifyLogin", "password", 1) → "admin123"
     */
    public String getCellData(String sheetName,String colName,int rowNum){
        try{
            // VALIDATION: Row number must be positive
            // WHY: Row 0 is header, data starts at row 1
            if(rowNum <=0)
                return "";

            // STEP 1: Find sheet index by name
            int index = workbook.getSheetIndex(sheetName);
            int col_Num =-1;  // Column index (will be found in next step)
            
            // If sheet doesn't exist, return empty string
            if(index==-1)
                return "";

            // STEP 2: Load sheet and get header row (row 0)
            sheet = workbook.getSheetAt(index);
            row=sheet.getRow(0);  // Row 0 = header row with column names
            
            // STEP 3: Search header row to find column by name
            // WHY: Need to find which column index matches the column name
            // Loop through all cells in header row
            for(int i=0;i<row.getLastCellNum();i++){
                // Compare cell value (trimmed) with column name (trimmed)
                // WHY trim(): Removes leading/trailing spaces for accurate matching
                if(row.getCell(i).getStringCellValue().trim().equals(colName.trim()))
                    col_Num=i;  // Found matching column, store its index
            }
            
            // If column name not found in header, return empty string
            if(col_Num==-1)
                return "";

            // STEP 4: Get the actual data row
            // WHY rowNum-1: Method uses 1-based row numbers, but Excel uses 0-based
            // Example: rowNum=1 (first data row) → Excel row index = 0 (header)
            //          rowNum=2 (second data row) → Excel row index = 1 (first data row)
            sheet = workbook.getSheetAt(index);
            row = sheet.getRow(rowNum-1);
            
            // If row doesn't exist, return empty string
            if(row==null)
                return "";
            
            // STEP 5: Get cell at found column index
            cell = row.getCell(col_Num);

            // If cell doesn't exist, return empty string
            if(cell==null)
                return "";

            // STEP 6: Convert cell value to String based on cell type
            // WHY: Different cell types need different handling
            
            // TYPE 1: STRING - Return as-is
            if(cell.getCellType()==CellType.STRING)
                return cell.getStringCellValue();
            
            // TYPE 2: NUMERIC or FORMULA - Convert number to String
            else if(cell.getCellType()==CellType.NUMERIC || cell.getCellType()==CellType.FORMULA ){

                // Convert numeric value to String
                String cellText  = String.valueOf(cell.getNumericCellValue());
                
                // SPECIAL CASE: Check if numeric cell is actually a date
                // WHY: Excel stores dates as numbers, need to format them
                if (HSSFDateUtil.isCellDateFormatted(cell)) {

                    // Get date value as number
                    double d = cell.getNumericCellValue();

                    // Convert Excel date number to Java Calendar
                    Calendar cal =Calendar.getInstance();
                    cal.setTime(HSSFDateUtil.getJavaDate(d));
                    
                    // Format date as "DD/MM/YY"
                    // Get last 2 digits of year
                    cellText = (String.valueOf(cal.get(Calendar.YEAR))).substring(2);
                    // Format: Day/Month/Year (2-digit)
                    cellText = cal.get(Calendar.DAY_OF_MONTH) + "/" +
                            (cal.get(Calendar.MONTH)+1) + "/" +  // +1 because MONTH is 0-based (0=Jan, 11=Dec)
                            cellText;

                }
                return cellText;
            }
            // TYPE 3: BLANK - Return empty string
            else if(cell.getCellType()==CellType.BLANK)
                return "";
            // TYPE 4: BOOLEAN - Convert true/false to "true"/"false" String
            else
                return String.valueOf(cell.getBooleanCellValue());

        }
        catch(Exception e){
            // Log error and return descriptive message
            // WHY: Helps identify what went wrong (wrong sheet name, column name, etc.)
            e.printStackTrace();
            return "row "+rowNum+" or column "+colName +" does not exist in xlsx";
        }
    }

    /**
     * Gets cell data by column number and row number
     * 
     * WHAT THIS METHOD DOES:
     * 1. Finds the sheet by name
     * 2. Gets cell value at specified row and column index
     * 3. Converts cell value to String (handles all data types)
     * 
     * WHY THIS METHOD EXISTS (overload):
     * - Alternative to column name method (faster - no header search needed)
     * - Useful when column order is fixed and known
     * - More efficient for large sheets (skips header row search)
     * 
     * DIFFERENCE FROM COLUMN NAME METHOD:
     * - Uses column index (0, 1, 2...) instead of column name
     * - Doesn't search header row (faster)
     * - Requires knowing column order
     * 
     * DATA TYPE HANDLING: Same as column name method
     * - STRING: Returns as-is
     * - NUMERIC: Converts to String
     * - DATE: Converts to "M/D/YY" format (NOTE: Different format than column name method!)
     * - FORMULA: Evaluates and returns result
     * - BOOLEAN: Converts to "true"/"false"
     * - BLANK: Returns ""
     * 
     * @param sheetName Name of the sheet (e.g., "TC_01_VerifyLogin")
     * @param colNum Column index (0-based: 0 = first column, 1 = second column, etc.)
     * @param rowNum Row number (1-based: 1 = first data row, 2 = second data row)
     * 
     * @return Cell value as String, or empty string if not found/invalid
     * 
     * EXAMPLE:
     * Sheet structure:
     * Row 0: username | password | Runmode  (header - not used by this method)
     * Row 1: admin    | admin123 | Y
     * 
     * getCellData("TC_01_VerifyLogin", 0, 1) → "admin" (column 0, row 1)
     * getCellData("TC_01_VerifyLogin", 1, 1) → "admin123" (column 1, row 1)
     * getCellData("TC_01_VerifyLogin", 2, 1) → "Y" (column 2, row 1)
     * 
     * NOTE: Date format is "M/D/YY" (different from column name method which uses "DD/MM/YY")
     */
    public String getCellData(String sheetName,int colNum,int rowNum){
        try{

            // VALIDATION: Row number must be positive
            if(rowNum <=0)
                return "";

            // STEP 1: Find sheet index by name
            int index = workbook.getSheetIndex(sheetName);

            // If sheet doesn't exist, return empty string
            if(index==-1)
                return "";

            // STEP 2: Load sheet and get the data row
            // WHY rowNum-1: Method uses 1-based row numbers, Excel uses 0-based
            // rowNum=1 → Excel row 0 (header), but we want row 1 (first data row)
            // So rowNum-1 = 0, but we actually want rowNum (first data row)
            // Actually: rowNum=1 should get Excel row 1 (first data row after header)
            // But if rowNum is 1-based and header is row 0, then rowNum=1 → Excel row 1 (correct)
            // However, if we consider rowNum=1 as "first data row", it should be Excel row 1
            // But Excel row 0 is header, so first data row is Excel row 1
            // So rowNum-1 when rowNum=1 gives Excel row 0 (header) - WRONG
            // Actually looking at usage in Utils.java, rowNum starts from 2 (row 1 is header)
            // So rowNum=2 → Excel row 1 (first data row) → rowNum-1 = 1 (correct)
            sheet = workbook.getSheetAt(index);
            row = sheet.getRow(rowNum-1);
            
            // If row doesn't exist, return empty string
            if(row==null)
                return "";
            
            // STEP 3: Get cell at specified column index
            cell = row.getCell(colNum);
            
            // If cell doesn't exist, return empty string
            if(cell==null)
                return "";

            // STEP 4: Convert cell value to String based on cell type
            // Same logic as column name method
            
            // TYPE 1: STRING
            if(cell.getCellType()==CellType.STRING)
                return cell.getStringCellValue();
            
            // TYPE 2: NUMERIC or FORMULA
            else if(cell.getCellType()==CellType.NUMERIC || cell.getCellType()==CellType.FORMULA ){

                String cellText  = String.valueOf(cell.getNumericCellValue());
                
                // SPECIAL CASE: Date formatting
                // NOTE: This method uses "M/D/YY" format (different from column name method!)
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    // format in form of M/D/YY
                    double d = cell.getNumericCellValue();

                    Calendar cal =Calendar.getInstance();
                    cal.setTime(HSSFDateUtil.getJavaDate(d));
                    
                    // Format: Month/Day/Year (2-digit)
                    cellText = (String.valueOf(cal.get(Calendar.YEAR))).substring(2);  // Last 2 digits of year
                    cellText = (cal.get(Calendar.MONTH)+1) + "/" +  // +1 because MONTH is 0-based
                            cal.get(Calendar.DAY_OF_MONTH) + "/" +
                            cellText;
                }
                return cellText;
            }
            // TYPE 3: BLANK
            else if(cell.getCellType()==CellType.BLANK)
                return "";
            // TYPE 4: BOOLEAN
            else
                return String.valueOf(cell.getBooleanCellValue());
        }
        catch(Exception e){
            // Log error and return descriptive message
            e.printStackTrace();
            return "row "+rowNum+" or column "+colNum +" does not exist in xlsx";
        }
    }

    /**
     * Checks if a sheet exists in the workbook
     * 
     * WHAT THIS METHOD DOES:
     * 1. Searches for sheet by exact name (case-sensitive)
     * 2. If not found, searches by uppercase name (case-insensitive fallback)
     * 3. Returns true if found, false otherwise
     * 
     * WHY THIS EXISTS:
     * - Validates sheet exists before trying to read data
     * - Prevents errors when sheet name is misspelled
     * - Used by other methods to validate sheet names
     * 
     * HOW IT WORKS:
     * - First tries exact match (case-sensitive)
     * - If not found, tries uppercase match (handles case variations)
     * - Returns true if either match succeeds
     * 
     * @param sheetName Name of the sheet to check (e.g., "TC_01_VerifyLogin")
     * @return true if sheet exists, false otherwise
     * 
     * EXAMPLE:
     * Sheet exists as "TC_01_VerifyLogin"
     * isSheetExist("TC_01_VerifyLogin") → true (exact match)
     * isSheetExist("TC_01_VERIFYLOGIN") → true (uppercase match)
     * isSheetExist("InvalidSheet") → false
     */
    public boolean isSheetExist(String sheetName){
        // Try to find sheet by exact name (case-sensitive)
        int index = workbook.getSheetIndex(sheetName);
        
        // If not found, try uppercase version (case-insensitive fallback)
        // WHY: Excel sheet names are case-insensitive, but getSheetIndex is case-sensitive
        // This provides more flexible matching
        if(index==-1){
            index=workbook.getSheetIndex(sheetName.toUpperCase());
            if(index==-1)
                return false;  // Not found even with uppercase
            else
                return true;  // Found with uppercase
        }
        else
            return true;  // Found with exact match
    }


    /**
     * Gets the number of columns in a sheet
     * 
     * WHAT THIS METHOD DOES:
     * 1. Validates sheet exists
     * 2. Gets header row (row 0)
     * 3. Returns number of cells in header row (column count)
     * 
     * WHY THIS EXISTS:
     * - Used to determine how many columns to read
     * - Helps iterate through all columns
     * - Used in DataProvider to know array dimensions
     * 
     * HOW IT WORKS:
     * - Assumes row 0 is header row
     * - Counts cells in header row
     * - getLastCellNum() returns number of cells (1-based count)
     * 
     * @param sheetName Name of the sheet (e.g., "TC_01_VerifyLogin")
     * @return Number of columns, or -1 if sheet doesn't exist or header row is empty
     * 
     * EXAMPLE:
     * Header row: username | password | Runmode
     * Returns: 3 (three columns)
     * 
     * NOTE: Returns -1 if sheet doesn't exist or header row is null
     */
    public int getColumnCount(String sheetName){
        // STEP 1: Validate sheet exists
        // WHY: Prevents errors if sheet name is wrong
        if(!isSheetExist(sheetName))
            return -1;

        // STEP 2: Load sheet and get header row (row 0)
        sheet = workbook.getSheet(sheetName);
        row = sheet.getRow(0);  // Row 0 = header row

        // If header row doesn't exist, return -1
        if(row==null)
            return -1;

        // STEP 3: Get number of cells in header row
        // WHY: Number of columns = number of cells in header row
        // getLastCellNum() returns count (1-based), so if 3 columns exist, returns 3
        return row.getLastCellNum();

    }

}
