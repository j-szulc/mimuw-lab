package com.rtbhouse;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HealthServlet extends HttpServlet {
    public static Boolean healthy = Boolean.TRUE;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getParameter("toggle") != null) {
                healthy = healthy == Boolean.TRUE ? Boolean.FALSE : Boolean.TRUE;
            }

            if (healthy) {
                response.getWriter().println("HEALTHY");
            } else {
                response.sendError(500, "UNHEALTHY");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
