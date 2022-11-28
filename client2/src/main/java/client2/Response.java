package client2;

import java.sql.Timestamp;

/**
 * @author srish
 * Response class details about HTTP responses response recieved.
 * Based on timestamps taken before and after the request was sent and response recieval
 * latency is calculated.
 * Furthermore, it stores the request type and response code recieved
 */

public class Response {
    private Timestamp start;
    private Timestamp end;
    private int latency;
    private String requestType = "POST"; //Here, since all requests are POST, this has been set as default value
    private int responseCode;

    /**
     * Constructor for Response class. To make it computationally as light as possible,
     * all latency calculations are kept outside
     * @param start Timestamp before request is sent
     * @param end   Timestamp after response is received
     * @param responseCode status code of the response received
     */
    public Response(Timestamp start, Timestamp end, int responseCode){
        this.start = start;
        this.end = end;
        this.responseCode = responseCode;
    }

    /**
     * Calculates latency based on timestamps before and after request is sent
     * @return latency for the given response
     */
    public int getLatency() {
        this.latency = (int)(end.getTime() - start.getTime());
        return this.latency;
    }

    /**
     * Displays all attributes of an instance of the class in readable format
     * @return String format of the object
     */
    @Override
    public String toString() {
        return "Response[" +
                "\nStart time: " + start +
                "\nEnd time:" + end +
                "\nLatency:" + latency +
                "\nRequest Type: " + requestType  +
                "\nResponse Code: " + responseCode +
                ']';
    }
}
