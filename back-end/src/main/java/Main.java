public class Main {
    public static void main(String[] args) {
        // Use the FileReader for now.
        // In production, this will be redundant - sql will come in string format from the front-end.
        String sql = FileReader.ReadFile("../propic_sql_scripts/agent_leads.sql");
        DataLineage dataLineage = LineageExtractor.extractLineage(sql);
    }
}