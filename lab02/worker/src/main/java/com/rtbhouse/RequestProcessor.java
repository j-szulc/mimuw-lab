package com.rtbhouse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestProcessor {
    private AsyncContext asyncContext;

    public RequestProcessor(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    public void process() {
        HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();

        int n = Integer.parseInt(request.getParameter("n"));
        double result = doWork(n);

        try {
            PrintWriter responseWriter = response.getWriter();
            responseWriter.println(result);
            responseWriter.flush();
        } catch (IOException e) {
            System.out.println(e);
        }

        asyncContext.complete();
    }

    private double doWork(int n) {
        double result = Math.sqrt(ThreadLocalRandom.current().nextDouble());

        for (int i = 0; i < n; i++) {
            double rand = ThreadLocalRandom.current().nextDouble();
            result = (result + Math.sqrt(rand)) / 2;
        }

        return result;
    }
}
