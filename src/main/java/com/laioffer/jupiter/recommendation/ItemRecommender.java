package com.laioffer.jupiter.recommendation;

import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
//function 1 and 2 is function 3 and 4 's helper
public class ItemRecommender {
//    3 constants
//    if do not have favorite, recommend top 3
//    how many games to recommend per game,每个game最多推荐多少个
//    how many games recommended totally，最多推荐多少个
//    出现次数多，推荐多
    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;
//if do not have favorite or not login
    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) throws RecommendationException {
//        return obj
        List<Item> recommendedItems = new ArrayList<>();
//        call topGame
        TwitchClient client = new TwitchClient();
//if have type: search by type
//        if no type: search item
        outerloop:
        for (Game game : topGames) {
            List<Item> items;
            try {
                items = client.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop;
                }
                recommendedItems.add(item);
            }
        }
//        item对象所组成的list
        return recommendedItems;
    }
//if have favorite
//    favoriteGameIds -> [1234, 1234, 2345], duplicate, recommend tid at higher frequency
//    {2345 : 1, 1234 : 2}, map and sort based on frequency
//    list -> hashmap / treemap
    private List<Item> recommendByFavoriteHistory(
            Set<String> favoritedItemIds, List<String> favoriteGameIds, ItemType type) throws RecommendationException {
//        groupBy:把相同的元素变成一个组, value is Collectors.counting() determined
//        groupBy可以按任意方法group
//        1st parameter: what is the key, str -> str, lambda, return str, 输入什么就输出啥
//        2nd parameter: 按什么方法group
//        favoriteGameIds -> [game, game, game]
//        1234 -> 0.5
//        1234 -> 0.8, if want sum 1234 -> 1.3
//        parallelStream()并行
        Map<String, Long> favoriteGameIdByCount = favoriteGameIds.parallelStream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
//convert to list and sort based on list built in method
        List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount = new ArrayList<>(
                favoriteGameIdByCount.entrySet());
//        sort based on frequency
        sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) -> Long
                .compare(e2.getValue(), e1.getValue()));

        if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
            sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
        }

        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        outerloop:
        for (Map.Entry<String, Long> favoriteGame : sortedFavoriteGameIdListByCount) {
            List<Item> items;
            try {
                items = client.searchByType(favoriteGame.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }

            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop;
                }
//                do not recommend games that users favorite before
                if (!favoritedItemIds.contains(item.getId())) {
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }

    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
//        [aaaa, bbbb, cccc]
        Set<String> favoriteItemIds;
//        {"video": ["1234, 2345, 3456"], "stream": ["2345"], "clip": []}
//        clip: get by top
//        recommend games in each type respectively
        Map<String, List<String>> favoriteGameIds;
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            favoriteItemIds = connection.getFavoriteItemIds(userId);
            favoriteGameIds = connection.getFavoriteGameIds(favoriteItemIds);
        } catch (MySQLException e) {
            throw new RecommendationException("Failed to get user favorite history for recommendation");
        } finally {
            connection.close();
        }

        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            if (entry.getValue().size() == 0) {
                TwitchClient client = new TwitchClient();
                List<Game> topGames;
                try {
                    topGames = client.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }
                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
            } else {
                recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
            }
        }
        return recommendedItemMap;
    }
// if not login
//    Map<type, list of item in that type>
    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        TwitchClient client = new TwitchClient();
        List<Game> topGames;
        try {
            topGames = client.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        for (ItemType type : ItemType.values()) {
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }
        return recommendedItemMap;
    }
}