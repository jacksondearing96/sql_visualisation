import com.facebook.presto.sql.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

class SivtVisitor<R, C> extends AstVisitor<R, C> {

    private static final DataLineage data_lineage = new DataLineage("lineage_output.json");
    private static final Stack<ArrayList<LineageNode>> lineage_nodes_stack = new Stack<>();
    private static final Stack<ArrayList<Column>> columns_stack = new Stack<>();
    private static final Stack<ArrayList<LineageNode>> table_stack = new Stack<>();
    private static final Stack<String> alias_names_stack = new Stack<>();

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
     */
    public void extractLineage(Statement statement) {
        statement.accept(this, null);
    }

    /**
     * Visit a node by first visiting all its children.
     * @param node Node
     * @param context Context
     * @return null - The node is the deepest entity in the AST. Once there is nothing further to visit, terminate the
     *                recursion by returning null.
     */
    @Override
    public final R visitNode(Node node, C context) {
        List<? extends Node> children = node.getChildren();
        for (Node child : children) {
            this.process(child, context);
        }
        return null;
    }

    /**
     * Visit a QuerySpecification node in the AST.
     *
     * A QuerySpecification is the entity in the AST which acts as a wrapper for a few important components including
     * the Select object and the Relation that determines the 'FROM' clause.
     * This is important because the Select object holds the columns and the 'FROM' clause holds the tables and these
     * need to be correlated with each other to track lineage.
     *
     * @param querySpecification The query specification node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitQuerySpecification(QuerySpecification querySpecification, C context) {

        // If this is not a 'SELECT' query specificaiton, return.
        Optional<Relation> from = querySpecification.getFrom();
        if (!from.isPresent()) return visitQueryBody(querySpecification, context);

        // This is a 'SELECT' statement.
        // Push new columns and tables to the stacks ready to be populated.
        columns_stack.push(new ArrayList<Column>());
        table_stack.push(new ArrayList<LineageNode>());

        // Recursively visit all the children of this node.
        currentlyInside.push(QuerySpecification.class);
        R query_body = visitQueryBody(querySpecification, context);
        currentlyInside.pop();

        // * For testing *
        // Print out the columns and tables on the stack.
        // This shows columns and the tables which they were derived from.
        System.out.println("\n\n");
        ArrayList<Column> columns = columns_stack.pop();
        for (Column column : columns) {
            System.out.println(column.getName());
        }
        ArrayList<LineageNode> tableNames = table_stack.pop();
        for (LineageNode table : tableNames) {
            System.out.println(table.getName());
            if (table.hasAlias()) {
                System.out.println("\tAS: " + table.getAlias());
            }
        }

        return query_body;
    }

    /**
     * Visit a Select node in the AST.
     * @param select The select node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitSelect(Select select, C context) {
        currentlyInside.push(Select.class);
        R node = visitNode(select, context);
        currentlyInside.pop();

        return node;
    }

    /**
     * Visit a SelectItem node in the AST.
     * @param selectItem The select item node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitSelectItem(SelectItem selectItem, C context) {
        if (!columns_stack.empty()) {
            ArrayList<Column> columns = columns_stack.pop();
            columns.add(new Column(selectItem.toString()));
            columns_stack.push(columns);
        }

        currentlyInside.push(SelectItem.class);
        R node = visitNode(selectItem, context);
        currentlyInside.pop();

        return node;
    }

    /**
     * Visit an AliasedRelation node in the AST.
     * @param aliasedRelation The aliased relation node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitAliasedRelation(AliasedRelation aliasedRelation, C context) {
        alias_names_stack.push(aliasedRelation.getAlias().toString());

        currentlyInside.push(AliasedRelation.class);
        R node = visitRelation(aliasedRelation, context, true);
        currentlyInside.pop();

        return node;
    }

    /**
     * Visit a Relation node in the AST.
     *
     * This method acts as a wrapper for the default visitRelation method. It offers an additional parameter 'isAliased'
     * which controls whether the 'currentlyInside' context should be overridden with a generic Relation context.
     * This is required because all AliasedRelations contain a generic Relation child. Therefore, by default the
     * AliasedRelation context would be overridden by its own child. We only want to override this if the Relation is
     * from some deeper point in the recursive decent.
     *
     * @param relation The relation node.
     * @param context The context.
     * @param isAliased Flag to indicate whether this relation is an immediate child of an AliasedRelation.
     * @return The result of recursively visiting the children.
     */
    protected R visitRelation(Relation relation, C context, boolean isAliased) {

        if (!isAliased) currentlyInside.push(Relation.class);
        R node = visitRelation(relation, context);
        if (!isAliased) currentlyInside.pop();

        return node;
    }

    /**
     * Visit a Table node in the AST.
     *
     * This visitor method combines a table with its associated alias. The presence of an alias is determined using the
     * currentlyInside class context.
     * @param table The table node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitTable(Table table, C context) {
        if (table_stack.empty()) return visitQueryBody(table, context);

        // Get the alias if we are within an AliasedRelation context.
        String alias = "";
        if (currentlyInside.peek() == AliasedRelation.class) alias = alias_names_stack.pop();

        // Create a new LineageNode (table) and append it to the list that is on top of the stack.
        table_stack.peek().add(new LineageNode("TABLE", table.getName().toString(), alias));

        return visitQueryBody(table, context);
    }
}