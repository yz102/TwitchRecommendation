package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//if 某个key不认识，不要抛Exception
@JsonIgnoreProperties(ignoreUnknown = true)
//if 某个对象is null, 不convert只convert有意义的数据
@JsonInclude(JsonInclude.Include.NON_NULL)
//用builder pattern
//String -> Game,用builder class，不用Game的constructor
@JsonDeserialize(builder = Game.Builder.class)
public class Game {
    //game object convert to json
    //是json的key
    //annotation
    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("box_art_url")
    private final String boxArtUrl;

    //private constructor
    private Game(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.boxArtUrl = builder.boxArtUrl;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    //builder pattern
    public static class Builder {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("box_art_url")
        private String boxArtUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder boxArtUrl(String boxArtUrl) {
            this.boxArtUrl = boxArtUrl;
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }
}

//    //property, 可以加or不加access modifier
//    //if not adding, by default is package private
//    private String name;
//    //if name is final, 不可以setter， builder.setNmae()还可以执行
//    //通过builder set好
//    //如果全是final，为什么builder里还set?????
//    //set的是Gamebuilder的property，然后赋值给Game class的property
//    private String developer;
//    private String releaseDate;
//    private float price;
//
//
//    public Game() {}
//    //if constructor is private
//    //private construct 还可以call, 因为在class内，
//    //public vs. private, private is better
//    //public 可以绕过builder
//    //Game game = new Game(builder);不要给这个，所以constructor给private， 不让user用这个方法，and GameBuilder should be put in the Game class
//    //Game game = builder.build();通过builder
//
//    public Game(GameBuilder builder) {
//        this.name = builder.name;
//        this.developer = builder.developer;
//        this.releaseDate = builder.releaseDate;
//        this.price = builder.price;
//
//    }
//
//    public Game(String name, String developer) {
//        this.name = name;
//        this.developer = developer;
//    }
//    //constructor有多种写法
//    //if 没有constructor，默认值0 false null
//
//    public String getName() {
//        return name;
//    }
//
////    public Game setName(String name) {
////        if (name == "vincent") {
////            System.out.println("Name cannot be vincent");
////            return this;
////        }
////        this.name = name;
////        return this;
////        //谁调用这个方法，返回谁
////        //game1.setName(); return game1
////        //game2.setName(); return game2
////    }
//
//    public String getDeveloper() {
//        return developer;
//    }
//
//    public void setDeveloper(String developer) {
//        this.developer = developer;
//    }
//
//    public float getPrice() {
//        return price;
//    }
//
//    public void setPrice(float price) {
//        this.price = price;
//    }
//
//    public static class GameBuilder {
//        private String name;
//        private String developer;
//        private String releaseDate;
//        private float price;
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public void setDeveloper(String developer) {
//            this.developer = developer;
//        }
//
//        public void setReleaseDate(String releaseDate) {
//            this.releaseDate = releaseDate;
//        }
//
//        public void setPrice(float price) {
//            this.price = price;
//        }
//
//        public Game build() {
//            return new Game(this);
//            //把builder的propetry赋值给Game里的property
//        }
//    }

//creat game object
//Game game = new Game(); referenced type 存heap
//int a = 10; primitive type 存stack\
//public private, no modifier, protected
// game.setName("dd").getDeveloper("ad").setPrice(10);
//if 想写成一串


//public setName 和public Name
//从程序实现角度和功能：
//game.setName("Vincent");
//if 有复杂功能， 可以检查input is valid,可以有复杂逻辑， 一般用private property, public setter
//game.name = "Vincent";

//if do not want to change, add final


//if there are lots of fields
//不用写所有constructor的排列组合， builder pattern

//GameBuilder builder = new GameBuidler();
//builder.setName("vincent");
//builder.setDeveloper("laioffer");
//Game game = builder.build():
//高端,灵活

//Game game = new Game();
//game.setName("vincent");

//builder pattern 在constructor比较popular
//GameBuilder is inner class
//得先有game对象


//static inner class怎么创建object
//import Game.GameBuilder;
//Game.GameBuilder.builder = new GameBuilder();

//or Game.GameBuilder.builder = new Game.GameBuilder();



