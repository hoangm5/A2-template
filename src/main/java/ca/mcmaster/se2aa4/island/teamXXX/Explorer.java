package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONArray;

public class Explorer implements IExplorerRaid {
    private final Logger logger = LogManager.getLogger();
    private int battery;
    private String direction;
    private final Set<String> visitedCells = new HashSet<>();
    private String emergencySite = "";
    private List<String> creeks = new ArrayList<>();
    private String closestCreek = "";
    private int droneX, droneY;
    private final Map<String, Integer> creekDistances = new HashMap<>();
    private String lastScanResult = "";
    private int moveCounter = 0;
    private final List<String> directions = Arrays.asList("NORTH", "EAST", "SOUTH", "WEST");
    
    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n{}", info.toString(2));
        direction = info.getString("heading");
        battery = info.getInt("budget");
        droneX = 1;
        droneY = 1;
    }
    
    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        
        if (!emergencySite.isEmpty() && !creeks.isEmpty()) {
            computeClosestCreek();
            decision.put("action", "stop");
        } else {
            decision = explore();
        }
        
        logger.info("** Decision: {}", decision.toString());
        return decision.toString();
    }
    
    private JSONObject explore() {
        JSONObject decision = new JSONObject();
        
        if (moveCounter % 3 == 0 || visitedCells.isEmpty()) {
            decision.put("action", "scan");
        } else {
            if (lastScanResult.equals("OCEAN")) {
                decision.put("action", "heading");
                direction = getNewDirection();
                decision.put("direction", direction);
            } else {
                decision.put("action", "fly");
                updateDronePosition();
            }
        }
        
        moveCounter++;
        return decision;
    }
    
    private void updateDronePosition() {
        visitedCells.add(droneX + "," + droneY);
        switch (direction) {
            case "NORTH": droneY += 1; break;
            case "SOUTH": droneY -= 1; break;
            case "EAST": droneX += 1; break;
            case "WEST": droneX -= 1; break;
        }
    }
    
    private String getNewDirection() {
        int currentIndex = directions.indexOf(direction);
        return directions.get((currentIndex + 1) % directions.size());
    }
    
    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        battery -= response.getInt("cost");
        
        if (response.has("extras")) {
            JSONObject extras = response.getJSONObject("extras");
            if (extras.has("found")) {
                JSONArray found = extras.getJSONArray("found");
                for (int i = 0; i < found.length(); i++) {
                    JSONObject poi = found.getJSONObject(i);
                    if (poi.getString("kind").equals("CREEK")) {
                        String creekId = poi.getString("id");
                        creeks.add(creekId);
                        creekDistances.put(creekId, Math.abs(droneX) + Math.abs(droneY));
                    } else if (poi.getString("kind").equals("SITE")) {
                        emergencySite = poi.getString("id");
                    }
                }
            }
            if (extras.has("biomes")) {
                lastScanResult = extras.getJSONArray("biomes").toString();
            }
        }
    }
    
    private void computeClosestCreek() {
        closestCreek = creeks.stream()
                .min(Comparator.comparingInt(creekDistances::get))
                .orElse("");
    }
    
    @Override
    public String deliverFinalReport() {
        return closestCreek.isEmpty() ? "no creek found" : closestCreek;
    }
}
