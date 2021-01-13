package com.laioffer.jupiter.db;

import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.entity.User;

import java.sql.*;
import java.util.*;
//TwitchClient平行，和MySQL连接
public class MySQLConnection {
//    function 1

    private final Connection conn;

    public MySQLConnection() throws MySQLException {
        try {
//            jdbc library有问题，处理意外情况
//            httpclient connect
//            和database connect
//            if在自己电脑启动mysql, instance change to localhost
//            Driver driver = new Driver(); compile 事必须有Driver class
//            compile, 不确定driver class是否有, runtime 有就行
//            runtime add Driver class is ok, 反射机制
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance(); //same to line 20
//            初始化这一次机会，final，只有一次这赋值机会
//            建立连接
            conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to Database");
        }
    }
//function 2
//    断开连接
//    why close,
//    reason 1: 资源有限，eg. database最多允许50个连接,不占用数据库资源
//    reason 2: 每连接一个外部资源，消耗内存，1. connect, 2. read, 3. return to front end, step 3 has nothing with connection
//    close after step2 and before step 3, 部分内存资源被回收，有更多内存资源供我使用.运行效率高
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//实际逻辑功能
//    添加收藏记录，添加在favorite table
    public void setFavoriteItem(String userId, Item item) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
//        insert user_id and item_id to favorite table, maybe insert to item table
//        我添加的item别人没添加过
//        sql, statement
//        不会把这个作为一整个输入找user_id
//        String sql = "INSERT INTO favorite_records (user_id) VALUES ('1111; DROP TABLES;')";
//        String sql = "INSERT INTO favorite_records (user_id, item_id) VALUES ('1111', 'abcd')"; hard code
//        String template = "INSERT INTO favorite_records (user_id, item_id) VALUES ("%s, %s")";
//        String sql = String.format(template, userId, item.getId());
//        在sql, ? is 占位符
//        PrepareStatement provided by sql, 有额外保护机制
//        "Select * FROM users WHERE user_id = 1111 OR 1 = 1";where 无限制，return all users
//        String.format不会检查意外可能
//        if not unique, duplicate, 不重复收藏成功就可以，不用抛出异常, IGNORE if the added data is duplicates, 过了就可以了

        saveItem(item);
        String sql = "INSERT IGNORE INTO favorite_records (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
//           library rule: start from 1
            statement.setString(1, userId);
            statement.setString(2, item.getId());
//            执行sql语句，前面都是准备sql语句
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to save favorite item to Database");
        }
    }

    public void unsetFavoriteItem(String userId, String itemId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
//        the only difference
        String sql = "DELETE FROM favorite_records WHERE user_id = ? AND item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete favorite item to Database");
        }
    }
//primary key, check + insert
//    INSERT IGNORE INTO ....,无脑insert
//    optional , if your favorite item was added by other people before
    public void saveItem(Item item) throws MySQLException {
//        database not connected
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getUrl());
            statement.setString(4, item.getThumbnailUrl());
            statement.setString(5, item.getBroadcasterName());
            statement.setString(6, item.getGameId());
            statement.setString(7, item.getType().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to add item to Database");
        }
    }
//    given an user id, see which game has been favorite before
//    item_id has id and game_id
//    function 1
//    item is video or stream channel name, game is game
//    based on user_id, get favorite_item_id
    public Set<String> getFavoriteItemIds(String userId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
//store result
        Set<String> favoriteItems = new HashSet<>();
//        select which column of data
//        # cols is decided by how many thigs are selected
//        # of rows are decided by how many records are satisfied your requirement
//        rs point to index -1, like iterator
//        item_id is the only column
        String sql = "SELECT item_id FROM favorite_records WHERE user_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
//            iterate
//            jump up next and see if it has next, 2 operations
//            start with index -1
            while (rs.next()) {
//                parameter is col name
//                if lots of column, orm
//                map database and java object, use orm, do not need to repeated use getString()
                String itemId = rs.getString("item_id");
                favoriteItems.add(itemId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite item ids from Database");
        }

        return favoriteItems;
    }
//key is video, clip, stream
//    all the information about favorite, everything
//    function 1 and 3 is useful to recommendation, do not recommend favorite, function 1
//    extrapolate what you may like based on game, function 3

//user-> item_id-> other information
//    key is item type, value is the list composed of item object
     public Map<String, List<Item>> getFavoriteItems(String userId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);
//        * stands for return all the column

        String sql = "SELECT * FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    ItemType itemType = ItemType.valueOf(rs.getString("type"));
//                    initiate item via Builder.build() since Builder is private
                    Item item = new Item.Builder().id(rs.getString("id")).title(rs.getString("title"))
                            .url(rs.getString("url")).thumbnailUrl(rs.getString("thumbnail_url"))
                            .broadcasterName(rs.getString("broadcaster_name")).gameId(rs.getString("game_id")).type(itemType).build();
                    itemMap.get(rs.getString("type")).add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite items from Database");
        }
        return itemMap;
    }
//similar to getFavoriteItemIds
//    function 3
//    video [list of game id], clip [list of game id], stream [list of game id]
    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        String sql = "SELECT game_id, type FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
//                get item with item_id, 0 or 1
//                at most one, if(rs.next()) get the first, while(rs.next()) get all
                if (rs.next()) {
//                    "clip" -> ItemType.Clip
//                    convert string to ItemType
                    itemMap.get(rs.getString("type")).add(rs.getString("game_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite game ids from Database");
        }
        return itemMap;
    }
//    return name if exist or null
    public String verifyLogin(String userId, String password) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        String name = "";
//        from table
        String sql = "SELECT first_name, last_name FROM users WHERE id = ? AND password = ?";
        try {
//            format is easy
//            sql injection, prevent malicious input
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
//            only one line fit the requirement
//            while is also fine

            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to verify user id and password from Database");
        }
        return name;
    }
//add successfully is true else is false
    public boolean addUser(User user) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
//sql return 1 or 0
        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, user.getUserId());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
//judge if insert 1 line
//  return 0 if not added (duplicated)
//  executeUpdate() return # of rows added
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get user information from Database");
        }
    }
}
