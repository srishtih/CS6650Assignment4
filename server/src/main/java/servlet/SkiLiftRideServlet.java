package servlet;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import model.SkiLiftRide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * @author srish
 *
 * Contains servlet implementation that handles GET and POST requests
 * The mapping to this servlet is covered in the web.xml file
 * URL: /skiers/*
 */

//@WebServlet(name = "SkiLiftRideServlet", value = "/SkiLiftRideServlet")
public class SkiLiftRideServlet extends HttpServlet {
    private static final int NUM_CHANNELS = 20;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Pattern urlPattern = Pattern.compile("/\\d+/seasons/\\d+/day/\\d+/skier/\\d+");
    private Connection connection;
    private String QUEUE_NAME = "tester";
    private BlockingQueue<Channel> channelPool;

    /**
     * init() method allows the servlet to read persistent configuration data, initialize resources,
     * and perform any other one-time activities, you override the init method of the Servlet interface.
     */
    @Override
    public void init(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("54.203.66.122");
        factory.setUsername("admin");
        factory.setPassword("rabbitmq");
        try {
            //One time connection establishment to EC2 instance hosting RabbitMQ
            this.connection= factory.newConnection();
        } catch (IOException |TimeoutException e) {
            System.out.println("Issue in establishing connection to remote RabbitMq service");
            throw new RuntimeException(e);
        }
        //creating a channel pool to be utilized in the doPost() method
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



    /**
     * doGet method defines the servlet behaviour when a GET request is sent to the url mapped to this servlet
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h1>You are doing okay!</h1>");
    }

    /**
     * doPost method defines the servlet behaviour when a POST request is sent to the url mapped to this servlet
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();     // reads the path specified in request, it contains path parameters
            if (urlPath == null || urlPath.isEmpty()) {
                //if the url path specified is null or empty, send a response with error code and error message
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("No path specified");
                return;
            } else if (!urlPattern.matcher(urlPath).matches()) {
                // if the url sent is not in the specified format(that matches Pattern urlPattern),
                // the values read from splitting this url, do not give us valid values. In this case, send error
                // code and error message
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("The path specified is invalid");
                return;
            }

            String[] pathParams = urlPath.split("/"); //split the url path to read the parameters passed
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class); //read the json payload specified in the body
            //create a new instance of the SkiLiftRide class with read path parameters and json payload as arguments
            SkiLiftRide ride = new SkiLiftRide(Integer.parseInt(pathParams[1]), String.valueOf(pathParams[3]),
                    Integer.parseInt(String.valueOf(body.get("liftID"))),
                    String.valueOf(pathParams[5]), Integer.parseInt(String.valueOf(body.get("time"))), Integer.parseInt(pathParams[7]));

            // check if the newly created instance of SkiLiftRide has all valid values
            if (ride.isValid()) {
                //if valid, write data as message to queue and send success response
                try {
                    //convert read data into message for queue
                    String message = new Gson().toJson(ride);
                    Channel channel = channelPool.take();  //retrieve channel from pool
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                    response.setStatus(HttpServletResponse.SC_OK);  //send response to client
                    response.getWriter().write("Record created");
                    channelPool.add(channel);   //return channel to pool
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                //if invalid, send error code and message
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("The data passed is invalid\n" + ride);
            }
    }
}



