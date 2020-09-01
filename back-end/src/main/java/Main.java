import java.util.List;

import com.facebook.presto.sql.tree.Statement;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.parser.StatementSplitter;

public class Main {
    public static void main(String[] args) {

        // Use the FileReader for now.
        // In production, this will be redundant - sql will come in string format from the
        // front end.
        String sql = FileReader.ReadFile("../propic_sql_scripts/agent_leads.sql");

        List<StatementSplitter.Statement> statements = SivtParser.getStatements(sql);

        // For testing.
        SivtParser.printAstOfFirstStatement(statements);

        SivtVisitor<Node, ?> sivtVisitor = new SivtVisitor<Node, Object>();

        // Iterate through each statement.
        // Use the SivtParser to parse the statement.
        // Call the accept method to traverse the AST for that statement.
        for (StatementSplitter.Statement statement : statements) {
            Statement parsedStatement = SivtParser.parse(statement);
            sivtVisitor.extractLineage(parsedStatement);
        }
    }
}