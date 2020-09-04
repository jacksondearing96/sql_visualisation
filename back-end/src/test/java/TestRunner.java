import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TestRunner {

    @BeforeAll
    static void setup(){
        System.out.println("Testing for SIVT Back-end Parser");
    }

    @BeforeEach
    void setupEach(){
        System.out.println("This is executed before each test");
    }

    @Tag("FileReader")
    @Test
    void testFileReader(){
        System.out.println("Test: FileReader");
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye", FileReader.ReadFile("./src/test/java/testInput.sql"));
    }

    // NOTE - this is untested - just adding for an idea of how to test the lineage extractor without needing
    // to dive into JSON. This will need some extra work to get it to run eg. import the LineageExtractor?
    //
    // TODO: Make some way of comparing two lineage nodes. I'm not sure how this is typically done in Java but 
    // in c++ this would be like overloading the == operator so that we can compare lineage nodes much more easily
    // that the extensive repetition below.
    @Tag("LineageExtractor")
    @Test
    void testSimpleSelect() {
        String simpleSelect = "SELECT a FORM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(simpleSelect).getNodeList();

        Assertions.assertEquals(nodeList.size(), 2);

        // Source table.
        Assertions.assertEquals(nodeList.get(1).GetType(), "TABLE");
        Assertions.assertEquals(nodeList.get(1).GetName(), "b");
        Assertions.assertEquals(nodeList.get(1).HasAlias(), false);
        Assertions.assertEquals(nodeList.get(1).GetColumns().size(), 1);
        Assertions.assertEquals(nodeList.get(1).GetColumns().get(0).GetName(), "a");

        // Anonymous table.
        Assertions.assertEquals(nodeList.get(1).GetType(), "ANONYMOUS");
        Assertions.assertEquals(nodeList.get(1).GetName(), "Anonymous0");
        Assertions.assertEquals(nodeList.get(1).HasAlias(), false);
        Assertions.assertEquals(nodeList.get(1).GetColumns().size(), 1);
        Assertions.assertEquals(nodeList.get(1).GetColumns().get(0).GetName(), "a");
    }

}
