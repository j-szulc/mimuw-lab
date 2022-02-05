package com.rtbhouse;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;

public class AsyncWorker implements Runnable {
    private static final ThreadLocalRandom rand = ThreadLocalRandom.current();
    private static LongAdder inFlight = new LongAdder();
    private static ScheduledExecutorService meteringExecutor = Executors.newSingleThreadScheduledExecutor();
    private static Histogram inFlightHistogram = new Histogram(new SlidingTimeWindowArrayReservoir(5, TimeUnit.SECONDS));

    static {
        meteringExecutor.scheduleAtFixedRate(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     inFlightHistogram.update(inFlight.longValue());
                                                 }
                                             }, 0, 50, TimeUnit.MILLISECONDS);
    }

    private AsyncContext asyncContext;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public AsyncWorker(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
        request = (HttpServletRequest) asyncContext.getRequest();
        response = (HttpServletResponse) asyncContext.getResponse();
    }

    public void run() {
        inFlight.increment();

        try {
            Thread.sleep(inFlight.longValue() * 5);
        } catch(InterruptedException e) {}

        response.setContentType("application/json");

        try {
            response.getWriter().println("{ \"status\": \"ok\"}");
        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println(inFlightHistogram.getSnapshot().getMean());

        asyncContext.complete();

        inFlight.decrement();
    }
}
