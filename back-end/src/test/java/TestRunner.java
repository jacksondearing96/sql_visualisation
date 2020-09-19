import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
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
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye",
                FileReader.ReadFile("./src/test/java/testInput.sql"));
    }

    /**
     * Get the data from a column in the format:
     * "alias=columnAlias,id=columnID,name=columnName,sources={source1,source2,...}"
     * @param column The column which will have its data extracted and stringified.
     * @return The column data in the string form (above).
     */
    private String getColumnDataString(Column column)  {
        String columnData = ReflectionToStringBuilder.reflectionToString(column);
        return columnData.substring(columnData.indexOf("[")+1, columnData.indexOf("]"));
    }

    @Test
    @DisplayName("testColumn")
    void testColumn() {
        // Testing "getters"
        Column column = new Column("name", "alias", "id");
        Assertions.assertEquals("name", column.getName());
        Assertions.assertEquals("alias", column.getAlias());
        Assertions.assertEquals("id", column.getID());
        Assertions.assertTrue(column.getSources().isEmpty());

        // Testing "setters"
        ArrayList<String> sources = new ArrayList<>(Arrays.asList("source1", "source2"));
        column.setName("newName");
        column.setAlias("newAlias");
        column.setID("newID");
        column.setSources(sources);
        column.addSource("source3");
        column.addListOfSources(Arrays.asList("source4", "source5"));
        column.addListOfSources(sources);
        column.addSource("source1");
        Assertions.assertEquals(
                "alias=newAlias,id=newID,name=newName,sources={source1,source2,source3,source4,source5}",
                getColumnDataString(column));

        // Test Column cloning and equals
        try {
            Column clone = (Column) column.clone();
            Assertions.assertTrue(column.equals(clone));
        } catch(CloneNotSupportedException c) {
            Assertions.fail("Cloning column failure.");
        }
    }

    @Test
    @DisplayName("lineageNodeNamingConvention")
    void lineageNodeSetName() {
        // Basic name.
        LineageNode node = new LineageNode("TABLE", "name");
        Assertions.assertEquals("name", node.getName());

        // Name with a base prefix.
        node.setName("base.field");
        Assertions.assertEquals("field", node.getName());

        // Name with multiple base parts.
        node.setName("base0.base1.base2.field");
        Assertions.assertEquals("field", node.getName());

        // Empty name. Make sure this doesn't throw an error.
        node.setName("");
    }

    @DisplayName("testMultipleIdentifiers")
    void testMultipleIdentifiers() {
        String multipleIdentifiersSelectStatement = "select a * b as c, d from mytable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        // Expected source table.
        LineageNode myTable = new LineageNode("TABLE", "mytable");
        Column a = new Column("a");
        Column b = new Column("b");
        Column d = new Column("d");
        myTable.addListOfColumns(Arrays.asList(a, b, d));

        // Expected anonymous table.
        LineageNode anonymousTable = new LineageNode("ANONYMOUS");
        anonymousTable.setName("Anonymous0");
        Column c = new Column("c");
        c.addListOfSources(Arrays.asList("mytable::a", "mytable::b"));
        d.addSource("mytable::d");
        anonymousTable.addListOfColumns(Arrays.asList(c, d));

        Assertions.assertEquals(2, nodeList.size(), "nodeList size");
        Assertions.assertTrue(myTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));

        // While we have the expected tables constructed, test more statements with the same expected output
        // with variations to the SQL syntax.
        multipleIdentifiersSelectStatement = "select someFunction(a, b) as c, d from mytable###";
        nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        Assertions.assertEquals(2, nodeList.size(), "nodeList size");
        Assertions.assertTrue(myTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testLineageNodes")
    void testLineageNodes() {
        String simpleSelect = "SELECT a FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(simpleSelect).getNodeList();

        // Source table.
        LineageNode sourceNode = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        sourceNode.addColumn(a);

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        anonymousNode.addColumn(a);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(anonymousNode.equals(nodeList.get(1)));
        Assertions.assertTrue(sourceNode.equals(nodeList.get(0)));
    }

    @Test
    @DisplayName("testBasicAnonymousTableGeneration")
    void testBasicAnonymousTableGeneration() {
        String statement = "select column1, column2, cast(someDate as date) as columnA from \"tablename\"###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode("TABLE", "tablename");
        Column column1 = new Column("column1");
        Column column2 = new Column("column2");
        Column dateColumn = new Column("someDate");
        table.addListOfColumns(Arrays.asList(column1, column2, dateColumn));

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column column1a = new Column("column1");
        Column column2a = new Column("column2");
        Column columnA = new Column("columnA");
        column1a.addSource("tablename::column1");
        column2a.addSource("tablename::column2");
        columnA.addSource("tablename::someDate");
        anonymousTable.addListOfColumns(Arrays.asList(column1a, column2a, columnA));

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testAliasForColumn")
    void testAliasForColumn() {
        String statement = "SELECT a AS b from c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column aliasedColumn = new Column("b");
        aliasedColumn.addSource("c::a");
        anonymousTable.addColumn(aliasedColumn);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testAliasForTable")
    void testAliasForTable() {
        String statement = "SELECT a FROM b AS c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "b", "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column aliasedColumn = new Column("a");
        aliasedColumn.addSource("b::a");
        anonymousTable.addColumn(aliasedColumn);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testMultipleSelect")
    void testMultipleSelect() {
        String statement = "SELECT a, b FROM c###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Expected tables.
        LineageNode table = new LineageNode("TABLE", "c");
        table.addColumn(new Column("a"));
        table.addColumn(new Column("b"));

        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column columnA = new Column("a");
        columnA.addSource("c::a");
        Column columnB = new Column("b");
        columnB.addSource("c::b");
        anonymousTable.addListOfColumns(Arrays.asList(columnA, columnB));

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testCreateView")
    void testCreateView() {
        String statement = "CREATE VIEW a AS SELECT b from c###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode("TABLE", "c");
        table.addColumn(new Column("b"));

        // View.
        LineageNode view = new LineageNode("VIEW", "a");
        Column columnA = new Column("b");
        columnA.addSource("c::b");
        view.addColumn(columnA);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(view.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testWildCardOperator")
    void testWildCardOperator() {
        String statement = "SELECT * from b###";

        // Output
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode("TABLE", "b");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        Column columnA = new Column("*");
        columnA.addSource("b::*");
        anonymousTable.addColumn(columnA);

        for (LineageNode node :  nodeList) {
            PrettyPrinter.printLineageNode(node);
        }

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @DisplayName("testMultipleStatements")
    void testMultipleStatements() {
        String multipleStatements = "SELECT a FROM b### SELECT c FROM d###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleStatements).getNodeList();

        // Source table (first statement).
        LineageNode firstSource = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        firstSource.addColumn(a);

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Source table (second statement).
        LineageNode secondSource = new LineageNode("TABLE", "d");
        Column c = new Column("c");
        secondSource.addColumn(c);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        c.addSource("d::c");
        secondAnonymous.addColumn(c);

        Assertions.assertEquals(4, nodeList.size());
        Assertions.assertTrue(firstSource.equals(nodeList.get(0)));
        Assertions.assertTrue(firstAnonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(secondSource.equals(nodeList.get(2)));
        Assertions.assertTrue(secondAnonymous.equals(nodeList.get(3)));
    }

    @Test
    @DisplayName("testMultipleReferences")
    void testMultipleReferences() {
        String multipleReferences = "SELECT a FROM b### SELECT c FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleReferences).getNodeList();

        // Source table (both statements).
        LineageNode source = new LineageNode("TABLE", "b");
        Column a = new Column("a");
        Column c = new Column("c");
        source.addListOfColumns(Arrays.asList(a, c));

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode("ANONYMOUS", "Anonymous0");
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        c.addSource("b::c");
        secondAnonymous.addColumn(c);

        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(firstAnonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(secondAnonymous.equals(nodeList.get(2)));
    }

    @Test
    @DisplayName("testBypassAnonymousTables")
    void testBypassAnonymousTables() {
        String sql =
                "CREATE VIEW view AS " +
                        "SELECT b " +
                        "FROM (" +
                        "SELECT b " +
                        "FROM B" +
                        ") AS A" +
                        "###";

        // Source table.
        LineageNode source = new LineageNode("TABLE", "b");
        Column b = new Column("b");
        source.addColumn(b);

        // Anonymous table.
        LineageNode anonymous = new LineageNode("ANONYMOUS", "Anonymous0");
        anonymous.setAlias("A");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        anonymous.addColumn(b);

        // View.
        LineageNode view = new LineageNode("VIEW", "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(anonymous.getName(), b.getName()));
        view.addColumn(b);

        // First, verify that the anonymous table is produced correctly as the intermediate table.
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(view.equals(nodeList.get(2)));

        // Now extract the lineage, including the step where the anonymous tables are bypassed.
        nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        // Adjust the view, it's column's sources have now bypassed the anonymous table.
        view = new LineageNode("VIEW", "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        view.addColumn(b);

        // Check the resultant lineage is as expected.
        Assertions.assertEquals(2,  nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(view.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testAgentLeads")
    void testAgentLeads() {
        String agentLeadsSql = FileReader.ReadFile("../propic_sql_scripts/agent_leads.sql");
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(agentLeadsSql).getNodeList();

        // crms_task table.
        String crmsTaskName = "%(crm)s_task";
        LineageNode crmsTask = new LineageNode("TABLE", crmsTaskName);
        Column accountid = new Column("accountid");
        Column ownerid = new Column("ownerid");
        Column status = new Column("status");
        Column activityDate = new Column("activitydate");
        crmsTask.addListOfColumns(Arrays.asList(accountid, ownerid, status, activityDate));

        // customer_insight table.
        String customerInsightName = "customer_insight";
        LineageNode customerInsight = new LineageNode("TABLE", customerInsightName);
        Column acctSfId = new Column("acct_sf_id");
        Column userSfId = new Column("user_sf_id");
        customerInsight.addListOfColumns(Arrays.asList(acctSfId, userSfId));
        customerInsight.setAlias("b");

        // Anonymous0.
        LineageNode anonymous0 = new LineageNode("ANONYMOUS", "Anonymous0");
        accountid.addSource(DataLineage.makeId(crmsTaskName, accountid.getName()));
        ownerid.addSource(DataLineage.makeId(crmsTaskName, ownerid.getName()));
        status.addSource(DataLineage.makeId(crmsTaskName, status.getName()));
        Column taskNoteDate = new Column("task_note_date");
        taskNoteDate.addSource(DataLineage.makeId(crmsTaskName, activityDate.getName()));
        anonymous0.addListOfColumns(Arrays.asList(accountid, ownerid, status, taskNoteDate));
        anonymous0.setAlias("a");

        // Anonymous1.
        LineageNode anonymous1 = new LineageNode("ANONYMOUS", "Anonymous1");
        acctSfId.addSource(DataLineage.makeId(customerInsightName, acctSfId.getName()));
        userSfId.addSource(DataLineage.makeId(customerInsightName, userSfId.getName()));
        anonymous1.addListOfColumns(Arrays.asList(acctSfId, userSfId));

        // View.
        String view0Name = "note_count_by_agent";
        LineageNode view0 = new LineageNode("VIEW", view0Name);
        acctSfId = new Column("acct_sf_id");
        acctSfId.addSource(DataLineage.makeId("Anonymous1", acctSfId.getName()));
        userSfId = new Column("user_sf_id");
        userSfId.addSource(DataLineage.makeId("Anonymous1", userSfId.getName()));
        Column cnt = new Column("cnt");
        view0.addListOfColumns(Arrays.asList(acctSfId, userSfId, cnt));

        // View (second statement).
        String view1Name = "agent_prediction_obj";
        LineageNode view1 = new LineageNode("VIEW", view1Name);
        Column address_detail_pid = new Column("address_detail_pid");
        Column acctName = new Column("acct_name");
        Column spark = new Column("spark");
        Column kafka = new Column("kafka");
        Column clientAgentScore = new Column("client_agent_score");
        Column caseE = new Column("case_e");
        Column caseF = new Column("case_f");
        Column caseG = new Column("case_g");
        Column primaryOwner = new Column("primary_owner");
        Column allOwners = new Column("all_owners");
        Column noteCnt = new Column("note_cnt");

        customerInsight.addListOfColumns(
                Arrays.asList(
                        address_detail_pid,
                        acctName,
                        spark,
                        kafka,
                        clientAgentScore,
                        caseE,
                        caseF,
                        caseG,
                        primaryOwner,
                        allOwners
                )
        );

        address_detail_pid.addSource(DataLineage.makeId(customerInsightName, address_detail_pid.getName()));
        acctSfId = new Column("acct_sf_id");
        acctSfId.addSource(DataLineage.makeId(customerInsightName, acctSfId.getName()));
        acctName.addSource(DataLineage.makeId(customerInsightName, acctName.getName()));
        userSfId = new Column("user_sf_id");
        userSfId.addSource(DataLineage.makeId(customerInsightName, userSfId.getName()));
        spark.addSource(DataLineage.makeId(customerInsightName, spark.getName()));
        kafka.addSource(DataLineage.makeId(customerInsightName, kafka.getName()));
        clientAgentScore.addSource(DataLineage.makeId(customerInsightName, clientAgentScore.getName()));
        caseE.addSource(DataLineage.makeId(customerInsightName, caseE.getName()));
        caseF.addSource(DataLineage.makeId(customerInsightName, caseF.getName()));
        caseG.addSource(DataLineage.makeId(customerInsightName, caseG.getName()));
        primaryOwner.addSource(DataLineage.makeId(customerInsightName, primaryOwner.getName()));
        allOwners.addSource(DataLineage.makeId(customerInsightName, allOwners.getName()));
        noteCnt.addSource(DataLineage.makeId(view0Name, cnt.getName()));

        view1.addListOfColumns(Arrays.asList(
                address_detail_pid,
                acctSfId,
                acctName,
                userSfId,
                spark,
                kafka,
                clientAgentScore,
                caseE,
                caseF,
                caseG,
                primaryOwner,
                allOwners,
                noteCnt
        ));

        Assertions.assertEquals(6, nodeList.size());
        Assertions.assertTrue(crmsTask.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous0.equals(nodeList.get(1)));
        Assertions.assertTrue(customerInsight.equals(nodeList.get(2)));
        Assertions.assertTrue(anonymous1.equals(nodeList.get(3)));
        Assertions.assertTrue(view0.equals(nodeList.get(4)));
        // TODO: the table not tested here is %(db)s.customer_insight
        //       The behaviour here depends on how the database name prefix is handled.
        //       The correct behaviour has not been determined yet.
        Assertions.assertTrue(view1.equals(nodeList.get(5)));
    }

    @DisplayName("testNumericSelectValues")
    void testNumbericSelectValues() {
        String numericSelectValues = "SELECT 1 as one FROM a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(numericSelectValues).getNodeList();

        // Source table (no columns).
        LineageNode sourceTable = new LineageNode("TABLE", "a");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode("ANONYMOUS", "Anonymous0");
        anonymousTable.addColumn(new Column("one"));

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(sourceTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testLiteralInlineTable")
    void testLiteralInlineTables() {

        String sql = "SELECT b FROM ( " +
                "VALUES " +
                "(1, 'a')," +
                "(2, 'b')," +
                "(3, 'c')" +
                ")###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode("ANONYMOUS", "Anonymous0");
        Column b = new Column("b");
        inlineLiteral.addColumn(b);

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        b.addSource("Anonymous0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testLiteralInlineTableWithAlias")
    void testLiteralInlineTablesWithAlias() {

        String sql = "SELECT b FROM ( " +
                "VALUES " +
                "(1, 'a')," +
                "(2, 'b')," +
                "(3, 'c')" +
                ") AS a###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode("ANONYMOUS", "Anonymous0", "a");
        Column b = new Column("b");
        inlineLiteral.addColumn(b);

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        b.addSource("Anonymous0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testLiteralInlineTableWithAliasAndColumnLabels")
    void testLiteralInlineTablesWithAliasAndColumnLabels() {

        String sql = "SELECT b FROM ( " +
                         "VALUES " +
                             "(1, 'a')," +
                             "(2, 'b')," +
                             "(3, 'c')" +
                     ") AS a (b, c)###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode("ANONYMOUS", "Anonymous0", "a");
        Column b = new Column("b");
        inlineLiteral.addListOfColumns(Arrays.asList(b, new Column("c")));

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode("ANONYMOUS", "Anonymous1");
        b.addSource("Anonymous0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }
}
