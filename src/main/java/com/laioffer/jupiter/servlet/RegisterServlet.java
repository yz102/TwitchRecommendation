package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
//request body read content, mapper
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       // ObjectMapper mapper = new ObjectMapper();
//       stream of request body
       // User user = mapper.readValue(request.getReader(), User.class);
//        userId and password are authenticated by frontend
        User user = ServletUtil.readRequestBody(User.class, request);

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean isUserAdded = false;
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
//            encrypt password
            user.setPassword(ServletUtil.encryptPassword(user.getUserId(), user.getPassword()));
            isUserAdded = connection.addUser(user);
        } catch (MySQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
//if not succeed. may duplicated, return 409 (SC_CONFLICT)
        if (!isUserAdded) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
//read request body , store in database
