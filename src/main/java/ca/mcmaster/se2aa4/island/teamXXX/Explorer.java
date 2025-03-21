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
    private ExplorerState state;
    private final List<ExplorerObserver> observers = new ArrayList<>();
    
    public Explorer() {
        this.state = new ScanningState(this);
    }
    
    public void setState(ExplorerState newState) {
        this.state = newState;
    }
    
    public void addObserver(ExplorerObserver observer) {
        observers.add(observer);
    }
    
    public void notifyObservers(String message) {
        for (ExplorerObserver observer : observers) {
            observer.update(message);
        }
    }
    
    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n{}", info.toString(2));
        direction = info.getString("heading");
        battery = info.getInt("budget");
        droneX = 1;
        droneY = 1;
        notifyObservers("Explorer initialized with battery: " + battery);
    }
    
    @Override
    public String takeDecision() {
        return state.takeAction();
    }
    
    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        battery -= response.getInt("cost");
        notifyObservers("Battery reduced to: " + battery);
        
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
                        notifyObservers("Found creek: " + creekId);
                    } else if (poi.getString("kind").equals("SITE")) {
                        emergencySite = poi.getString("id");
                        notifyObservers("Found emergency site: " + emergencySite);
                    }
                }
            }
            if (extras.has("biomes")) {
                lastScanResult = extras.getJSONArray("biomes").toString();
            }
        }
    }
    
    @Override
    public String deliverFinalReport() {
        return closestCreek.isEmpty() ? "no creek found" : closestCreek;
    }
}
