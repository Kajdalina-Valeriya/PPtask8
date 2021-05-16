package bsu.rfe.java.group7.lab8.kaydalina.var7A.servlet;

import bsu.rfe.java.group7.lab8.kaydalina.var7A.entity.ChatMessage;
import bsu.rfe.java.group7.lab8.kaydalina.var7A.entity.ChatUser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.HashMap;


public class ChatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // Карта текущих пользователей
    protected HashMap<String, ChatUser> activeUsers;
    // Список сообщений чата
    protected ArrayList<ChatMessage> messages;
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
// Вызвать унаследованную от HttpServlet версию init()
        super.init();
// Извлечь из контекста карту пользователей и список сообщений
        activeUsers = (HashMap<String, ChatUser>)
                getServletContext().getAttribute("activeUsers");
        messages = (ArrayList<ChatMessage>)
                getServletContext().getAttribute("messages");
// Если карта пользователей не определена ...
        if (activeUsers==null) {
// Создать новую карту
            activeUsers = new HashMap<String, ChatUser>();
// Поместить ее в контекст сервлета,
// чтобы другие сервлеты могли до него добраться
            getServletContext().setAttribute("activeUsers",
                    activeUsers);
        }
// Если список сообщений не определен ...
        if (messages==null) {
// Создать новый список
            messages = new ArrayList<ChatMessage>(100);
// Поместить его в контекст сервлета,
// чтобы другие сервлеты могли до него добраться
            getServletContext().setAttribute("messages", messages);
        }
    }
}