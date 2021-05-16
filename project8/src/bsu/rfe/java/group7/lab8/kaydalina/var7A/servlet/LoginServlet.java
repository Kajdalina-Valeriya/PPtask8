package bsu.rfe.java.group7.lab8.kaydalina.var7A.servlet;

import bsu.rfe.java.group7.lab8.kaydalina.var7A.entity.ChatUser;
import bsu.rfe.java.group7.lab8.kaydalina.var7A.entity.ChatMessage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

@WebServlet(name = "LoginServlet",urlPatterns = "/login.do")
public class LoginServlet extends ChatServlet {
    private static final long serialVersionUID = 1L;
    // Длительность сессии, в секундах
    private int sessionTimeout = 10*60;

    public void init() throws ServletException {
        super.init();
// Прочитать из конфигурации значение параметра SESSION_TIMEOUT
        String value =
                getServletConfig().getInitParameter("SESSION_TIMEOUT");
// Если он задан, переопределить длительность сессии по умолчанию
        if (value!=null) {
            sessionTimeout = Integer.parseInt(value);
        }
    }
    // Метод будет вызван при обращении к сервлету HTTP-методом GET
    // т.е. когда пользователь просто открывает адрес в браузере
    protected void doGet(HttpServletRequest request, HttpServletResponse
            response) throws ServletException, IOException {
// Проверить, есть ли уже в сессии заданное имя пользователя?
        String name = (String)request.getSession().getAttribute("name");
// Извлечь из сессии сведения о предыдущей ошибке (возможной)
        String errorMessage =
                (String)request.getSession().getAttribute("error");
// Идентификатор предыдущей сессии изначально пуст
        String previousSessionId = null;
// Если в сессии имя не сохранено, то попытаться
// восстановить имя через cookie
        if (name==null) {// Найти cookie с именем sessionId
            for (Cookie aCookie: request.getCookies()) {
                if (aCookie.getName().equals("sessionId")) {
// Запомнить значение этого cookie –
// это старый идентификатор сессии
                    previousSessionId = aCookie.getValue();
                    break;
                }
            }
            if (previousSessionId!=null) {
// Мы нашли session cookie
// Попытаться найти пользователя с таким sessionId
                for (ChatUser aUser: activeUsers.values()) {
                    if
                    (aUser.getSessionId().equals(previousSessionId)) {
// Мы нашли такого, т.е. восстановили имя
                        name = aUser.getName();
                        aUser.setSessionId(request.getSession().getId());
                    }
                }
            }
        }
// Если в сессии имеется не пустое имя пользователя, то...
        if (name!=null && !"".equals(name)) {
            errorMessage = processLogonAttempt(name, request, response);
        }
// Пользователю необходимо ввести имя. Показать форму
// Задать кодировку HTTP-ответа
        response.setCharacterEncoding("utf8");
// Получить поток вывода для HTTP-ответа
        PrintWriter pw = response.getWriter();
        pw.println("<html><head><title>Мега-чат!</title><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head>");
// Если возникла ошибка - сообщить о ней
        if (errorMessage!=null) {
            pw.println("<p><font color='red'>" + errorMessage +
                    "</font></p>");
        }
// Вывести форму
        pw.println("<form action='/project8_war_exploded/' " +
                "method='post'>Введите имя: <input type='text' name='name' " +
                "value=''><input type='submit' value='Войти в чат'>");
        pw.println("</form></body></html>");
// Сбросить сообщение об ошибке в сессии
        request.getSession().setAttribute("error", null);
    }
    // Метод будет вызван при обращении к сервлету HTTP-методом POST
// т.е. когда пользователь отправляет сервлету данные
    protected void doPost(HttpServletRequest request, HttpServletResponse
            response) throws ServletException, IOException {
// Задать кодировку HTTP-запроса - очень важно!
// Иначе вместо символов будет абракадабра
        request.setCharacterEncoding("UTF-8");
// Извлечь из HTTP-запроса значение параметра 'name'
        String name = (String)request.getParameter("name");
// Полагаем, что изначально ошибок нет
        String errorMessage = null;
        if (name==null || "".equals(name)) {
// Пустое имя недопустимо - сообщить об ошибке
            errorMessage = "Имя пользователя не может быть пустым!";
        } else {
// Если ия не пустое, то попытаться обработать запрос
            errorMessage = processLogonAttempt(name, request, response);}
        if (errorMessage!=null) {
// Сбросить имя пользователя в сессии
            request.getSession().setAttribute("name", null);
// Сохранить в сессии сообщение об ошибке
            request.getSession().setAttribute("error", errorMessage);
// Переадресовать обратно на исходную страницу с формой
            response.sendRedirect(response.encodeRedirectURL("/project8_war_exploded/"));
        }
    }
    // Возвращает текстовое описание возникшей ошибки или null
    String processLogonAttempt(String name, HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
// Определить идентификатор Java-сессии пользователя
        String sessionId = request.getSession().getId();
// Извлечь из списка объект, связанный с этим именем
        ChatUser aUser = activeUsers.get(name);
        if (aUser==null) {
// Если оно свободно, то добавить
// нового пользователя в список активных
            aUser = new ChatUser(name,
                    Calendar.getInstance().getTimeInMillis(), sessionId);
// Так как одновременно выполняются запросы
// от множества пользователей
// то необходима синхронизация на ресурсе
            synchronized (activeUsers) {
                activeUsers.put(aUser.getName(), aUser);
            }
        }
        if (aUser.getSessionId().equals(sessionId) ||
                aUser.getLastInteractionTime()<(Calendar.getInstance().getTimeInMillis()-
                        sessionTimeout*1000)) {
// Если указанное имя принадлежит текущему пользователю,
// либо оно принадлежало кому-то другому, но сессия истекла,
// то одобрить запрос пользователя на это имя
// Обновить имя пользователя в сессии
            request.getSession().setAttribute("name", name);
// Обновить время взаимодействия пользователя с сервером
            aUser.setLastInteractionTime(Calendar.getInstance().getTimeInMillis());
// Обновить идентификатор сессии пользователя в cookies
            Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
// Установить срок годности cookie 1 год
            sessionIdCookie.setMaxAge(60*60*24*365);
// Добавить cookie в HTTP-ответ
            response.addCookie(sessionIdCookie);
// Перейти к главному окну чата
            response.sendRedirect(response.encodeRedirectURL("/project8_war_exploded/view.htm"));
// Вернуть null, т.е. сообщений об ошибках нет
            return null;
        } else {
// Сохранѐнное в сессии имя уже закреплено за кем-то другим.
// Извиниться, отказать и попросить ввести другое имя
            return "Извините, но имя <strong>" + name + "</strong> уже кем-то занято." +
                    " Пожалуйста выберите другое имя!";
        }
    }
}