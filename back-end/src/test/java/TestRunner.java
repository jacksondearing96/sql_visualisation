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
        column.addListOfSources(sources);
        Assertions.assertEquals(
                "alias=newAlias,id=newID,name=newName,sources={source1,source2,source3,source1,source2}",
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
    @DisplayName("testLineageNodes")
    void testLineageNodes() {
        String simpleSelect = "SELECT a FROM b###";
        List<LineageNode> nodeList = LineageExtractor.extractLineage(simpleSelect).getNodeList();

        // Anonymous table.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous0", "");
        Column anonymousColumn = new Column("a", "", "Anonymous0::a");
        anonymousColumn.addSource("b::a");
        anonymousNode.addColumn(anonymousColumn);

        // Source table.
        LineageNode sourceNode = new LineageNode("TABLE", "b", "");
        Column sourceColumn = new Column("a", "", "b::a");
        sourceColumn.addSource("b::a");
        sourceNode.addColumn(sourceColumn);

        // Compare the pair.
        boolean success = true;
        success &= nodeList.get(1).equals(anonymousNode);
        success &= nodeList.get(0).equals(sourceNode);

        Assertions.assertTrue(success);
    }

    @Test
    @DisplayName("testAgentLeads")
    void testAgentLeads() {
        String agentLeadsSql = FileReader.ReadFile("../propic_sql_scripts/agent_leads.sql");
        List<LineageNode> nodeList = LineageExtractor.extractLineage(agentLeadsSql).getNodeList();

        // crms_task table.
        String crmsTaskName = "%(db)s.%(crm)s_task";
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
        String view0Name = "%(db)s.note_count_by_agent";
        LineageNode view0 = new LineageNode("VIEW", view0Name);
        acctSfId.clearSources();
        acctSfId.addSource(DataLineage.makeId("Anonymous1", acctSfId.getName()));
        userSfId.clearSources();
        userSfId.addSource(DataLineage.makeId("Anonymous1", userSfId.getName()));
        Column cnt = new Column("cnt");
        view0.addListOfColumns(Arrays.asList(acctSfId, userSfId, cnt));

        // View (second statement).
        customerInsightName = "%(db)s.customer_insight";
        String view1Name =  "%(db)s.agent_prediction_obj";
        LineageNode view1 = new LineageNode("VIEW", view1Name);
        Column address_detail_pid = new Column("address_detail_pid");
        acctSfId.clearSources();
        Column acctName = new Column("acct_name");
        userSfId.clearSources();
        Column spark = new Column("spark");
        Column kafka = new Column("kafka");
        Column clientAgentScore = new Column("client_agent_score");
        Column caseE = new Column("case_e");
        Column caseF = new Column("case_f");
        Column caseG = new Column("case_g");
        Column primaryOwner = new Column("primary_owner");
        Column allOwners = new Column("all_owners");
        Column noteCnt = new Column("note_cnt");
        address_detail_pid.addSource(DataLineage.makeId(customerInsightName, address_detail_pid.getName()));
        acctSfId.addSource(DataLineage.makeId(customerInsightName, acctSfId.getName()));
        acctName.addSource(DataLineage.makeId(customerInsightName, acctName.getName()));
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

        Assertions.assertEquals(7, nodeList.size());
        Assertions.assertTrue(crmsTask.equals(nodeList.get(0)));
        Assertions.assertTrue(anonymous0.equals(nodeList.get(1)));
        Assertions.assertTrue(customerInsight.equals(nodeList.get(2)));
        Assertions.assertTrue(anonymous1.equals(nodeList.get(3)));
        Assertions.assertTrue(view0.equals(nodeList.get(4)));
        // TODO: there is a %(db)s.customer_insight table produced here that should be consolidated with cusotmer_insight.
        Assertions.assertTrue(view1.equals(nodeList.get(6)));
    }
}
