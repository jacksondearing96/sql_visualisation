import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.Statement;

import java.util.List;

public class LineageExtractor {
    public static DataLineage extractLineage(String sql) {
        DataLineage dataLineage = new DataLineage("lineage_output.json");

        List<StatementSplitter.Statement> statements = SivtParser.getStatements(sql);

        SivtVisitor<Node, ?> sivtVisitor = new SivtVisitor<Node, Object>();

        // Iterate through each statement.
        // Use the SivtParser to parse the statement.
        // Call the accept method to traverse the AST for that statement.
        for (StatementSplitter.Statement statement : statements) {
            Statement parsedStatement = SivtParser.parse(statement);
            dataLineage.addListOfNodes(sivtVisitor.extractLineage(parsedStatement));
        }
        return dataLineage;
    }
}