package com.rtbhouse;

import java.io.IOException;
import java.io.PrintWriter;

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
        long fib = fib(n);

        try {
            PrintWriter responseWriter = response.getWriter();
            responseWriter.println(fib);
            responseWriter.flush();
        } catch (IOException e) {
            System.out.println(e);
        }

        asyncContext.complete();
    }

    private long fib(int n) {
        long fibCur = 1;
        long fibPrev = 1;

        for (int i = 0; i < n; i++) {
            long fibNext = fibCur + fibPrev;
            fibPrev = fibCur;
            fibCur = fibNext;
        }

        return fibCur;
    }
}
