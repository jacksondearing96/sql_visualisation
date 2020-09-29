import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.Statement;

import java.util.List;

public class LineageExtractor {
    public static String extractLineageAsJson(String sql)  {
        return extractLineage(sql).getNodeListAsJson();
    }

    public static DataLineage extractLineage(String sql) {
        DataLineage dataLineage = extractLineageWithAnonymousTables(sql);
        dataLineage.bypassAnonymousTables();
        return dataLineage;
    }

    public static DataLineage extractLineageWithAnonymousTables(String sql) {
        DataLineage dataLineage = new DataLineage();

        List<StatementSplitter.Statement> statements = SivtParser.getStatements(sql);

        SivtVisitor<Node, ?> sivtVisitor = new SivtVisitor<Node, Object>();

        Util.resetAnonymousTableCount();

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
