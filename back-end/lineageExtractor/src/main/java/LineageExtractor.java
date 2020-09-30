import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.Statement;

import java.util.List;

public class LineageExtractor {
    public static String extractLineageAsJson(String sql)  {
        return extractLineage(sql).getNodeListAsJson();
    }

    public static DataLineage extractLineageWithAnonymousTables(String sql) {
        return extractLineage(sql, false);
    }

    public static DataLineage extractLineage(String sql) {
        return extractLineage(sql, true);
    }

    public static DataLineage extractLineage(String sql, boolean bypassAnonymousTables) {
        
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
            if (bypassAnonymousTables) {
                dataLineage.bypassAnonymousTables();
                dataLineage.clearAllAliases();
            }
        }
        return dataLineage;
    }
}
