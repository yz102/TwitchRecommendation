package com.laioffer.jupiter.external;

public class TwitchException extends RuntimeException {
    public TwitchException(String errorMessage) {
        //调用父类的constructor！！！
        super(errorMessage);
    }
}

//把所有Exception包裹一层，变成TwitchException