import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ConsumerThread class defines the behaviour of individual consumer threads that each create
 * a channel to consume data from message queue and precess this data
 */
public class ConsumerThread implements Runnable {

    private final String queueName;
    private final Connection conn;
    Jedis dbConnection;
    private static final String QUEUE_NAME = "tester";

    /**
     * Constructor class for ConsumerThread
     *
     * @param queueName  Name of the queue to which each channel must bind
     * @param connection Connection established between consumer and message queue
     */
    public ConsumerThread(String queueName, Connection connection) {
        this.queueName = queueName;
        this.conn = connection;
    }


    /**
     * Contains code executed by each instance of this class(a consumer thread)
     */
    @Override
    public void run() {
        boolean autoAck = false;
        try {
            //channel declaration and binding
            Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            //defining callback function
            DeliverCallback callback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                if (doWork(message)) {
                    // acknowledge successful consumption
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } else {
                    // inform the consumption is incomplete
                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            //consume messages from queue
            channel.basicConsume(QUEUE_NAME, autoAck, callback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the message consumed from the queue as per requirement.
     * Here, the message is written into a database as a hash map
     *
     * @param message Message read from queue, json string of the post request's data
     * @return boolean confirmation indicating message is written to hashmap
     */

    private boolean doWork(String message) {
        try{
            JSONObject body = new JSONObject(message);
            Map<String, String> event = new HashMap<>();
            String skierID = body.getInt("skierId")+ ":" +  body.getString("seasonId") + ":" +
                    body.getInt("dayId") + ":"+ body.getInt("timeId");
            event.put("day", String.valueOf(body.getInt("dayId")));
            event.put("time", String.valueOf(body.getInt("timeId")));
            event.put("liftId", String.valueOf(body.getInt("liftId")));
            event.put("seasonId", body.getString("seasonId"));
            event.put("resortID", String.valueOf(body.getInt("resortId")));

            dbConnection = Consumer.jPool.getResource();
            dbConnection.hmset("skier:"+skierID, event);
            dbConnection.sadd("LiftRides", "skier:"+skierID);
            System.out.println("Success!!");
            Consumer.jPool.returnResource(dbConnection);
            return true;
        } catch(Exception e){
            e.printStackTrace();
//            System.out.println(e.getMessage());
            return false;
        }
    }
}

/*
String field = skierID + " "+
"season " + body.getString("seasonId") + " day " + body.getInt("dayId") + ":" +
                    body.getInt("timeId");
            String value = "resort " + body.getInt("resortId") + " lift " + body.getInt("liftId");
 */




