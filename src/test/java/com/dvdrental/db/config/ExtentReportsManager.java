package com.dvdrental.db.config;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportsManager {
    private static ExtentReports extent;
    private static ExtentTest test;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (extent != null) {
                extent.flush();
            }
        }));
    }

    public static void initReports() {
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/extent-report/index.html");
        htmlReporter.config().setReportName("Database Automation Report");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    public static void createTest(String testName) {
        test = extent.createTest(testName);
    }

    public static void logInfo(String message) {
        if (test != null) {
            test.info(message);
        }
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
