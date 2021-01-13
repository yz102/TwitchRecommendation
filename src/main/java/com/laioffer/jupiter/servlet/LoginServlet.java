package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);

        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String username;
// set up connection
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            String userId = body.getUserId();
// compare the encrypted password, so login also need to encrypt password
            String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
            username = connection.verifyLogin(userId, password);
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            connection.close();
        }
//new a session and return to frontend
//        if verify login fails, user name is empty, return 401, SC_UNAUTHORIZED
//        else
        if (!username.isEmpty()) {
//        get a session
//        login means do not login successfully before, get the session is exist or creat new one
//        in our case: create a new session
//        attributes are not encrypted but is fine since they are not returned to frontend
            HttpSession session = request.getSession();
            session.setAttribute("user_id", body.getUserId());
            session.setMaxInactiveInterval(600);
//constructor need 2 parameters: userid + username
            LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), username);
//return to frontend
            response.setContentType("application/json;charset=UTF-8");
            ObjectMapper mapper = new ObjectMapper();

            mapper.writeValue(response.getWriter(), loginResponseBody);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
//session is created  automatically when session is created
//tomcat accomplished bundling session id and add it to header and bundle session to response body
//tomcat 9.0 session management