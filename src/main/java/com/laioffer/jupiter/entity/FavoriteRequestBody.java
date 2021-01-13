package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FavoriteRequestBody {
//    @JsonProperty("favorite")
//    put it here, you can also convert favoriteRequestBody to json
//    we only need to convert json to java object, this is request body, do not need to return to front end
    private final Item favoriteItem;

//    apply to constructor, json -> favoriteRequestBody object, you @JsonCreator mark de constructor
//    if want to new favoriteRequestBody or convert json to
//    favorite key de value is Item favoriteItem
    @JsonCreator
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }
}
