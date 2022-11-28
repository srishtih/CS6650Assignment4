import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Consumer class is responsible for reading messages from the RabbitMQ's queue and writing
 * this data into a redis database
 */
public class Consumer {
    protected static final String QUEUE_NAME = "tester";
    private static final Integer NUM_THREADS = 1000;
    public static final JedisPool jPool = new JedisPool("35.165.209.121", 6379);


    /**
     * Driver code for establishing connection to queue and creating channels to read queue
     * @param args Arguments, if any
     * @throws IOException for signalling connection related I/O exception
     * @throws TimeoutException for connection timeout related scenarios
     */
    public static void main(String[] args) throws TimeoutException, IOException {
        //Initialization
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("54.203.66.122");
        factory.setUsername("admin");
        factory.setPassword("rabbitmq");
        Connection connection = factory.newConnection();

        //Use thread pool for churning out consumers
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            pool.execute(new ConsumerThread(QUEUE_NAME, connection));
        }
    }
}


