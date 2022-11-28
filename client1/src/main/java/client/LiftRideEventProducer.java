package client;

import org.json.simple.JSONObject;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author srish
 * Producer thread implementation that generates specified number of ski likt ride events for client threads consumption
 */

public class LiftRideEventProducer implements Runnable {
    //Specifying value ranges for different parameters
    private static final int MINIMUM_ID = 1;
    private static final int LAST_RESORT_ID = 10;
    private static final int LAST_SKIER_ID = 100000;
    private static final int LAST_DAY_ID = 360;
    private static final int LAST_LIFT_ID = 40;
    private final int requestCount;
    public EventBuffer eventBuffer;

    /**
     * Constructor for LiftRiderEventProducer
     * @param eventBuffer  Buffer that holds the content produced by the producer
     * @param requestCount Number of events nd payloads to be generated
     */
    public LiftRideEventProducer(EventBuffer eventBuffer, int requestCount) {
        this.eventBuffer = eventBuffer;
        this.requestCount = requestCount;
    }


    /**
     * Defines code that is implemented when a thread that implements
     * this producer object is started(i.e.,thread.start())
     */
    public void run() {
        int i = 0;
        while(i < requestCount){
            String url = "/skiers/" + ThreadLocalRandom.current().nextInt(MINIMUM_ID, LAST_RESORT_ID+1)
                    +"/seasons/2022/day/1/skier/" + ThreadLocalRandom.current().nextInt(MINIMUM_ID, LAST_SKIER_ID+1);
            eventBuffer.putEvent(url);  //adding randomly generated event as path params to buffer

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("time", ThreadLocalRandom.current().nextInt(MINIMUM_ID, LAST_DAY_ID));
            bodyJson.put("liftID", ThreadLocalRandom.current().nextInt(MINIMUM_ID, LAST_LIFT_ID));
            String payLoad = bodyJson.toJSONString();
            eventBuffer.putPayload(payLoad);    //Adding randomly generating json payload in string format to buffer
            i++;
        }
        //Indicating the full consumption of previously generated requestCount number of entries
        eventBuffer.putEvent("FIN");
        eventBuffer.putPayload("FIN");
    }
}