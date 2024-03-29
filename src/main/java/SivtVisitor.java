import com.facebook.presto.sql.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;
import java.util.HashMap;

class SivtVisitor<R, C> extends AstVisitor<R, C> {

    final static Logger LOGGING = LoggerFactory.getLogger(SivtVisitor.class);

    private final ArrayList<LineageNode> lineageNodes = new ArrayList<>();
    private DataLineage existingLineage;

    private final Stack<SelectStatement> selectStatementStack = new Stack<>();
    private final Stack<ArrayList<LineageNode>> sourcesStack = new Stack<>();
    private final Stack<LabellingInformation> labellingInformationStack = new Stack<>();
    private final Stack<ArrayList<ColumnDefinition>> columnDefinitionStack = new Stack<>();
    private final HashMap<String, LineageNode> withTables = new HashMap<>();

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
            new ArrayList<Class>(Arrays.asList(TableSubquery.class, CreateView.class, Insert.class, Prepare.class, CreateTable.class, CreateTableAsSelect.class, WithQuery.class));

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
    public ArrayList<LineageNode> extractLineage(Statement statement, DataLineage existingLineage) {
        this.existingLineage = existingLineage;
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
     * Convert a LineageNode to a TABLE type.
     * @param node The LineageNode that is to be converted into a TABLE.
     * @param tableName The name of the table to be created.
     */
    private void convertNodeToTable(LineageNode node, String tableName) {
        node.setType(Constants.Node.TYPE_TABLE);
        node.setName(tableName);
        node.setAlias("");

        for (Column column : node.getColumns()) {
            column.setID(DataLineage.makeId(tableName, column.getName()));
        }
    }

    /**
     * Adds the columns from the given column definitions to the table.
     * @param table The table to have columns populated.
     * @param columnDefinitions The column definitions that define columns within the table.
     */
    void applyColumnDefinitionsToTable(LineageNode table, ArrayList<ColumnDefinition> columnDefinitions) {
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            table.addColumn(new Column(columnDefinition.getName().getValue()));
        }
    }

    /**
     * Visit a CreateTable node in the AST.
     *
     * @param createTable The CreateTable node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitCreateTable(CreateTable createTable, C context)  {
        columnDefinitionStack.push(new ArrayList<>());

        R node = visitStatement(createTable, context);

        LineageNode table = new LineageNode("TABLE", createTable.getName().getSuffix());
        applyColumnDefinitionsToTable(table, columnDefinitionStack.pop());
        lineageNodes.add(table);

        return node;
    }

    /**
     * Visit a ColumnDefinition node in the AST.
     *
     * @param columnDefinition The ColumnDefinition node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitColumnDefinition(ColumnDefinition columnDefinition, C context)
    {
        if (!columnDefinitionStack.isEmpty()) columnDefinitionStack.peek().add(columnDefinition);
        return visitTableElement(columnDefinition, context);
    }

    /**
     * Visit a CreateTableAsSelect node in the AST.
     *
     * @param createTableAsSelect The CreateView node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitCreateTableAsSelect(CreateTableAsSelect createTableAsSelect, C context) {
        sourcesStack.push(new ArrayList<>());

        currentlyInside.push(CreateTableAsSelect.class);
        R node = visitStatement(createTableAsSelect, context);
        currentlyInside.pop();

        LineageNode table = sourcesStack.pop().get(0);
        convertNodeToTable(table, createTableAsSelect.getName().getSuffix());
        lineageNodes.add(table);

        return node;
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
        convertNodeToView(view, createView.getName().getSuffix());
        lineageNodes.add(view);

        return node;
    }

    /**
     * Visit an Insert node in the AST.
     *
     * @param insert The Insert node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitInsert(Insert insert, C context) {

        // Make room for the sources table which contains the insert values.
        sourcesStack.push(new ArrayList<>());

        currentlyInside.push(Insert.class);
        R node = visitStatement(insert, context);
        currentlyInside.pop();

        // Get the lineage nodes that result from the INSERT statement.
        InsertStatement insertStatement = new InsertStatement(insert, sourcesStack.pop());
        ArrayList<LineageNode> nodes = insertStatement.getLineageNodes();

        // Add the source node (if exists).
        if (nodes.size() > 1) lineageNodes.add(nodes.get(1));

        // Add the target node.
        lineageNodes.add(nodes.get(0));
        return node;
    }

    /** Visit the Prepare node in the AST.
     * A prepare statement produces a table that can be referenced in later statements.
     * As a result, the generated table cannot just be an anonymous table because that would lose
     * its ability to be referenced later by the assigned name. Therefore, prepare statements simply
     * generate a first class lineage node.
     * @param prepare The Prepare node.
     * @param context The context
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitPrepare(Prepare prepare, C context) {

        sourcesStack.push(new ArrayList<>());

        currentlyInside.push(Prepare.class);
        R node = visitStatement(prepare, context);
        currentlyInside.pop();

        LineageNode prepareNode = sourcesStack.pop().get(0);
        convertNodeToTable(prepareNode, prepare.getName().getValue());
        lineageNodes.add(prepareNode);

        return node;
    }

    @Override
    protected R visitWithQuery(WithQuery withQuery, C context) {

        sourcesStack.push(new ArrayList<>());

        currentlyInside.push(WithQuery.class);
        R node = visitNode(withQuery, context);
        currentlyInside.pop();

        if (sourcesStack.peek().size() != 1) LOGGING.warn("Sources stack not maintained correctly for WITH query");
        LineageNode withTable = sourcesStack.pop().get(0);
        withTable.setAlias(withQuery.getName().getValue());
        lineageNodes.add(withTable);
        withTables.put(withTable.getAlias(), withTable);

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

        // TODO: Assumption - at the conclusion of recursing through the children of a TableSubquery,
        //       there will be a single anonymous table on the sources stack.
        //       This assumption should be investigated more thoroughly to ensure it is correct.
        if (sourcesStack.peek().size() != 1) {
            LOGGING.warn("There was not a single source table after recursing through a subquery");
        }

        // Give the result of the subquery its alias if it has one.
        if (isCurrentlyInside(AliasedRelation.class)) {
            LabellingInformation labellingInformation = labellingInformationStack.pop();
            sourcesStack.peek().get(0).setAlias(labellingInformation.getAlias());
            sourcesStack.peek().get(0).addListOfColumns(labellingInformation.getColumns());
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

        boolean isParentOfSelectStatement = querySpecification.getFrom().isPresent();

        // Push a new list to the sources stack for this query.
        sourcesStack.push(new ArrayList<>());

        // Recursively visit all the children of this node.
        currentlyInside.push(QuerySpecification.class);
        R query_body = visitQueryBody(querySpecification, context);
        currentlyInside.pop();

        if (!isParentOfSelectStatement) return query_body;

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

        selectStatementStack.push(new SelectStatement(existingLineage));

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
     * Correctly account for dereferenced wildcard select items, Eg. SELECT a.* FROM b;.
     *
     * This is required because a dereferenced wildcard is not actually treated as a DereferenceExpression
     * like ordinary dereference expressions are in the AST.
     * TODO: This method of dealing with dereference wildcards is a work-around and is not the ideal way.
     *       This functionality belongs in the proper method that the AST visitor recurses to to deal with
     *       dereferenced wildcards but I can't figure out which method this would be.
     *       Functionally, this work-around operates correctly.
     * @param selectItem A select item that may be a dereferenced wildcard.
     */
    private void accountForDereferenceWildcard(com.facebook.presto.sql.tree.SelectItem selectItem) {

        boolean hasIdentifier = selectStatementStack.peek().currentSelectItem().getIdentifiers().size() != 0;
        if (hasIdentifier) return;

        String[] dereferenceParts = selectItem.toString().split(Constants.PrestoSQLSyntax.DEREFERENCE_DELIM_REGEX);

        boolean isDereferenceWildcard = dereferenceParts.length == 2 && dereferenceParts[1].equals(Constants.WILDCARD);
        if (!isDereferenceWildcard) return;

        // Update the current select item with the dereferenced wildcard.
        String base = dereferenceParts[0];
        String field = dereferenceParts[1];
        selectStatementStack.peek().currentSelectItem().addIdentifier(base, field);
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

        accountForDereferenceWildcard(selectItem);

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
        String alias = aliasedRelation.getAlias().toString();
        List<com.facebook.presto.sql.tree.Identifier> columnNames = aliasedRelation.getColumnNames();
        labellingInformationStack.push(new LabellingInformation(alias, columnNames));

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

        String tableName = table.getName().toString();
        LineageNode node;
        if (withTables.containsKey(tableName)) {
            node = withTables.get(tableName);
        } else {
            node = new LineageNode(Constants.Node.TYPE_TABLE, tableName);
        }

        // Get the alias if we are within an AliasedRelation context.
        if (isCurrentlyInside(AliasedRelation.class)) {
            LabellingInformation labellingInformation = labellingInformationStack.pop();
            node.setAlias(labellingInformation.getAlias());
            node.addListOfColumns(labellingInformation.getColumns());
        }

        // Append the new node to the list that is on top of the stack.
        sourcesStack.peek().add(node);

        return visitQueryBody(table, context);
    }

    /**
     * Visit a Values node in the AST.
     *
     * This keyword generates an inline literal table. Therefore, a new anonymous table is required to be pushed
     * to the sourcesStack.
     * @param values The values node.
     */
    protected R visitValues(Values values, C context) {

        if (isCurrentlyInside(TableSubquery.class)) {
            sourcesStack.peek().add(new LineageNode(Constants.Node.TYPE_ANON, Util.getNextAnonymousTableName()));
        }

        currentlyInside.push(Values.class);
        R node = visitQueryBody(values, context);
        currentlyInside.pop();

        return node;
    }

     /** Visit the RenameTable node of the AST.
     * @param renameTable The RenameTable node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitRenameTable(RenameTable renameTable, C context) {

        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, renameTable.getSource().getSuffix());
        table.stageRenameTo(renameTable.getTarget().getSuffix());
        lineageNodes.add(table);

        return visitStatement(renameTable, context);
    }

    /**
     * Visit the RenameColumn node of the AST.
     * @param renameColumn The RenameColumn node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitRenameColumn(RenameColumn renameColumn, C context) {

        LineageNode table = new LineageNode(Constants.Node.TYPE_TABLE, renameColumn.getTable().getSuffix());
        Column column = new Column(renameColumn.getSource().getValue());
        column.stageRenameTo(renameColumn.getTarget().getValue());
        table.addColumn(column);
        lineageNodes.add(table);

        return visitStatement(renameColumn, context);
    }

    /**
     * Visit the addColumn node of the AST.
     * @param addColumn The AddColumn node.
     * @param context The context.
     * @return The result of recursively visiting the children.
     */
    @Override
    protected R visitAddColumn(AddColumn addColumn, C context) {

        LineageNode node = new LineageNode(Constants.Node.TYPE_TABLE, addColumn.getName().getSuffix());
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
        if (node.toString().equals(Constants.WILDCARD)) {
            if (isCurrentlyInside(com.facebook.presto.sql.tree.SelectItem.class)) {
                selectStatementStack.peek().currentSelectItem().addIdentifier(Constants.WILDCARD);
            }
        }

        List<? extends Node> children = node.getChildren();
        for (Node child : children) {
            this.process(child, context);
        }
        return null;
    }
}