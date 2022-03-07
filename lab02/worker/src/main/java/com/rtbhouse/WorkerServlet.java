package com.rtbhouse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

public class WorkerServlet extends HttpServlet {
    private static final ThreadPoolExecutor REQUEST_PROCESSING_EXECUTOR = new ThreadPoolExecutor(2, 2, 60000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(128));

    public WorkerServlet() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        AsyncContext asyncContext = request.startAsync(request, response);
        RequestProcessor processor = new RequestProcessor(asyncContext);
        REQUEST_PROCESSING_EXECUTOR.submit(processor::process);
    }
}
