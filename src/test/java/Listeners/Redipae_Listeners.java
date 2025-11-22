package Listeners;

import Utilities.Utils;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static Base.Setup.props;
import static Utilities.Utils.screenshotName;

public class Redipae_Listeners implements ITestListener, ISuiteListener {

    static Date d = new Date();
    public static String fileName;
    public static boolean attachmentflag;
    public static ExtentReports extent;
    public static ExtentTest test;

    public static void initializeExtent() {
        if (extent == null && props != null) {
            try {
                fileName = props.get("Reportfilename") + d.toString().replace(":", "_").replace(" ", "_") + ".html";
                extent = Listeners.ExtentManager.createInstance(props.get("TestReportspath") + fileName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ThreadLocal<ExtentTest> testReport = new ThreadLocal
            <ExtentTest>();

    @Override
    public void onStart(org.testng.ISuite suite) {
        initializeExtent();
    }

    public void onTestStart(ITestResult result) {
        if (extent == null) {
            initializeExtent();
        }
        ExtentTest test = extent.createTest(result.getTestClass().getName() + "     @TestCase : " + result.getMethod().getMethodName());
        testReport.set(test);
    }

    public void onTestSuccess(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String logText = "<b>" + "TEST CASE:- " + methodName.toUpperCase() + " PASSED" + "</b>";
        Markup m = MarkupHelper.createLabel(logText, ExtentColor.GREEN);
        testReport.get().pass(m);
    }

    public void onTestFailure(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String excepionMessage = Arrays.toString(result.getThrowable().getStackTrace());
        testReport.get().fail("<details>" + "<summary>" + "<b>" + "<font color=" + "red>" + "Exception Occurred: Click to see" + "</font>" + "</b >" + "</summary>" + excepionMessage.replaceAll(",", "<br>") + "</details>" + " \n");
        attachmentflag = true;
        try {
            Utils.captureScreenshot();
            testReport.get().fail("<b>" + "<font color=" + "red>" + "Screenshot of failure" + "</font>" + "</b>",
                    MediaEntityBuilder.createScreenCaptureFromPath(props.get("Screenshotpath") + screenshotName)
                            .build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String failureLog = "TEST CASE FAILED";
        Markup m = MarkupHelper.createLabel(failureLog, ExtentColor.RED);
        testReport.get().log(Status.FAIL, m);
    }

    public void onTestSkipped(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String logText = "<b>" + "Test Case:- " + methodName + " Skipped" + "</b>";
        Markup m = MarkupHelper.createLabel(logText, ExtentColor.YELLOW);
        testReport.get().skip(m);
    }

    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    public static String formatData(String message, String... values) {
        String formattedData = "<span style=\"color: Black; font-weight: bold;\">" + message + " </span><span style=\"color: Black;\">";
        formattedData += String.join(" | ", values);
        formattedData += "</span>";
        return formattedData;
    }

    public static void logAndReportSuccess(String message) {
        testReport.get().pass(formatData(message));
    }

    public static void logAndinfo(String message) {
        testReport.get().info(formatData(message));
    }

    public static String logAndReportFailure(String message) {
        testReport.get().fail(formatData(message));
        return message;
    }

}
