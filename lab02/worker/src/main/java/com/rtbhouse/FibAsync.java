package com.rtbhouse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FibAsync implements Runnable {
    private final AsyncContext asyncContext;

    public FibAsync(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    public void run() {
        HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();

        int n = Integer.parseInt(request.getParameter("n"));
        int fib = fib(n);

        try {
            PrintWriter responseWriter = response.getWriter();
            responseWriter.println(fib);
            responseWriter.flush();
        } catch (IOException e) {
            System.out.println(e);
        }

        asyncContext.complete();
    }

    public int fib(int n) {
        if (n == 0) return 1;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }
}
