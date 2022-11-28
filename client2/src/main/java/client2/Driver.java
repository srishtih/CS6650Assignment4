package client2;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * @author srish
 * Contains the driver code for client description provided in assignment's part 2.
 * Client send 200K post requests to the server deployed on EC2  instance
 * In first phase, 32 threads are created that send 1000 requests each, the rest
 * are sent by the newly created n = THREAD_COUNT threads that terminate after 200K
 * requests are sent.
 * For every response sent, the responses are captured and stored in a queue shared by all threads
 * for insights about the response time.
 *
 */

public class Driver {
    protected static final AtomicInteger SUCCESSFUL_REQUESTS = new AtomicInteger(0);
    protected static final AtomicInteger FAILED_REQUESTS = new AtomicInteger(0);
    protected static final int REQUEST_COUNT = 200000;
    protected  static final int INITIAL_THREAD_COUNT = 32;
    protected static final int THREAD_COUNT= 200;
    protected static final int MINIMUM_THREAD_COMPLETION = 1;
    protected static CountDownLatch mainLatch = new CountDownLatch(MINIMUM_THREAD_COMPLETION);
    protected static CountDownLatch secondLatch = new CountDownLatch(THREAD_COUNT);
    protected static final double AVG_RESPONSE_TIME = 25.2; //Obtained by sending 10K requests on single thread(time taken/10,000)
    static File file;

    public static void main(String[] args) throws InterruptedException, IOException {
       String serverAddress = "http://34.219.86.200:8080/server_war"; //Address of remote server

       EventBuffer events = new EventBuffer();      // Shared buffer that contains all lift ride events and payloads
       LiftRideEventProducer producer = new LiftRideEventProducer(events, REQUEST_COUNT); //Producer thread for events
       Thread producerThread = new Thread(producer);
       producerThread.start();
       Thread.sleep(1000);

       ResponseBuffer responses = new ResponseBuffer(); //Shared buffer that holds all POST request responses

        // Start sending requests to remote server from clients running on threads
        Timestamp start = new Timestamp(System.currentTimeMillis());
        for(int i = 0; i< INITIAL_THREAD_COUNT; i++){
           WorkerThread wThread = new WorkerThread(mainLatch, serverAddress, events, responses, true);
           Thread t = new Thread(wThread);
           t.start();
        }
       mainLatch.await();   //Completion of Phase 1; 32 threads sent 1K request each
       // Beginning of Phase 2; Number of threads here is THREAD_COUNT
       for(int i = 0; i < THREAD_COUNT; i++){
           WorkerThread wThread = new WorkerThread(secondLatch, serverAddress, events, responses, false);
           Thread t = new Thread(wThread);
           t.start();
       }
       secondLatch.await();
       // Completed Phase 2; all 200K requests now sent
       Timestamp end = new Timestamp(System.currentTimeMillis());
       long wallTime = (end.getTime() - start.getTime());
       //Print wall time, observed throughput and expected throughput
        System.out.println("Successful Requests: "+ SUCCESSFUL_REQUESTS.get() +
                "\nFailed Requests: " + FAILED_REQUESTS.get() +
                "\nWall time(in milliseconds): " + wallTime +
                "\nThroughput observed: " + ((float)REQUEST_COUNT/wallTime*1000) +" requests/sec" +
                "\nThroughput expected: " +((0.16*INITIAL_THREAD_COUNT+ 0.84*THREAD_COUNT)/AVG_RESPONSE_TIME) *1000 +
                " requests/sec");
        // Print captured response time metrics: mean, median, min, max and p99
        int stats[] = responses.getLatencyStatistics();
        System.out.println("\nStatistics:" +
                "\n___________" +
                "\nMean(in ms): " + stats[0] +
                "\nMedian(in ms): " + stats[1] +
                "\nMinimum(in ms): " + stats[2] +
                "\nMaximum(in ms): " + stats[3]+
                "\n99th Percentile value(in ms): " + stats[4]);
    }
}

