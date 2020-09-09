import com.facebook.presto.sql.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

class SivtVisitor<R, C> extends AstVisitor<R, C> {

    private static final ArrayList<LineageNode> lineageNodes = new ArrayList<>();

    private static final Stack<ArrayList<Column>> columnsStack = new Stack<>();
    private static final Stack<ArrayList<LineageNode>> tableStack = new Stack<>();
    private static final Stack<String> aliasStack = new Stack<>();

    /**
     * Stack to maintain the class context.
     * This is important for when a node is visited and its behaviour depends on the context from which it was called.
     * For example, when a table name identifier is visited, it must know if it has been reached in the context of an
     * aliased relation or not so that the table can be associated with the correct alias if one exists.
     */
    private static final Stack<Class> currentlyInside = new Stack<>();

    /**
     * Extract the lineage from a statement.
     * @param statement The statement which will have its AST traversed recursively to extract the lineage.
     * @return A list of the lineage nodes that have been extracted from statement
     */
    public ArrayList<LineageNode> extractLineage(Statement statement) {
        statement.accept(this, null);
        return lineageNodes;
    }

    // Override visit functions here.
}