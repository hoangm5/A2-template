package ca.mcmaster.se2aa4.island.teamXXX;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class ExplorerTest {
    private Explorer explorer;

    @BeforeEach
    void setUp() {
        explorer = new Explorer();
        String initJson = "{"heading":"EAST", "budget":1000}";
        explorer.initialize(initJson);
    }

    @Test
    void testInitialization() {
        assertNotNull(explorer);
    }

    @Test
    void testTakeDecisionInitial() {
        String decision = explorer.takeDecision();
        assertTrue(decision.contains("scan"));
    }

    @Test
    void testAcknowledgeResultsEmergencySite() {
        String response = "{"cost":10, "extras":{"found":[{"kind":"SITE", "x":5, "y":10}]}}";
        explorer.acknowledgeResults(response);
        assertTrue(explorer.deliverFinalReport().contains("No creek found"));
    }

    @Test
    void testAcknowledgeResultsCreek() {
        String response = "{"cost":10, "extras":{"found":[{"kind":"CREEK", "id":"creek-123", "x":3, "y":8}]}}";
        explorer.acknowledgeResults(response);
        assertTrue(explorer.deliverFinalReport().contains("creek-123"));
    }

    @Test
    void testBatteryDepletion() {
        for (int i = 0; i < 100; i++) {
            explorer.acknowledgeResults("{"cost":50}");
        }
        assertTrue(explorer.takeDecision().contains("stop"));
    }

    @Test
    void testMultiplePOIs() {
        String response = "{"cost":10, "extras":{"found":[{"kind":"CREEK", "id":"creek-001", "x":3, "y":8}, {"kind":"CREEK", "id":"creek-002", "x":5, "y":6}]}}";
        explorer.acknowledgeResults(response);
        assertTrue(explorer.deliverFinalReport().contains("creek-001") || explorer.deliverFinalReport().contains("creek-002"));
    }
}
