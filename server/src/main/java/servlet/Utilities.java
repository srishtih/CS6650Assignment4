package servlet;

public class Utilities {

    private static final String SEASONS_PARAMETER = "seasons";
    private static final String DAYS_PARAMETER = "days";
    private static final String SKIERS_PARAMETER = "skiers";
    private static final int DAY_ID_MIN = 1;
    private static final int DAY_ID_MAX = 3;

    public static String patternTwoKey(String url){
        String[] urlParts = url.split("/");
        String resortId = urlParts[1];
        String seasonId = urlParts[3];
        String dayId = urlParts[5];
        String skierId = urlParts[7];
        return "skiers:" + resortId + ":" + seasonId + ":" + dayId + ":" + skierId;
    }

    public static String patternThreeKey(String url){
        String[] urlParts = url.split("/");
        String skierId = urlParts[1];
        return "skiers:" + skierId;
    }

    private boolean isValid(String path) {
        String[] urlPath = path.split("/");

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
}
