package ca.mcmaster.se2aa4.island.teamXXX;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import java.util.List;

public class ExplorerTest {
    private Explorer explorer;

    @BeforeEach
    void setUp() {
        explorer = new Explorer();
        String initJson = "{\"budget\": 1000, \"heading\": \"NORTH\"}";
        explorer.initialize(initJson);
    }

    @Test
    void testInitialization() {
        String decision = explorer.takeDecision();
        assertNotNull(decision);
        assertTrue(decision.contains("action"));
    }

    @Test
    void testTakeDecisionBeforeFindingPOIs() {
        String decision = explorer.takeDecision();
        assertTrue(decision.contains("scan") || decision.contains("fly"));
    }

    @Test
    void testAcknowledgeResultsWithCreekAndSite() {
        String resultJson = "{\"cost\": 10, \"extras\": {\"found\": [{\"kind\": \"CREEK\", \"id\": \"creek-1\"}, {\"kind\": \"SITE\", \"id\": \"site-1\"}]}}";
        explorer.acknowledgeResults(resultJson);
        assertEquals("creek-1", explorer.deliverFinalReport());
    }

    @Test
    void testAcknowledgeResultsWithMultipleCreeks() {
        String resultJson = "{\"cost\": 10, \"extras\": {\"found\": [{\"kind\": \"CREEK\", \"id\": \"creek-1\"}, {\"kind\": \"CREEK\", \"id\": \"creek-2\"}]}}";
        explorer.acknowledgeResults(resultJson);
        assertFalse(explorer.deliverFinalReport().isEmpty());
    }

    @Test
    void testFinalReportNoCreekFound() {
        assertEquals("no creek found", explorer.deliverFinalReport());
    }
    
    // New Test Cases
    
    @Test
    void testExplorerMovesCorrectly() {
        explorer.takeDecision();
        assertNotEquals("{\"action\":\"stop\"}", explorer.takeDecision());
    }
    
    @Test
    void testBatteryDecreasesAfterAction() {
        int initialBattery = explorer.getBattery();
        explorer.acknowledgeResults("{\"cost\": 50}");
        assertTrue(explorer.getBattery() < initialBattery);
    }
    
    @Test
    void testChangeDirectionOnOceanDetection() {
        explorer.acknowledgeResults("{\"cost\": 10, \"extras\": {\"biomes\": [\"OCEAN\"]}}");
        String decision = explorer.takeDecision();
        assertTrue(decision.contains("heading"));
    }
    
    @Test
    void testDecisionAfterBatteryLow() {
        explorer.acknowledgeResults("{\"cost\": 1000}"); // Drains battery completely
        assertEquals("{\"action\":\"stop\"}", explorer.takeDecision());
    }
    
    @Test
    void testMultipleScans() {
        explorer.takeDecision();
        explorer.takeDecision();
        explorer.takeDecision();
        assertTrue(explorer.takeDecision().contains("scan"));
    }
}
