package com.innovitech.usermanager.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/user.xhtml")
public class RequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null) && session.getAttribute("token") != null;
        String loginURL = request.getContextPath() + "/index.xhtml";

        if (!loggedIn) {
            response.sendRedirect(loginURL);
        } else {
            chain.doFilter(request, response);
        }
    }
}
