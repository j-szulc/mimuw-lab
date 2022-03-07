package com.rtbhouse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestProcessor {
    private static final ThreadPoolExecutor REQUEST_PROCESSING_EXECUTOR = new ThreadPoolExecutor(2, 2, 60000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(16384));
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final KQueueAvg samplesQ = new KQueueAvg(10);

    static {
        scheduler.scheduleAtFixedRate(()->samplesQ.addSample(REQUEST_PROCESSING_EXECUTOR.getActiveCount()), 1, 1, TimeUnit.SECONDS);
    }

    private AsyncContext asyncContext;



    public RequestProcessor() {
    }

    public void process(HttpServletRequest request, HttpServletResponse response) {
        AsyncContext asyncContext = request.startAsync(request, response);

        if(samplesQ.getAvg().orElse(0f) > REQUEST_PROCESSING_EXECUTOR.getPoolSize()*0.9){
            System.out.println("Dropped!");
            asyncContext.complete();
        }

        REQUEST_PROCESSING_EXECUTOR.submit(() -> this.processAsync(asyncContext));
    }

    private void processAsync(AsyncContext asyncContext) {
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
