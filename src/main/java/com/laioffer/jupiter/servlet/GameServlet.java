package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;
import org.json.JSONObject;
import org.apache.commons.io.IOUtils;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public class GameServlet extends HttpServlet {
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        //前端request body读出来，convert成string
//        //request.getReader()读request body, InputStream body = request.getReader();
//        //IOUtils.toString是把stream变成一个string, parameter is InputStream
//        //new JSONObject变成json格式的数据
//        JSONObject jsonRequest = new JSONObject(IOUtils.toString(request.getReader()));
          //拿json 的value，用.getString(key)
//        String name = jsonRequest.getString("name");
//        String developer = jsonRequest.getString("developer");
//        String releaseTime = jsonRequest.getString("release_time");
//        String website = jsonRequest.getString("website");
//        float price = jsonRequest.getFloat("price");
//
//        //打印在console里
//        System.out.println("Name is: " + name);
//        System.out.println("Developer is: " + developer);
//        System.out.println("Release time is: " + releaseTime);
//        System.out.println("Website is: " + website);
//        System.out.println("Price is: " + price);
//
//        //返回一个json格式的status ok
//        response.setContentType("application/json");
//        JSONObject jsonResponse = new JSONObject();
//        jsonResponse.put("status", "ok");
//        response.getWriter().print(jsonResponse);
//    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //判断请求参数是否有gameName
        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();

        response.setContentType("application/json;charset=UTF-8");
        try {
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGame(gameName)));
            } else {
                //0 is default 20
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }

//        //.getParameter() 只能读link里的参数， body里的读不了
//        String gameName = request.getParameter("gamename");
//        response.getWriter().print("Game is: " + gameName);

//        //HashMap,then convert to String
//        //map的排列顺序和input顺序无关
//        //if use json, do not use index!!!! can only use key name to access, similar to hashmap
//        JSONObject game = new JSONObject();
//        //object 加东西
//        //清楚生成json格式数据
//        game.put("name", "World of Warcraft");
//        game.put("developer", "Blizzard Entertainment");
//        game.put("release_time", "Feb 11, 2005");
//        game.put("website", "https://www.worldofwarcraft.com");
//        game.put("price", 49.99);
//        //明确告诉前端是json格式
//        response.setContentType("application/json");
//        ObjectMapper mapper = new ObjectMapper();
//        Game game = new Game("World of Warcraft", "Blizzard Entertainment", "Feb 11, 2005", "https://www.worldofwarcraft.com", 49.99);
//        //jackson library
//        //convert Game object to json format
//        //response.getWriter(): 往response body里面写
//        response.getWriter().print(mapper.writeValueAsString(game));
        //response.getWriter().print(object.toString());等价于下面一句
        //response.getWriter().print(game);
    }
}
