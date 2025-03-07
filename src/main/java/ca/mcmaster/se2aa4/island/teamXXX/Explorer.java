package ca.mcmaster.se2aa4.island.teamXXX;

import eu.ace_design.island.bot.IExplorerRaid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Explorer implements IExplorerRaid {
    private final Logger logger = LogManager.getLogger();
    private int battery;
    private String heading;
    private boolean foundEmergencySite = false;
    private boolean foundCreek = false;
    private String nearestCreekId = "";
    private int minDistance = Integer.MAX_VALUE;
    private int emergencyX = -1, emergencyY = -1;
    private List<JSONObject> creeks = new ArrayList<>();
    private boolean coastlineDetected = false;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        this.heading = info.getString("heading");
        this.battery = info.getInt("budget");
        logger.info("The drone is facing {}", heading);
        logger.info("Battery level is {}", battery);
    }

    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        if (!coastlineDetected) {
            decision.put("action", "scan");
        } else if (!foundEmergencySite) {
            decision.put("action", "fly");
        } else if (!foundCreek) {
            decision.put("action", "fly");
        } else {
            decision.put("action", "stop");
        }
        logger.info("** Decision: {}", decision.toString());
        return decision.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Response received:\n{}", response.toString(2));
        int cost = response.getInt("cost");
        this.battery -= cost;
        logger.info("The cost of the action was {}", cost);

        if (response.has("extras")) {
            JSONObject extras = response.getJSONObject("extras");
            if (extras.has("biomes") && extras.getJSONArray("biomes").toString().contains("OCEAN")) {
                coastlineDetected = true;
                logger.info("Coastline detected, switching to efficient exploration mode");
            }
            if (extras.has("found")) {
                for (Object obj : extras.getJSONArray("found")) {
                    JSONObject foundObj = (JSONObject) obj;
                    if (foundObj.getString("kind").equals("CREEK")) {
                        creeks.add(foundObj);
                        foundCreek = true;
                        logger.info("Creek found with ID: {}", foundObj.getString("id"));
                    } else if (foundObj.getString("kind").equals("SITE")) {
                        foundEmergencySite = true;
                        emergencyX = foundObj.getInt("x");
                        emergencyY = foundObj.getInt("y");
                        logger.info("Emergency site found at ({}, {})", emergencyX, emergencyY);
                    }
                }
            }
        }
    }

    private void findNearestCreek() {
        for (JSONObject creek : creeks) {
            int creekX = creek.getInt("x");
            int creekY = creek.getInt("y");
            int distance = Math.abs(emergencyX - creekX) + Math.abs(emergencyY - creekY);
            if (distance < minDistance) {
                minDistance = distance;
                nearestCreekId = creek.getString("id");
            }
        }
    }

    @Override
    public String deliverFinalReport() {
        findNearestCreek();
        JSONObject report = new JSONObject();
        report.put("nearestCreek", nearestCreekId);
        report.put("creeks", new JSONArray(creeks));
        report.put("emergencySite", new JSONObject().put("x", emergencyX).put("y", emergencyY));
        try (FileWriter file = new FileWriter("./outputs/_pois.json")) {
            file.write(report.toString(2));
            logger.info("** Report saved to _pois.json");
        } catch (IOException e) {
            logger.error("Error writing report file: {}", e.getMessage());
        }
        return foundCreek ? "Nearest rescue creek found: " + nearestCreekId : "No creek found";
    }
}
