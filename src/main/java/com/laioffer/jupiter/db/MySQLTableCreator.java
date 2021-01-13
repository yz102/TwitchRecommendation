package com.laioffer.jupiter.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

//create once
//存在意义就是生成table
public class MySQLTableCreator {
    // Run this as a Java application to reset the database.
    //含有main, 这个java class可以作为独立的java class启动
    //其他程序基于tomcat启动,MySqlCreator不依赖于tomcat启动
    public static void main(String[] args) {
        try {

            // Step 1 Connect to MySQL.
            System.out.println("Connecting to " + MySQLDBUtil.getMySQLAddress());
            //library有瑕疵，运行这一句保证不会出问题
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            //返回值是成功搭建的连接,连接就连上了
            //完成连接
            Connection conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());

            if (conn == null) {
                return;
            }

            // Step 2 Drop tables in case they exist.
//            delete old database, reset database
//            conn is返回的连接
            //通过连接创立statement，通过statement执行db query语句
            Statement statement = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS favorite_records";
//            executeUpdate: 写操作
//            executeQuery: 读操作
//            先delete favorite，再delete user and item

            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS items";
            statement.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS users";
            statement.executeUpdate(sql);

            // Step 3 Create new tables.
//            先create user and item, 再创建favorite
//            与item class对应
//            VARCHAR(255): String
            sql = "CREATE TABLE items ("
                    + "id VARCHAR(255) NOT NULL,"
                    + "title VARCHAR(255),"
                    + "url VARCHAR(255),"
                    + "thumbnail_url VARCHAR(255),"
                    + "broadcaster_name VARCHAR(255),"
                    + "game_id VARCHAR(255),"
                    + "type VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE users ("
                    + "id VARCHAR(255) NOT NULL,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "PRIMARY KEY (id)"
                    + ")";
            statement.executeUpdate(sql);

            sql = "CREATE TABLE favorite_records ("
                    + "user_id VARCHAR(255) NOT NULL,"
                    + "item_id VARCHAR(255) NOT NULL,"
                    + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"//插入数据时，可以不提供时间，mysql用当前时间，也可以自己提供时间
                    + "PRIMARY KEY (user_id, item_id),"//组合键
                    + "FOREIGN KEY (user_id) REFERENCES users(id),"//foreign key 指向谁
                    + "FOREIGN KEY (item_id) REFERENCES items(id)"
                    + ")";
            statement.executeUpdate(sql);

            // Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050.
            //insert fake user
            //id, password, firstname, lastname
            //password is 加密
//            hard code, else front end 给这些数据
            sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
            statement.executeUpdate(sql);

            conn.close();
            System.out.println("Import done successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
