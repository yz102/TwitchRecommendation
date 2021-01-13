package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.Item;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
//this is a class not a servlet
public class ServletUtil {
    public static void writeItemMap(HttpServletResponse response, Map<String, List<Item>> itemMap) throws IOException {
//        item map -> response body
//        response body 按json格式解析
//        charset=UTF-8" 字符集，包括中文韩文拉丁文,为了解析特殊字符
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new ObjectMapper().writeValueAsString(itemMap));
    }

    public static String encryptPassword(String userId, String password) throws IOException {
//  DigestUtils is class
//        md5Hex encrypt, only one direction, not easy to decrypt
//        parameter can also add firstName and last name as well
//        frontend and backend can both do this
//        list all the possibility and could crack the password
//        two levels of encrypt, encrypt (userId + encrypted(password))
        return DigestUtils.md5Hex(userId + DigestUtils.md5Hex(password)).toLowerCase();
    }
//    generics
//    according to caller, decide what type it is
//    T could be any type: loginRequestBody/favoriteRequestBody/registerRequestBody
    public static <T> T readRequestBody(Class<T> cls, HttpServletRequest request) throws IOException {
//        request body -> java object
        ObjectMapper mapper = new ObjectMapper();
        try {
//            cls is convert to what type of java class, cls stands for class
//            json -> java
            return mapper.readValue(request.getReader(), cls);
        } catch (JsonParseException | JsonMappingException e) {
            return null;
        }
    }
}