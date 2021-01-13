package com.laioffer.jupiter.db;
//servlet处理简单，都包装成一个exception
public class MySQLException extends RuntimeException {
    public MySQLException(String errorMessage) {
//      调用父类的constructor
        super(errorMessage);
    }
}

