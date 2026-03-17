package com.project.cybershield.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DatabaseConfig {
    private static final String DB_FILE = "database-properties/database.properties";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    public static Connection connectionToDatabase() throws SQLException, IOException{
        Properties properties = new Properties();
        properties.load(new FileReader(DB_FILE));
        String databaseUrl = properties.getProperty("databaseUrl");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        Connection connection = DriverManager.getConnection(databaseUrl, username, password);

        return connection;
    }


}
