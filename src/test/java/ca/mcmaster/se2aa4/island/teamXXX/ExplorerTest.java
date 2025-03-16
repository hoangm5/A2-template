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
        
        String decision = explorer.takeDecision();
        assertEquals("{\"action\":\"stop\"}", decision);
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
}
