package com.laioffer.jupiter.recommendation;
//based on twitch and mysql,so it may have exception
//twitch and mysql: both could have exception

public class RecommendationException extends RuntimeException {
    public RecommendationException(String errorMessage) {
        super(errorMessage);
    }
}
