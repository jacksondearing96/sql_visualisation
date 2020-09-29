import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRunner {

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
    @DisplayName("testFileReader")
    void testFileReader() {
        Assertions.assertEquals(" SELECT * FROM hello### SELECT a FROM goodbye",
                FileReader.ReadFile("./src/test/java/testInput.sql"));
    }

    /**
     * Get the data from a column in the format:
     * "alias=columnAlias,id=columnID,name=columnName,sources={source1,source2,...}"
     * 
     * @param column The column which will have its data extracted and stringified.
     * @return The column data in the string form (above).
     */
    private String getColumnDataString(Column column) {
        String columnData = ReflectionToStringBuilder.reflectionToString(column);
        return columnData.substring(columnData.indexOf("[") + 1, columnData.indexOf("]"));
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
                "alias=newAlias,id=newID,name=newName,sources={source1,source2,source3,source4,source5},stagedRename=Optional.empty",
                getColumnDataString(column));

        // Test Column cloning and equals
        try {
            Column clone = (Column) column.clone();
            Assertions.assertTrue(column.equals(clone));
        } catch (CloneNotSupportedException c) {
            Assertions.fail("Cloning column failure.");
        }
    }

    @Test
    @DisplayName("lineageNodeNamingConvention")
    void lineageNodeSetName() {
        // Basic name.
        LineageNode node = new LineageNode(Constants.Node.TYPE_TABLE, "name");
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

    @Test
    @DisplayName("testMultipleIdentifiers")
    void testMultipleIdentifiers() {
        String multipleIdentifiersSelectStatement = "select a * b as c, d from mytable###";
        List<LineageNode> nodeList = LineageExtractor
                .extractLineageWithAnonymousTables(multipleIdentifiersSelectStatement).getNodeList();

        // Expected source table.
        LineageNode myTable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        Column a = new Column("a");
        Column b = new Column("b");
        Column d = new Column("d");
        myTable.addListOfColumns(Arrays.asList(a, b, d));

        // Expected anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON);
        anonymousTable.setName(Constants.Node.TYPE_ANON.concat("0"));
        Column c = new Column("c");
        c.addListOfSources(Arrays.asList("mytable::a", "mytable::b"));
        d.addSource("mytable::d");
        anonymousTable.addListOfColumns(Arrays.asList(c, d));

        Assertions.assertEquals(2, nodeList.size(), "nodeList size");
        Assertions.assertTrue(myTable.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));

        // While we have the expected tables constructed, test more statements with the
        // same expected output
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
        LineageNode sourceNode = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column a = new Column("a");
        sourceNode.addColumn(a);

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
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
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "tablename");
        Column column1 = new Column("column1");
        Column column2 = new Column("column2");
        Column dateColumn = new Column("someDate");
        table.addListOfColumns(Arrays.asList(column1, column2, dateColumn));

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
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
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
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
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "b", "c");
        table.addColumn(new Column("a"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
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
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("a"));
        table.addColumn(new Column("b"));

        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
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
    @DisplayName("testCreateTable")
    void testCreateTable() {
        String sql = "CREATE TABLE createdTable(" +
                        "col1 varchar," +
                        "col2 double" +
                     ")###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        LineageNode createdTable = new LineageNode("TABLE", "createdtable");
        createdTable.addListOfColumns(Arrays.asList(new Column("col1"), new Column("col2")));

        Assertions.assertEquals(1, nodeList.size());
        createdTable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testCreateTableAsSelect")
    void testCreateTableAsSelect() {
        String sql = "CREATE TABLE createdtable AS SELECT a, b FROM existingtable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        Column a = new Column("a");
        Column b = new Column("b");
        existingTable.addListOfColumns(Arrays.asList(a, b));

        LineageNode createdTable = new LineageNode("TABLE", "createdtable");
        a.addSource(DataLineage.makeId(existingTable.getName(), a.getName()));
        b.addSource(DataLineage.makeId(existingTable.getName(), b.getName()));
        createdTable.addListOfColumns(Arrays.asList(a, b));

        Assertions.assertEquals(2, nodeList.size());
        existingTable.equals(nodeList.get(0));
        createdTable.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testCreateView")
    void testCreateView() {
        String statement = "CREATE VIEW a AS SELECT b from c###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(statement).getNodeList();

        // Source table.
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        table.addColumn(new Column("b"));

        // View.
        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "a");
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
        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, "b");

        // Anonymous table.
        LineageNode anonymousTable = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column columnA = new Column("*");
        columnA.addSource("b::*");
        anonymousTable.addColumn(columnA);

        Assertions.assertEquals(2, nodeList.size());
        Assertions.assertTrue(table.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymousTable.equals(nodeList.get(1)));
    }

    @Test
    @DisplayName("testMultipleStatements")
    void testMultipleStatements() {
        String multipleStatements = "SELECT a FROM b### SELECT c FROM d###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleStatements)
                .getNodeList();

        // Source table (first statement).
        LineageNode firstSource = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column a = new Column("a");
        firstSource.addColumn(a);

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Source table (second statement).
        LineageNode secondSource = new LineageNode(Constants.Node.TYPE_TABLE, "d");
        Column c = new Column("c");
        secondSource.addColumn(c);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
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
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(multipleReferences)
                .getNodeList();

        // Source table (both statements).
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column a = new Column("a");
        Column c = new Column("c");
        source.addListOfColumns(Arrays.asList(a, c));

        // Anonymous table (first statement).
        LineageNode firstAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("b::a");
        firstAnonymous.addColumn(a);

        // Anonymous table (second statement).
        LineageNode secondAnonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
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
        String sql = "CREATE VIEW view AS " + "SELECT b " + "FROM (" + "SELECT b " + "FROM B" + ") AS A" + "###";

        // Source table.
        LineageNode source = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column b = new Column("b");
        source.addColumn(b);

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        anonymous.setAlias("A");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        anonymous.addColumn(b);

        // View.
        LineageNode view = new LineageNode(Constants.Node.TYPE_VIEW, "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(anonymous.getName(), b.getName()));
        view.addColumn(b);

        // First, verify that the anonymous table is produced correctly as the
        // intermediate table.
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        Assertions.assertEquals(3, nodeList.size());
        Assertions.assertTrue(source.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous.equals(nodeList.get(1)));
        Assertions.assertTrue(view.equals(nodeList.get(2)));

        // Now extract the lineage, including the step where the anonymous tables are
        // bypassed.
        nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        // Adjust the view, it's column's sources have now bypassed the anonymous table.
        view = new LineageNode(Constants.Node.TYPE_VIEW, "view");
        b = new Column("b");
        b.addSource(DataLineage.makeId(source.getName(), b.getName()));
        view.addColumn(b);

        // Check the resultant lineage is as expected.
        Assertions.assertEquals(2, nodeList.size());
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
        LineageNode crmsTask = new LineageNode(Constants.Node.TYPE_TABLE, crmsTaskName);
        Column accountid = new Column("accountid");
        Column ownerid = new Column("ownerid");
        Column status = new Column("status");
        Column activityDate = new Column("activitydate");
        crmsTask.addListOfColumns(Arrays.asList(accountid, ownerid, status, activityDate));

        // customer_insight table.
        String customerInsightName = "customer_insight";
        LineageNode customerInsight = new LineageNode(Constants.Node.TYPE_TABLE, customerInsightName);
        Column acctSfId = new Column("acct_sf_id");
        Column userSfId = new Column("user_sf_id");
        customerInsight.addListOfColumns(Arrays.asList(acctSfId, userSfId));
        customerInsight.setAlias("b");

        // ANONYMOUS0.
        LineageNode anonymous0 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        accountid.addSource(DataLineage.makeId(crmsTaskName, accountid.getName()));
        ownerid.addSource(DataLineage.makeId(crmsTaskName, ownerid.getName()));
        status.addSource(DataLineage.makeId(crmsTaskName, status.getName()));
        Column taskNoteDate = new Column("task_note_date");
        taskNoteDate.addSource(DataLineage.makeId(crmsTaskName, activityDate.getName()));
        anonymous0.addListOfColumns(Arrays.asList(accountid, ownerid, status, taskNoteDate));
        anonymous0.setAlias("a");

        // Anonymous1.
        LineageNode anonymous1 = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        acctSfId.addSource(DataLineage.makeId(customerInsightName, acctSfId.getName()));
        userSfId.addSource(DataLineage.makeId(customerInsightName, userSfId.getName()));
        anonymous1.addListOfColumns(Arrays.asList(acctSfId, userSfId));

        // View.
        String view0Name = "note_count_by_agent";
        LineageNode view0 = new LineageNode(Constants.Node.TYPE_VIEW, view0Name);
        acctSfId = new Column("acct_sf_id");
        acctSfId.addSource(DataLineage.makeId(Constants.Node.TYPE_ANON.concat("1"), acctSfId.getName()));
        userSfId = new Column("user_sf_id");
        userSfId.addSource(DataLineage.makeId(Constants.Node.TYPE_ANON.concat("1"), userSfId.getName()));
        Column cnt = new Column("cnt");
        view0.addListOfColumns(Arrays.asList(acctSfId, userSfId, cnt));

        // View (second statement).
        String view1Name = "agent_prediction_obj";
        LineageNode view1 = new LineageNode(Constants.Node.TYPE_VIEW, view1Name);
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

        customerInsight.addListOfColumns(Arrays.asList(address_detail_pid, acctName, spark, kafka, clientAgentScore,
                caseE, caseF, caseG, primaryOwner, allOwners));

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

        view1.addListOfColumns(Arrays.asList(address_detail_pid, acctSfId, acctName, userSfId, spark, kafka,
                clientAgentScore, caseE, caseF, caseG, primaryOwner, allOwners, noteCnt));

        Assertions.assertEquals(6, nodeList.size());
        Assertions.assertTrue(crmsTask.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous0.equals(nodeList.get(1)));
        Assertions.assertTrue(customerInsight.equals(nodeList.get(2)));
        Assertions.assertTrue(anonymous1.equals(nodeList.get(3)));
        Assertions.assertTrue(view0.equals(nodeList.get(4)));
        // TODO: the table not tested here is %(db)s.customer_insight
        // The behaviour here depends on how the database name prefix is handled.
        // The correct behaviour has not been determined yet.
        Assertions.assertTrue(view1.equals(nodeList.get(5)));
    }

    @Test
    @DisplayName("testNumericSelectValues")
    void testNumbericSelectValues() {
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
    @DisplayName("testDereferencedWildcard")
    void testDereferencedWildcard() {
        String sql = "SELECT a.*, b.c FROM a INNER JOIN b ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table a.
        LineageNode sourceA = new LineageNode(Constants.Node.TYPE_TABLE, "a");

        // Source table b.
        LineageNode sourceB = new LineageNode(Constants.Node.TYPE_TABLE, "b");
        Column c = new Column("c");
        sourceB.addColumn(c);

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column wildcard = new Column("*");
        wildcard.addSource("a::*");
        c.addSource("b::c");
        anonymous.addListOfColumns(Arrays.asList(wildcard, c));

        Assertions.assertEquals(3, nodeList.size());
        sourceA.equals(nodeList.get(0));
        sourceB.equals(nodeList.get(1));
    }
  
    @Test
    @DisplayName("testStandAloneLiteralTable")
    void testStandAloneLiteralTable() {
        String sql = "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        Assertions.assertEquals(0, nodeList.size());
    }

    @Test
    @DisplayName("testLiteralInlineTable")
    void testLiteralInlineTables() {

        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ")###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column b = new Column("b");
        inlineLiteral.addColumn(b);

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        b.addSource("ANONYMOUS0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
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
    @DisplayName("testLiteralInlineTableWithAlias")
    void testLiteralInlineTablesWithAlias() {

        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ") AS a###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "a");
        Column b = new Column("b");
        inlineLiteral.addColumn(b);

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        b.addSource("ANONYMOUS0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testMultipleAliasesWithinSelectItem")
    void testMultipleAliasesWithinSelectItem() {
        String sql = "SELECT cast(a AS date) AS b FROM c###";
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
    @DisplayName("testLiteralInlineTableWithAliasAndColumnLabels")
    void testLiteralInlineTablesWithAliasAndColumnLabels() {

        String sql = "SELECT b FROM ( " + "VALUES " + "(1, 'a')," + "(2, 'b')," + "(3, 'c')" + ") AS a (b, c)###";

        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Inline literal table.
        LineageNode inlineLiteral = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"), "a");
        Column b = new Column("b");
        inlineLiteral.addListOfColumns(Arrays.asList(b, new Column("c")));

        // Anonymous table (from select statement).
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("1"));
        b.addSource("ANONYMOUS0::b");
        anonymous.addColumn(b);

        Assertions.assertEquals(2, nodeList.size());
        inlineLiteral.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testInsertStatement")
    void testInsertStatement() {
        String sql = "INSERT INTO existingTable VALUES a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // The only table that will be derived from this.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");

        Assertions.assertEquals(1, nodeList.size());
        existingTable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testInsertFromSelect")
    void testInsertFromSelect() {
        String sql = "INSERT INTO existingTable SELECT * FROM a###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table a.
        LineageNode sourceA = new LineageNode("TABLE", "a");

        // Anonymous table from select statement.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        Column wildcard = new Column("*");
        wildcard.addSource("a::*");
        anonymous.addColumn(wildcard);

        // The existing table that is having values inserted.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        wildcard = new Column("*");
        wildcard.addSource(DataLineage.makeId(anonymous.getName(), wildcard.getName()));
        existingTable.addColumn(wildcard);

        Assertions.assertEquals(3, nodeList.size());
        sourceA.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
        existingTable.equals(nodeList.get(2));
    }

    @Test
    @DisplayName("testInsertWithListedColumnsAndInlineLiteral")
    void testInsertWithListedColumnsAndInlineLiteral() {
        String sql = "INSERT INTO existingTable (a, b, c) VALUES d, e, f###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // The existing table that is having values inserted.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        existingTable.addListOfColumns(Arrays.asList(new Column("a"), new Column("b"), new Column("c")));

        Assertions.assertEquals(1, nodeList.size());
        existingTable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testInsertWithListedColumnsAndSelect")
    void testInsertWithListedColumnsAndSelect() {
        String sql = "INSERT INTO existingTable (a, b, c) SELECT d, e, f FROM sourceTable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table.
        LineageNode sourceTable = new LineageNode("TABLE", "sourcetable");
        Column d = new Column("d");
        Column e = new Column("e");
        Column f = new Column("f");
        sourceTable.addListOfColumns(Arrays.asList(d, e, f));

        // Anonymous table from select statement.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        d.addSource(DataLineage.makeId(sourceTable.getName(), d.getName()));
        e.addSource(DataLineage.makeId(sourceTable.getName(), e.getName()));
        f.addSource(DataLineage.makeId(sourceTable.getName(), f.getName()));
        anonymous.addListOfColumns(Arrays.asList(d, e, f));

        // The existing table that is having values inserted.
        LineageNode existingTable = new LineageNode("TABLE", "existingtable");
        Column a = new Column("a");
        Column b = new Column("b");
        Column c = new Column("c");
        a.addSource(DataLineage.makeId(anonymous.getName(), d.getName()));
        b.addSource(DataLineage.makeId(anonymous.getName(), e.getName()));
        c.addSource(DataLineage.makeId(anonymous.getName(), f.getName()));
        existingTable.addListOfColumns(Arrays.asList(a, b, c));

        Assertions.assertEquals(3, nodeList.size());
        sourceTable.equals(nodeList.get(0));
        anonymous.equals(nodeList.get(1));
        existingTable.equals(nodeList.get(2));
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
    @DisplayName("testMultipleSources")
    void testMultipleSources() {
        String sql = "SELECT table1.a, table2.b " + "FROM table1 INNER JOIN table2 ON 1 = 1### ";
        List<LineageNode> nodeList = LineageExtractor.extractLineageWithAnonymousTables(sql).getNodeList();

        // Source table1.
        LineageNode table1 = new LineageNode(Constants.Node.TYPE_TABLE, "table1");
        Column a = new Column("a");
        table1.addColumn(a);

        // Source table2.
        LineageNode table2 = new LineageNode(Constants.Node.TYPE_TABLE, "table2");
        Column b = new Column("b");
        table2.addColumn(b);

        // Anonymous table.
        LineageNode anonymous = new LineageNode(Constants.Node.TYPE_ANON, Constants.Node.TYPE_ANON.concat("0"));
        a.addSource("table1::a");
        b.addSource("table2::b");
        anonymous.addListOfColumns(Arrays.asList(a, b));

        Assertions.assertEquals(3, nodeList.size());
        table1.equals(nodeList.get(0));
        table2.equals(nodeList.get(1));
        anonymous.equals(nodeList.get(2));
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

    @Test
    @DisplayName("testRenameColumn")
    void testRenameColumn()  {
        String sql = "SELECT a FROM mytable### ALTER TABLE mytable RENAME COLUMN a TO b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode tableAfterRename = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        tableAfterRename.addColumn(new Column("b"));

        Assertions.assertEquals(1, nodeList.size());
        tableAfterRename.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testAddColumn")
    void testAddColumn() {
        String sql = "ALTER TABLE mytable ADD COLUMN a varchar###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode mytable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        mytable.addColumn(new Column("a"));

        Assertions.assertEquals(1, nodeList.size());
        mytable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testConditionalSelectItems")
    void testConditionalSelectItems() {
        String sql = "SELECT CASE WHEN a = b THEN c ELSE d END FROM mytable###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode mytable = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        mytable.addListOfColumns(Arrays.asList(
                new Column("a"),
                new Column("b"),
                new Column("c"),
                new Column("d")
        ));

        Assertions.assertEquals(1, nodeList.size());
        mytable.equals(nodeList.get(0));
    }

    @Test
    @DisplayName("testDereferenceConditionalSelectItems")
    void testDereferenceConditionalSelectItems() {
        String sql = "SELECT CASE " + "WHEN lefttable.a = righttable.b " + "THEN lefttable.c " + "ELSE righttable.d "
                + "END FROM lefttable INNER JOIN righttable ON 1 = 1###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode leftTable = new LineageNode(Constants.Node.TYPE_TABLE, "lefttable");
        leftTable.addListOfColumns(Arrays.asList(
                new Column("a"),
                new Column("c")
        ));

        LineageNode rightTable = new LineageNode(Constants.Node.TYPE_TABLE, "righttable");
        rightTable.addListOfColumns(Arrays.asList(
                new Column("b"),
                new Column("d")
        ));

        for (LineageNode node : nodeList) {
            PrettyPrinter.printLineageNode(node);
        }

        Assertions.assertEquals(2, nodeList.size());
        leftTable.equals(nodeList.get(0));
        rightTable.equals(nodeList.get(1));
    }

    @Test
    @DisplayName("testPrepareStatement")
    void testPrepareStatement() {
        String sql = "PREPARE mytable FROM SELECT a, b FROM c###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(sql).getNodeList();

        LineageNode tableC = new LineageNode(Constants.Node.TYPE_TABLE, "c");
        Column columnA = new Column("a");
        Column columnB = new Column("b");
        tableC.addListOfColumns(Arrays.asList(columnA, columnB));

        LineageNode prepareNode = new LineageNode(Constants.Node.TYPE_TABLE, "mytable");
        columnA.addSource(DataLineage.makeId(tableC.getName(), columnA.getName()));
        columnB.addSource(DataLineage.makeId(tableC.getName(), columnB.getName()));
        prepareNode.addListOfColumns(Arrays.asList(columnA, columnB));

        Assertions.assertEquals(2, nodeList.size());
        tableC.equals(nodeList.get(0));
        prepareNode.equals(nodeList.get(1));
    }
}
