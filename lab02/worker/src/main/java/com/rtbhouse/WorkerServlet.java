package com.rtbhouse;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkerServlet extends HttpServlet {
    public WorkerServlet() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        RequestProcessor processor = new RequestProcessor();
        processor.process(request, response);
    }
}
