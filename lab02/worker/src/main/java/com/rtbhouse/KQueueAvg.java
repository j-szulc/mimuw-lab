package com.rtbhouse;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class KQueueAvg {
    private static final Queue<Integer> samples = new LinkedList<>();
    private int samplesSum = 0;
    private int samplesCount = 0;
    private final int k;

    public KQueueAvg(int k){
        this.k = k;
    }

    public void addSample(int sample){
        samples.add(sample);
        samplesCount++;
        samplesSum+=sample;

        if(samplesCount>k){
            samplesSum -= samples.remove();
            samplesCount--;
        }

        System.out.println("Added sample "+sample);
    }

    public Optional<Float> getAvg(){
        if(samplesCount == 0)
            return Optional.empty();
        return Optional.of(((float) samplesSum)/((float) samplesCount));
    }




}
