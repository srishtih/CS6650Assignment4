package model;

/**
 * @author srish
 * SkiLiftRide is a class that models the event of a ski lift ride with its attributes describing the lift's
 * resord ID, seasonID, liftID, dayId, the time of ride and skierID
 *
 */

public class SkiLiftRide {
    private int resortId;
    private String seasonId;
    private int liftId;
    private String dayId;
    private int skierId;
    private int timeId;

    /**
     * Simple constructor for SkiLiftRide class
     * @param resortId id of the resort
     * @param seasonId season's id
     * @param liftId id of the lift
     * @param dayId day id
     * @param timeId id for the time of the lift ride
     * @param skierId id of the skier
     */
    public SkiLiftRide(int resortId, String seasonId, int liftId, String dayId, int timeId, int skierId) {
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.liftId = liftId;
        this.dayId = dayId;
        this.timeId = timeId;
        this.skierId = skierId;
    }

    /**
     * Validates if all the attributes of an instance of this class are value. If the values of the attributes
     * differ from the specified range, this event is not considered valid
     * @return boolean value; true if all the instance attributes have values in the range of allowable values,
     * false otherwise
     */
    public boolean isValid(){
        if(this.skierId < 1 || this.skierId > 100000) {
            return false;
        }
        if(this.resortId < 1 || this.resortId > 10){
            return false;
        }
        if(this.liftId < 1 || this.liftId > 40) {
            return false;
        }
        if(!this.seasonId.equals("2022")){
            return false;
        }
        if(!this.dayId.equals("1")) {
            return false;
        }
        if(this.timeId <1 || this.timeId >360){
            return false;
        }
        return true;
    }

    @Override
    /**
     * Displays all attributes of an instance of the class in readable format
     * @return String format of the object
     */
    public String toString() {
        return "Ski Lift ride details:" +
                "\nresortId: " + resortId +
                "\nseasonId: " + seasonId +
                "\nliftId: " + liftId +
                "\ndayId: " + dayId +
                "\ntimeId:" + timeId+
                "\nskierId: " + skierId;
    }
}
