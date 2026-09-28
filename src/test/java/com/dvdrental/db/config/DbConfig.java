package com.dvdrental.db.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DbConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    public static String getUrl() {
        return props.getProperty("db.url");
    }

    public static String getUser() {
        return props.getProperty("db.user");
    }

    public static String getPassword() {
        return props.getProperty("db.password");
    }

    public static String getDriver() {
        return props.getProperty("db.driver");
    }
}
