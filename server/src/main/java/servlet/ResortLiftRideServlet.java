package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "ResortLiftRideServlet", urlPatterns = "/resorts/*")
public class ResortLiftRideServlet extends HttpServlet {

    private Connection connection;
    private static final int NUM_CHANNELS = 20;
    private BlockingQueue<Channel> channelPool;
    private String QUEUE_NAME = "resort";
    Gson g  = new Gson();
    public static final JedisPool jPool = new JedisPool(buildPoolConfig(), Protocol.DEFAULT_HOST, 6379);

    Jedis dbConnection;

    private final String SEASONS_PARAMETER = "seasons";
    private final String DAYS_PARAMETER = "days";
    private final String SKIERS_PARAMETER = "skiers";
    private final int DAY_ID_MIN = 1;
    private final int DAY_ID_MAX = 3;

    @Override
    public void init() throws ServletException {
        super.init();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setPort(5672);
        factory.setHost("localhost");
        factory.setUsername("test1");
        factory.setPassword("test1");

        try {
            //One time connection establishment to EC2 instance hosting RabbitMQ
            this.connection= factory.newConnection();
        } catch (IOException |TimeoutException e) {
            System.out.println("Issue in establishing connection to remote RabbitMq service");
            throw new RuntimeException(e);
        }

        channelPool = new LinkedBlockingDeque<>();
        for (int i = 0; i < NUM_CHANNELS; i++) {
            try {
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channelPool.add(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject values = new JsonObject();
        PrintWriter out = response.getWriter();

        String urlPath = request.getPathInfo();

        System.out.println(urlPath);

        if (urlPath == null || urlPath.isEmpty()){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("404: Missing url");
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (!isUrlValid(urlParts, request)){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Not a valid url");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);

            String resort_composite_key = createCompositeKey(urlParts);
            // connect with jedis
            // return the number of skiers
            dbConnection = jPool.getResource();
            Long numberOfSkiers = dbConnection.scard(resort_composite_key);
            values.addProperty("resort", numberOfSkiers);
            values.addProperty("numOfSkiers", "123");
            response.getWriter().write(g.toJson(values));
        }
    }

    private boolean isUrlValid(String[] urlPath, HttpServletRequest res) {
        if (urlPath.length == 7) {
            try {
                for (int i = 1; i < urlPath.length; i+=2){
                    Integer.parseInt(urlPath[i]);
                }
                return (urlPath[3].length() == 4
                        && Integer.parseInt(urlPath[5]) >= DAY_ID_MIN
                        && Integer.parseInt(urlPath[5]) < DAY_ID_MAX
                        && urlPath[2].equals(SEASONS_PARAMETER)
                        && urlPath[4].equals(DAYS_PARAMETER)
                        && urlPath[6].equals(SKIERS_PARAMETER));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private String createCompositeKey(String[] urlParts) {
        String resortId = String.valueOf(Integer.parseInt(urlParts[1]));
        String seasonId = String.valueOf(Integer.parseInt(urlParts[3]));
        String dayId = String.valueOf(Integer.parseInt(urlParts[5]));

        return resortId + ":" + seasonId + ":" + dayId;
    }

    private static JedisPoolConfig buildPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxWait(Duration.ofMillis(2000));
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        return poolConfig;
    }
}
