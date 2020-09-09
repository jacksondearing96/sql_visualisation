import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

import java.util.List;

public class TestRunner {

    @BeforeAll
    static void setup(){
        System.out.println("Testing for SIVT Back-end:");
    }

    @AfterAll
    static void afterAll(){
        System.out.println("All tests complete.");
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo){
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Started");
    }

    @AfterEach
    void afterEach(TestInfo testInfo){
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Complete");
    }

    @Test
    @DisplayName("testFileReader")
    void testFileReader(){
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye", FileReader.ReadFile("./src/test/java/testInput.sql"));
    }

    // TODO: Make some way of comparing two lineage nodes. I'm not sure how this is typically done in Java but
    // in c++ this would be like overloading the == operator so that we can compare lineage nodes much more easily
    // that the extensive repetition below.
    @Tag("LineageExtractor")
    @Test
    void testSimpleSelect() {
        String simpleSelect = "SELECT a FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(simpleSelect).getNodeList();

        Assertions.assertEquals(2, nodeList.size());

        // Source table.
        Assertions.assertEquals("TABLE", nodeList.get(1).getType());
        Assertions.assertEquals("b", nodeList.get(1).getName());
        Assertions.assertEquals(false, nodeList.get(1).hasAlias());
        Assertions.assertEquals(1, nodeList.get(1).getColumns().size());
        Assertions.assertEquals("a", nodeList.get(1).getColumns().get(0).getName());

        // Anonymous table.
        Assertions.assertEquals("ANONYMOUS", nodeList.get(1).getType());
        Assertions.assertEquals("Anonymous0", nodeList.get(1).getName());
        Assertions.assertEquals(false, nodeList.get(1).hasAlias());
        Assertions.assertEquals(1, nodeList.get(1).getColumns().size());
        Assertions.assertEquals("a", nodeList.get(1).getColumns().get(0).getName());
    }
}
