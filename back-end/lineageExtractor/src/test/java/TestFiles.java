import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestFiles {

    @Test
    @DisplayName("testAgentLeads")
    void testAgentLeads() {
        String agentLeadsSql = FileReader.ReadFile("../../propic_sql_scripts/agent_leads.sql");
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

}
