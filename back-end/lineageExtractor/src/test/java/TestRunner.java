import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRunner {

    void TestRunner() {
        System.out.println("InsideMain");
        Result result = JUnitCore.runClasses(TestCreate.class);
    }

    @BeforeAll
    static void setup() {
        System.out.println("Testing for SIVT Back-end:");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("All tests complete.");
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Started");
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        System.out.println("Testing: " + testInfo.getDisplayName() + " - Complete");
    }

    @Test
    @DisplayName("testCreates")
    void testCreates(){
        JUnitCore.runClasses(TestCreate.class);
    }

    @Test
    @DisplayName("testInserts")
    void testInserts(){
        JUnitCore.runClasses(TestInsert.class);
    }

    @Test
    @DisplayName("testAlias")
    void testAlias(){
        JUnitCore.runClasses(TestAlias.class);
    }

    @Test
    @DisplayName("testConditionals")
    void testConditionals(){
        JUnitCore.runClasses(TestConditional.class);
    }

    @Test
    @DisplayName("testFiles")
    void testFiles(){
        JUnitCore.runClasses(TestFiles.class);
    }

    @Test
    @DisplayName("testLiterals")
    void testLiterals(){
        JUnitCore.runClasses(TestLiteral.class);
    }

    @Test
    @DisplayName("testColumns")
    void testColumns(){
        JUnitCore.runClasses(TestColumns.class);
    }

    @Test
    @DisplayName("testWildcards")
    void testWildcards(){
        JUnitCore.runClasses(TestWildcard.class);
    }

    @Test
    @DisplayName("testPrepares")
    void testPrepares(){
        JUnitCore.runClasses(TestPrepare.class);
    }

    @Test
    @DisplayName("testMultiple")
    void testMultiples(){
        JUnitCore.runClasses(TestMultiple.class);
    }

    @Test
    @DisplayName("testAnonymous")
    void testAnonymous(){
        JUnitCore.runClasses(TestAnonymous.class);
    }

    @Test
    @DisplayName("testLineage")
    void testLineage(){
        JUnitCore.runClasses(TestLineage.class);
    }

    @Test
    @DisplayName("testFileReader")
    void testFileReader() {
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye",
                FileReader.ReadFile("./src/test/java/testInput.sql"));
    }

    /**
     *  Todo: Organise below methods
     */


    @Test
    @DisplayName("testNumericSelectValues")
    void testNumericSelectValues() {
        String numericSelectValues = "SELECT 1 as one FROM a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(numericSelectValues)
                .getNodeList();

        // Source table (no columns).
        LineageNode sourceTable = new LineageNode(Constants.Node.TYPE_TABLE, "a");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymousTable.addColumn(new Column("one"));

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(sourceTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testFunctionCall")
    void testFunctionCall() {
        String sql = "SELECT someFunction(a) AS b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table.
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        source.addColumn(new Column("a"));

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column b = new Column("b");
        b.addSource("c::a");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        source.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testSubquery")
    void testSubquery() {
        String sql = "SELECT a FROM (\n" + "SELECT b FROM c\n" + ")###\n";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Table c.
        LineageNode tableC = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        Column b = new Column("b");
        tableC.addColumn(b);

        // Inner-most anonymous table.
        LineageNode anonymous0 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        b.addSource("c::b");
        anonymous0.addColumn(b);

        // Outer-most anonymous table.
        LineageNode anonymous1 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        Column a = new Column("a");
        anonymous0.addColumn(a);
        a.addSource("ANONYMOUS0::a");
        anonymous1.addColumn(a);

        Assertions.assertEquals(3, nodeList.size());
        tableC.equals(nodeList.get(0));
        anonymous0.equals(nodeList.get(1));
        anonymous1.equals(nodeList.get(2));
    }

    @Test
    @DisplayName("testRenameTable")
    void testRenameTable() {
        String sql = "ALTER TABLE mytable RENAME TO newname###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "newname");

        Assertions.assertEquals(1, nodeList.size());
        table.equals(nodeList.get(0));
    }

}
