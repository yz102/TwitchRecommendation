package com.laioffer.jupiter.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class TwitchClient {
    //constant
    //resources建立一个文件夹，把token和client Id放里面，更保险
    private static final String TOKEN = "Bearer v5ozlmri3xhc9h5z2ivdbqdknhvx9c";
    private static final String CLIENT_ID = "f0jd1lsy6edm1vki5uco0gteb9bmfy";
    //%s占位
    private static final String TOP_GAME_URL_TEMPLATE = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;

    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    //搜索limit
    private static final int DEFAULT_SEARCH_LIMIT = 20;
// convert %s to the real destination
    //step 1: get the url
    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            return String.format(url, limit);
        } else {
            try {
                //encode: 空格
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //the first parameter: url is template
            //replace %s with the real game name
            return String.format(url, gameName);
        }
    }
    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }
//step 2: based on url, send request to Twitch, return response as json string
    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }
            //如果entity == null, throw Exception
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get result from Twitch API");
            }
            //data key所对应的array有用，即返回json格式的某一段数据
            //把response body的整体内容变成json object
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            //key is data
            return obj.getJSONArray("data").toString();
        };

        try {

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            //return response body
            return httpclient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            //变成TwitchException
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            //close 也得try catch
            //close http 的connection or 和数据库的链接， 咋处理

            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
                //链接断开出错，自己handle
            }
        }
    }
//response body is input
    //step 3: convert data from json to Java object
    private List<Game> getGameList(String data) throws TwitchException {
        //convertion, json 格式的string convert成某个java object
        //json 格式的string convert成某个array
        ObjectMapper mapper = new ObjectMapper();
        try {
            //Game class里面的annotation
            //if 还有个price, 会有json exception
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameURL(TOP_GAME_URL_TEMPLATE, "", limit);
        String responseBody = searchTwitch(url);
        return getGameList(responseBody);
        //return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL_TEMPLATE, "", limit)));
    }

    public Game searchGame(String gameName) throws TwitchException {
        //buildGameURL: 得到一个url
        //searchTwitch: 给url，返回相对应的response body, data所对应的value
        //getGameList: json convert to Java object via jackson

        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }


//convert Twitch returned data to a list of Item objects.
    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }
    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }
    //only call this method, and then change parameters
    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }
//searchItems: return all
    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }
}

//    example 1: old version
//Create a default http client
//    CloseableHttpClient httpclient = HttpClients.createDefault();
//定义一个get request, parameter is 发给谁
//    HttpGet httpGet = new HttpGet("http://targethost/homepage");
//return result, how to send request, 用.execute()
//response1: status + header + body, including all http response
//发送get请求，得到response
//    CloseableHttpResponse response1 = httpclient.execute(httpGet);

//通过java角度读取HTTP response
//try {
            //get status, first line, print
//        System.out.println(response1.getStatusLine());
            //get response body
//        HttpEntity entity1 = response1.getEntity();
//         下面两行是 读取response body过程
//        // do something useful with the response body
//        // and ensure it is fully consumed
//          consume(HttpEntity entity): Ensures that the entity content is fully consumed and the content stream, if exists, is closed.
        //response body 读完了
//        EntityUtils.consume(entity1);
//        } finally {
//        response1.close();
//        }
//
//        HttpPost httpPost = new HttpPost("http://targethost/login");
//        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//        nvps.add(new BasicNameValuePair("username", "vip"));
//        nvps.add(new BasicNameValuePair("password", "secret"));
//        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//if想复用httpclient，要确保前一个读完
//        CloseableHttpResponse response2 = httpclient.execute(httpPost);
//
//        try {

//        System.out.println(response2.getStatusLine());
//        HttpEntity entity2 = response2.getEntity();
//
//        // do something useful with the response body
//        // and ensure it is fully consumed
//        EntityUtils.consume(entity2);
//        } finally {
//        response2.close();
//        }


//example 2
//closeableHttpClient httpclient = HttpClients.createDefault();
//
//try {
//定义http get请求
//        HttpGet httpget = new HttpGet("http://httpbin.org/");
//        System.out.println("Executing request " + httpget.getRequestLine());
//
//        // Create a custom response handler
//          Handler that encapsulates the process of generating a response object from a HttpResponse.
//        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
//
//@Override
//public String handleResponse(
//final HttpResponse response) throws ClientProtocolException, IOException {
//      ClientProtocolException - in case of an http protocol error
//      IOException - in case of a problem or the connection was aborted
//        int status = response.getStatusLine().getStatusCode();
//        if (status >= 200 && status < 300) {
//        HttpEntity entity = response.getEntity();
//
//        return entity != null ? EntityUtils.toString(entity) : null;
//        } else {
//        throw new ClientProtocolException("Unexpected response status: " + status);
//        }
//        }
//
//        };
//          发送get请求，得到response
//        //example 1: CloseableHttpResponse response1 = httpclient.execute(httpGet);
//responseHandler提供的功能： 拿到response之后， 得到body
//execute不一定有东西返回
//        String responseBody = httpclient.execute(httpget, responseHandler);
//        System.out.println("----------------------------------------");
//        System.out.println(responseBody);
//        } finally {
//        httpclient.close();
//        }


//responseHandler可能还有一个consume()
// Class MyResoonseHandler extends RensponseHandler {
//@Override
//public String ****(){}
//}
//匿名类的父类是response handler
//entity is body ???