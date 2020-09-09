import com.facebook.presto.sql.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;

class SivtVisitor<R, C> extends AstVisitor<R, C> {

    private static final ArrayList<LineageNode> lineageNodes = new ArrayList<>();

    private static final Stack<ArrayList<Column>> columnsStack = new Stack<>();
    private static final Stack<ArrayList<LineageNode>> sourcesStack = new Stack<>();
    private static final Stack<String> aliasStack = new Stack<>();

    /**
     * Counter variable used to generate the unique IDs for anonymous tables.
     */
    private static int anonymousTableCount = 0;

    /**
     * Returns the next ID used for allocating unique names to anonymous tables.
     * @return The next unique ID.
     */
    private int getNextAnonymousTableId() {
        return ++anonymousTableCount;
    }

    /**
     * Stack to maintain the context within which the recursive decent visitor is in.
     * This is important for when a node is visited and its behaviour depends on the context from which it was called.
     * For example, when a table name identifier is visited, it must know if it has been reached in the context of an
     * aliased relation or not so that the table can be associated with the correct alias, if one exists.
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

    /**
     * Convert a LineageNode to a VIEW type.
     * @param node The LineageNode that is to be converted into a VIEW.
     * @param viewName The name of the view to be created.
     */
    private void convertNodeToView(LineageNode node, String viewName) {
        node.setType("VIEW");
        node.setName(viewName);
        node.setAlias("");
    }

    @Override
    protected R visitCreateView(CreateView createView, C context) {

        // Push space on the stack for the source LineageNode that is to be received.
        sourcesStack.push(new ArrayList<>());

        currentlyInside.push(CreateView.class);
        R node = visitStatement(createView, context);
        currentlyInside.pop();

        // Mutate the anonymous table that was received to become the view.
        LineageNode view = sourcesStack.pop().get(0);
        convertNodeToView(view, createView.getName().toString());

        return node;
    }

    /**
     * Visit a TableSubquery node in the AST.
     *
     * A TableSubquery is a query in SQL statements that is encapsulated with parentheses. This is used to construct
     * anonymous tables which can then be used by elements higher in the AST.
     * @param tableSubquery The TableSubquery node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitTableSubquery(TableSubquery tableSubquery, C context) {

        currentlyInside.push(TableSubquery.class);
        R node = visitQueryBody(tableSubquery, context);
        currentlyInside.pop();

        // Give the result of the subquery its alias if it has one.
        // TODO: Assumption - at the conclusion of recursing through the children of a TableSubquery, there will be a single anonymous table on the sources stack.
        // This assumption should be investigated more thoroughly to ensure it is correct.
        if (!currentlyInside.isEmpty() && currentlyInside.peek() == AliasedRelation.class) {
            sourcesStack.peek().get(0).setAlias(aliasStack.pop());
        }
        return node;
    }

    /**
     * Reconcile a list of columns and sources to produce the complete lineage nodes. The list of columns and sources
     * are a result of the SELECT statements. For example:
     * SELECT a.x, a.y, b.z
     * FROM table1 AS a
     * INNER JOIN table2 AS b
     *
     * After recursively visiting this SELECT statement, a list of columns (a.x, a.y, b.z) and a list of sources
     * (table1 as a, table2 as b) will available on the stacks. This function is responsible for reconciling this
     * information into dedicated LineageNodes eg:
     *     ______________       ______________
     *    | table1 as a  |     | table2 as b  |
     *    | a.x          |     | b.z          |
     *    | a.y          |     |              |
     *     ---------------      --------------
     * @param columns The columns that are to be populated into the sources tables (and anonymous table).
     * @param sources The source tables that the columns came from.
     */
    public void reconcileColumnsWithSources(ArrayList<Column> columns, ArrayList<LineageNode> sources) {
        // The anonymous lineageNode to be populated - one of these is created by default from every SELECT statement.
        LineageNode anonymousNode = new LineageNode("ANONYMOUS", "Anonymous" + getNextAnonymousTableId());

        for (Column column : columns) {
            for (LineageNode source : sources) {

                // In the case of a single source, the columns won't already have the source recorded in their list of
                // sources (because it will have appeared as a pure identifier in the SQL eg. columnName instead of
                // tableName.columnName.
                // Therefore, explicitly check for the case of a single source.
                if (sources.size() == 1 || source.isSourceOf(column.getSources())) {

                    // When this point is reached, 'source' is a known source of 'column'.
                    // Add this column to the source table.

                    // Filter out the sources of the column if they are the same as the source table's name or alias.
                    Predicate<String> isNameOrAlias = sourceName -> sourceName.equals(source.getAlias()) || sourceName.equals(source.getName());
                    column.getSources().removeIf(isNameOrAlias);

                    try {
                        Column sourceColumn = (Column)column.clone();
                        sourceColumn.setAlias("");
                        sourceColumn.setID(DataLineage.makeId(source.getName(), column.getName()));
                        source.addColumn(sourceColumn);

                        // Add this as a source of the column. This will be for the anonymous table.
                        column.addSource(sourceColumn.getID());
                    } catch(CloneNotSupportedException c) {}

                }

                // Every selected item is added to the anonymous table.
                column.setID(DataLineage.makeId(anonymousNode.getName(), column.getName()));
                anonymousNode.addColumn(column);
            }
        }

        // Define all the parent nodes that are interested in keeping the resultant anonymous table to use as a source.
        ArrayList<Class> contextToKeepList =
                new ArrayList<Class>(Arrays.asList(TableSubquery.class, CreateView.class));

        if (!currentlyInside.isEmpty()){
            for (Class parent : contextToKeepList) {
                if (currentlyInside.peek() == parent) {
                    if (!sourcesStack.isEmpty()) sourcesStack.peek().add(anonymousNode);
                    break;
                }
            }
        }
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
        columnsStack.push(new ArrayList<Column>());
        sourcesStack.push(new ArrayList<LineageNode>());

        // Recursively visit all the children of this node.
        currentlyInside.push(QuerySpecification.class);
        R query_body = visitQueryBody(querySpecification, context);
        currentlyInside.pop();

        // This SELECT statement will have generated a list of columns and sources on their respective stacks.
        // Reconcile these and generate the required LineageNodes.
        reconcileColumnsWithSources(columnsStack.pop(), sourcesStack.pop());

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
     * Extract the alias for a particular SelectItem.
     * @param selectItem The SelectItem that will have its alias extracted.
     * @return The alias of the SelectItem (an empty string if no alias is present).
     */
    private String extractAlias(SelectItem selectItem) {
        // TODO: Could this be done better? Ideally we don't want to rely on string manipulation to extract semantics.
        // There is probably a way to better utilise the type system and the AST to extract what we are after here.
        String[] splitStrings = selectItem.toString().split(" ");
        if (splitStrings.length > 1) return splitStrings[splitStrings.length - 1];
        return "";
    }

    /**
     * Visit a SelectItem node in the AST. Also applies the relevant alias to the column constructed for this SelectItem.
     * @param selectItem The select item node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitSelectItem(SelectItem selectItem, C context) {

        currentlyInside.push(SelectItem.class);
        R node = visitNode(selectItem, context);
        currentlyInside.pop();

        // Extract and apply the column's alias.
        Column currentColumn = columnsStack.peek().get(columnsStack.peek().size() - 1);
        currentColumn.setAlias(extractAlias(selectItem));

        return node;
    }

    /**
     * Visit an Identifier node in the AST.
     * @param identifier The identifier node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitIdentifier(Identifier identifier, C context) {
        if (currentlyInside.peek() == SelectItem.class) {
            columnsStack.peek().add(new Column(identifier.getValue()));
        }
        return visitExpression(identifier, context);
    }

    /**
     * Visit a DereferencedExpression node in the AST.
     * A DereferencedExpression is an identifier-like entity that involves a base and a field.
     * Eg: SELECT a.b, c, d.e FROM tableName
     * Here, a.b is an example DereferencedExpression (base = a, field = b).
     * c is a regualr Identifier
     * d.e is also a DereferencedExpression
     * @param dereferenceExpression The dereferenced expression node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitDereferenceExpression(DereferenceExpression dereferenceExpression, C context) {
        // Add this as a column. The base may be an alias, this can be reconciled later.
        if (currentlyInside.peek() == SelectItem.class) {
            Column column = new Column(dereferenceExpression.getField().getValue());
            column.addSource(dereferenceExpression.getBase().toString());
            columnsStack.peek().add(column);
        }

        currentlyInside.push(DereferenceExpression.class);
        R node = visitExpression(dereferenceExpression, context);
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
        aliasStack.push(aliasedRelation.getAlias().toString());

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
        if (sourcesStack.empty()) return visitQueryBody(table, context);

        // Get the alias if we are within an AliasedRelation context.
        String alias = "";
        if (currentlyInside.peek() == AliasedRelation.class) alias = aliasStack.pop();

        // Create a new LineageNode (table) and append it to the list that is on top of the stack.
        sourcesStack.peek().add(new LineageNode("TABLE", table.getName().toString(), alias));

        return visitQueryBody(table, context);
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
}