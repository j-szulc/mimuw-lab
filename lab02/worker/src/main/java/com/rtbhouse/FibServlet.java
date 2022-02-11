package com.rtbhouse;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FibServlet extends HttpServlet {
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(16, 16,
            50000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(500));

    public FibServlet() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        AsyncContext async = request.startAsync(request, response);
        EXECUTOR.execute(new FibAsync(async));
    }
}
