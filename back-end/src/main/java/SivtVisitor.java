import com.facebook.presto.sql.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.Arrays;

class SivtVisitor<R, C> extends AstVisitor<R, C> {

    private final ArrayList<LineageNode> lineageNodes = new ArrayList<>();

    private final Stack<SelectStatement> selectStatementStack = new Stack<>();
    private final Stack<ArrayList<LineageNode>> sourcesStack = new Stack<>();
    private final Stack<String> aliasStack = new Stack<>();

    /**
     * Stack to maintain the context within which the recursive decent visitor is in.
     * This is important for when a node is visited and its behaviour depends on the context from which it was called.
     * For example, when a table name identifier is visited, it must know if it has been reached in the context of an
     * aliased relation or not so that the table can be associated with the correct alias, if one exists.
     */
    private final Stack<Class> currentlyInside = new Stack<>();

    /**
     * Safe and clean method for checking the current context according to currentlyInside.
     * @param object The class that is being checked against the current context.
     * @return Whether object is equal to the top of 'currentlyInside' context stack.
     */
    private boolean isCurrentlyInside(Class object) {
        return !currentlyInside.isEmpty() && currentlyInside.peek() == object;
    }

    /**
     * Defines all the parent nodes that are interested in keeping the resultant anonymous table to use as a source.
      */
    private final ArrayList<Class> contextToKeepList =
            new ArrayList<Class>(Arrays.asList(TableSubquery.class, CreateView.class));

    /**
     * Determines whether the current Class context is one which is required to keep the anonymous table.
     * @return true if the anonymous table should be kept and false otherwise.
     */
    private boolean isInContextToKeep() {
        for (Class parent : contextToKeepList) {
            if (isCurrentlyInside(parent)) return true;
        }
        return false;
    }

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

        for (Column column : node.getColumns()) {
            column.setID(DataLineage.makeId(viewName, column.getName()));
        }
    }

    /**
     * Visit a CreateView node in the AST.
     *
     * @param createView The CreateView node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
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
        lineageNodes.add(view);

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
        if (isCurrentlyInside(AliasedRelation.class)) {
            sourcesStack.peek().get(0).setAlias(aliasStack.pop());
        }
        return node;
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
        selectStatementStack.push(new SelectStatement());
        sourcesStack.push(new ArrayList<LineageNode>());

        // Recursively visit all the children of this node.
        currentlyInside.push(QuerySpecification.class);
        R query_body = visitQueryBody(querySpecification, context);
        currentlyInside.pop();

        // Extract the source tables and anonymous table from the select statement.
        SelectStatement selectStatement = selectStatementStack.pop();
        selectStatement.setSourceTables(sourcesStack.pop());
        ArrayList<LineageNode> sourceTables = selectStatement.getSourceTables();
        LineageNode anonymouTable = selectStatement.getAnonymousTable();

        // Add the source tables and anonymous table to the list of LineageNodes.
        lineageNodes.addAll(sourceTables);

        // Either save the anonymous table for a parent node that needs it or add it to the
        // list of lineage nodes.
        if (isInContextToKeep() && !sourcesStack.isEmpty()) {
            sourcesStack.peek().add(anonymouTable);
        } else {
            lineageNodes.add(anonymouTable);
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
     * Extract the alias for a particular SelectItem.
     * @param selectItem The SelectItem that will have its alias extracted.
     * @return The alias of the SelectItem (an empty string if no alias is present).
     */
    private String extractAlias(com.facebook.presto.sql.tree.SelectItem selectItem) {
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
    protected R visitSelectItem(com.facebook.presto.sql.tree.SelectItem selectItem, C context) {

        selectStatementStack.peek().addEmptySelectItem();

        currentlyInside.push(com.facebook.presto.sql.tree.SelectItem.class);
        R node = visitNode(selectItem, context);
        currentlyInside.pop();

        // Extract and apply the column's alias.
        selectStatementStack.peek().currentSelectItem().setAlias(extractAlias(selectItem));

        return node;
    }

    /**
     * Visit an Identifier node in the AST.
     * @param identifier The identifier node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitIdentifier(com.facebook.presto.sql.tree.Identifier identifier, C context) {
        if (isCurrentlyInside(com.facebook.presto.sql.tree.SelectItem.class)) {
            selectStatementStack.peek().currentSelectItem().addIdentifier(identifier.getValue());
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
        if (isCurrentlyInside(com.facebook.presto.sql.tree.SelectItem.class)) {
            String base = dereferenceExpression.getBase().toString();
            String field = dereferenceExpression.getField().toString();
            selectStatementStack.peek().currentSelectItem().addIdentifier(base, field);
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
        if (isCurrentlyInside(AliasedRelation.class)) alias = aliasStack.pop();

        // Create a new LineageNode (table) and append it to the list that is on top of the stack.
        sourcesStack.peek().add(new LineageNode("TABLE", table.getName().toString(), alias));

        return visitQueryBody(table, context);
    }

    @Override
    protected R visitRenameTable(RenameTable renameTable, C context) {

        LineageNode table = new LineageNode("TABLE", renameTable.getSource().getSuffix());
        table.stageRenameTo(renameTable.getTarget().getSuffix());
        lineageNodes.add(table);

        return visitStatement(renameTable, context);
    }

    @Override
    protected R visitRenameColumn(RenameColumn renameColumn, C context) {

        LineageNode table = new LineageNode("TABLE", renameColumn.getTable().getSuffix());
        Column column = new Column(renameColumn.getSource().getValue());
        column.stageRenameTo(renameColumn.getTarget().getValue());
        table.addColumn(column);
        lineageNodes.add(table);

        return visitStatement(renameColumn, context);
    }

    @Override
    protected R visitAddColumn(AddColumn addColumn, C context) {

        LineageNode node = new LineageNode("TABLE", addColumn.getName().getSuffix());
        Column newColumn = new Column(addColumn.getColumn().getName().getValue());
        node.addColumn(newColumn);
        lineageNodes.add(node);

        return visitStatement(addColumn, context);
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

        // Columns are usually captured in the visitIdentifier function but wildcard operators
        // are not classed as an Identifier which means they are skipped.
        // Explicitly add wildcard select items here instead.
        if (node.toString().equals("*")) {
            if (isCurrentlyInside(com.facebook.presto.sql.tree.SelectItem.class)) {
                selectStatementStack.peek().currentSelectItem().addIdentifier("*");
            }
        }

        List<? extends Node> children = node.getChildren();
        for (Node child : children) {
            this.process(child, context);
        }
        return null;
    }
}