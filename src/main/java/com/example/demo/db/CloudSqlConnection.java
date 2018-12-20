package com.example.demo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CloudSqlConnection {

    public CloudSqlConnection() throws  SQLException {
        String url = System.getProperty("cloudsql");
        String urlv3 = "jdbc:mysql://google/apireq?useSSL=false&cloudSqlInstance=cloud-sql-220423:us-central1:mysql-instance&socketFactory=com.google.cloud.sql.mysql.SocketFactory&user=root&password=dynamo_1984";

        final String selectSql = "SELECT DESCRIPTION,FEATURE_CODE,FEATURE_NAME,STATUS,USER_PETITIONER,USER_REVISER FROM apireq.SOLICITUDE";

        try {
            Connection conn = DriverManager.getConnection(urlv3);

           ResultSet rs = conn.prepareStatement(selectSql).executeQuery();

                System.out.println("Last 10 visits:\n");
                while (rs.next()) {
                    String timeStamp = rs.getString("DESCRIPTION");
                    System.out.println("Visited at time: " + timeStamp);
                }
        } catch (SQLException e) {
            throw new SQLException("Unable to connect to Cloud SQL", e);
        }
    }
}
