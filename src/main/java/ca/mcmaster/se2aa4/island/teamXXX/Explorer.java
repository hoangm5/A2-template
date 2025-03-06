package ca.mcmaster.se2aa4.island.teamXXX;

import eu.ace_design.island.bot.IExplorerRaid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.StringReader;

public class Explorer implements IExplorerRaid {
    private final Logger logger = LogManager.getLogger();
    private int battery;
    private String heading;
    private boolean foundEmergencySite = false;
    private boolean foundCreek = false;
    private String creekId = "";

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        this.heading = info.getString("heading");
        this.battery = info.getInt("budget");
        logger.info("Battery level: {}", battery);
        logger.info("Drone is facing: {}", heading);
    }

    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        if (!foundEmergencySite) {
            decision.put("action", "scan");
        } else if (!foundCreek) {
            decision.put("action", "fly");
        } else {
            decision.put("action", "stop");
        }
        return decision.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        if (response.has("extras")) {
            JSONObject extras = response.getJSONObject("extras");
            if (extras.has("found")) {
                for (Object obj : extras.getJSONArray("found")) {
                    JSONObject foundObj = (JSONObject) obj;
                    if (foundObj.getString("kind").equals("CREEK")) {
                        foundCreek = true;
                        creekId = foundObj.getString("id");
                    } else if (foundObj.getString("kind").equals("SITE")) {
                        foundEmergencySite = true;
                    }
                }
            }
        }
    }

    @Override
    public String deliverFinalReport() {
        return foundCreek ? "Rescue creek found: " + creekId : "No creek found";
    }
}
