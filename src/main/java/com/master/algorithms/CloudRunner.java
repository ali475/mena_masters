package com.master.algorithms;

public class CloudAlgorithm {
    private CloudProcess process;

    public CloudAlgorithm(CloudProcess process) {
        this.process = process;
    }
    public void run(){
        // get starting time
        int starting_time = 0;
        process.execute();
        //get end time
        int end_time = 1;
        int total_time = starting_time - end_time;
        System.out.println("execution time is : " + total_time);
    }
}
