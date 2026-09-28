package com.dvdrental.db.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.dvdrental.db.hooks", "com.dvdrental.db.steps"},
    plugin = {"pretty", "json:target/cucumber-reports/cucumber.json"},
    monochrome = true
)
public class DvdRentalRunner {
}
