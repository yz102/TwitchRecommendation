package com.laioffer.jupiter.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//帮我生成一个链接MySQL的address
public class MySQLDBUtil {
    //endpoint
    private static final String INSTANCE = "laiproject.cp0tbbjozcy1.us-east-2.rds.amazonaws.com";
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "jupiter";

    //how to link
    //固定写法
    //连接mysql的目的地
    public static String getMySQLAddress() throws IOException {
        //read property document
        Properties prop = new Properties();
        //relative path
        String propFileName = "config.properties";
        //read data from document
        //文件有大有小，读文件和读网络数据，都是一段一段读取
        //property: key, value pair
        //class.getClassLoader().getResourceAsStream 当前路径切换,我只写文件名字就可以
//        if is absolute path, FileInputStream
        InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);
        //map.get(key)
        String username = prop.getProperty("user");
        String password = prop.getProperty("password");
        //jdbc:mysql
        //java database connector,通过java程序连接mysql
        //下面是三个%s
        //first parameter: instance
        //second parameter: port
        //third parameter: database name
        //autoreconnect=true: if accident happens, reconnect
        //createDatabaseIfNotExist=true, we explicitly write DB_name = jupiter, 这句话可有可无

        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password);
    }

}
