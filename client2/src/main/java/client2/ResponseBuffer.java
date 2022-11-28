package client2;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.math3.stat.StatUtils;

/**
 * @author srish
 * Buffer class that stores n responses received by all client threads when
 * n POST requests have been sent to remote server
 * The responses are stored as response objects in a concurrentLinkedQueue.
 */
public class ResponseBuffer {

    private ConcurrentLinkedQueue responses = new ConcurrentLinkedQueue<>(); //queue that holds all response objects


    /**
     * Getter for Responses Queue
     * @return queue containing responses
     */
    public ConcurrentLinkedQueue getResponses() {
        return responses;
    }

    /**
     * Returns readable information about object of this instance
     * @return String containing information about the number of responses received
     */
    @Override
    public String toString() {
        return responses.size() + " responses have been recorded";
    }


    /**
     * Adds response received by the client as an instance of Response class
     * @param start Timestamp taken before the request is sent
     * @param end   Timestamp taken after the response is received
     * @param responseCode response status code received in the response
     */
    public synchronized void put(Timestamp start, Timestamp end, int responseCode) {
        responses.add(new Response(start, end, responseCode));
    }

    /**
     * Calculates metrics related to the latency such as mean, median and p99
     * @return Array containing calculated latency metrics
     */
    public int[] getLatencyStatistics(){
        double[] latencyList = new double[responses.size()];
        int i = 0;
        //To analyse the latency for the required metrics, we calculate latencies for all responses and store is as an array
        for(Object object: responses){
            Response response = (Response)object;
            latencyList[i] = Double.valueOf(response.getLatency());
            i++;
        }
        //The created latency array is used to calculate mean, median, min, max and p99 using StatUtils function
        int mean = (int) StatUtils.mean(latencyList);
        int median = (int)StatUtils.percentile(latencyList,50); //Since median is 50th percentile
        int minimum = (int)StatUtils.min(latencyList);
        int maximum = (int)StatUtils.max(latencyList);
        int p99 = (int)StatUtils.percentile(latencyList, 99);
        int[] stats = new int[]{ mean, median, minimum, maximum, p99};

        return stats;
        }

}


