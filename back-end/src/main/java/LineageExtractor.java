import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.Statement;

import java.util.List;

public class LineageExtractor {

    private static DataLineage dataLineage = new DataLineage("lineage_output.json");

    public static DataLineage extractLineage(String sql) {
        List<StatementSplitter.Statement> statements = SivtParser.getStatements(sql);

        // For testing.
        SivtParser.printAstOfFirstStatement(statements);

        SivtVisitor<Node, ?> sivtVisitor = new SivtVisitor<Node, Object>();

        // Iterate through each statement.
        // Use the SivtParser to parse the statement.
        // Call the accept method to traverse the AST for that statement.
        for (StatementSplitter.Statement statement : statements) {
            Statement parsedStatement = SivtParser.parse(statement);
            // TODO: add the newly extracted LineageNodes to the actual dataLineage here.
            sivtVisitor.extractLineage(parsedStatement);
        }
        return dataLineage;
    }
}