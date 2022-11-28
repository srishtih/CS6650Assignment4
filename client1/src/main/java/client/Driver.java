package client;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * @author srish
 * Contains the driver code for client description provided in assignment's part 1.
 * Client send 200K post requests to the server deployed on EC2  instance
 * In first phase, 32 threads are created that send 1000 requests each, the rest
 * are sent by the newly created n = THREAD_COUNT threads that terminate after 200K
 * requests are sent.
 *
 */

public class Driver {
    protected static final AtomicInteger SUCCESSFUL_REQUESTS = new AtomicInteger(0); //atomic since is shared by all threads
    protected static final AtomicInteger FAILED_REQUESTS = new AtomicInteger(0); //atomic since is shared by all threads
    protected static final int REQUEST_COUNT = 10000;
    protected  static final int INITIAL_THREAD_COUNT = 32;
    protected static final int THREAD_COUNT= 192;
    protected static final int MINIMUM_THREAD_COMPLETION = 1;
    //free execution to move on to next stage after first thread finishes sending its 1K requests
    protected static CountDownLatch mainLatch = new CountDownLatch(MINIMUM_THREAD_COMPLETION);
    protected static CountDownLatch secondLatch = new CountDownLatch(THREAD_COUNT);
    protected static final double AVG_RESPONSE_TIME = 25.0; //Obtained by sending 10K requests on single thread(time taken/10,000)

    public static void main(String[] args) throws InterruptedException {
       String serverAddress = "http://52.43.193.145:8080/server_war2/"; //Address of remote server

       EventBuffer events = new EventBuffer();  // Shared buffer that contains all lift ride events
       LiftRideEventProducer producer = new LiftRideEventProducer(events, REQUEST_COUNT);   // Creation of lift ride event
       Thread producerThread = new Thread(producer);
       producerThread.start();
       Thread.sleep(1000);

       // Start sending requests to remote server from clients running on threads
       Timestamp start = new Timestamp(System.currentTimeMillis());
       //Beginning of Phase 1
       for(int i = 0; i< INITIAL_THREAD_COUNT; i++){
           WorkerThread wThread = new WorkerThread(mainLatch, serverAddress, events, true);
           Thread t = new Thread(wThread);
           t.start();
       }
       mainLatch.await();   // Completion of Phase 1; 32 threads sent 1K request each
       // Beginning of Phase 2; Number of threads here is THREAD_COUNT
       for(int i = 0; i < THREAD_COUNT; i++){
           WorkerThread wThread = new WorkerThread(secondLatch, serverAddress, events, false);
           Thread t = new Thread(wThread);
           t.start();
       }
       secondLatch.await();
       // Completed Phase 2; all 200K requests now sent
       Timestamp end = new Timestamp(System.currentTimeMillis());
       long wallTime = (end.getTime() - start.getTime());

        System.out.println("Successful Requests: "+ SUCCESSFUL_REQUESTS.get() +
                "\nFailed Requests: " + FAILED_REQUESTS.get() +
                "\nWall time(in milliseconds): " + wallTime +
                "\nThroughput observed: " + ((float)REQUEST_COUNT/wallTime*1000) +" requests/sec" +
                "\nThroughput expected: " +((0.16*INITIAL_THREAD_COUNT+ 0.84*THREAD_COUNT)/AVG_RESPONSE_TIME) *1000 +
                " requests/sec");
    }
}

