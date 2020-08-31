import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.IdentityHashMap;
import java.io.PrintStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.tree.*;
import com.facebook.presto.sql.TreePrinter;
import com.facebook.presto.sql.parser.StatementSplitter;
import static com.facebook.presto.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DECIMAL;

class FileReader {
    public static String ReadFile(String filePath) {
        String data = "";
        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                // Replace newlines with spaces in the SQL scripts for compactness.
                data += " " + myReader.nextLine();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error reading file: " + filePath);
            System.exit(1);
        }
        return data;
    }
}

public class Main {

    // Splits and returns the statements from the string representing the SQL queries.
    // Uses '###' as the delimiter as this is the convention for Propic.
    private static List<StatementSplitter.Statement> getStatements(String sql) {
        Set<String> delimiters = new HashSet<String>();
        delimiters.add("###");
        StatementSplitter statementSplitter = new StatementSplitter(sql, delimiters);
        List<StatementSplitter.Statement> statements = statementSplitter.getCompleteStatements();
        return statements;
    }

    public static void main(String[] args) {
        // All the SQL scripts have been concatenated into the AllStatements.sql file.
        String sql = FileReader.ReadFile("resources/agent_leads.sql");
        List<StatementSplitter.Statement> statements = getStatements(sql);

        // Create the SqlParser.
        SqlParser sqlParser = new SqlParser();
        ParsingOptions.Builder parsingOptionsBuilder = ParsingOptions.builder();
        parsingOptionsBuilder.setDecimalLiteralTreatment(AS_DECIMAL);

        // Use the TreePrinter to print out the AST of the first statement.
        Iterator<StatementSplitter.Statement> iterator = statements.iterator();
        StatementSplitter.Statement firstStatement = iterator.next();
        Statement statementToPrint = sqlParser.createStatement(firstStatement.statement(), parsingOptionsBuilder.build());
        IdentityHashMap<Expression, QualifiedName> resolvedNameReferences = new IdentityHashMap<Expression, QualifiedName>();
        // Just print out to the console for now.
        PrintStream printStream = new PrintStream(System.out);
        TreePrinter treePrinter = new TreePrinter(resolvedNameReferences, printStream);
        treePrinter.print(statementToPrint);

        AstVisitorExtractor<Node, ?> sivtVisitor = AstVisitorExtractor.by(Extractors.sivtVisitor());


        // Iterate through each statement.
        // Use the sqlParser to parse the statement and then extract its table names.
        for (StatementSplitter.Statement splitterStatement : statements) {
            Statement statement = sqlParser.createStatement(splitterStatement.statement(), parsingOptionsBuilder.build());
            statement.accept(sivtVisitor, null)
                    .collect(Collectors.toList());
        }
    }
}


// A custom class used to extend and override the provided AstVisitor.
// This will allow custom traversals of the AST.
class AstVisitorExtractor<R, C> extends AstVisitor<Stream<R>, C> {
    private final AstVisitor<R, C> visitor;

    public AstVisitorExtractor(AstVisitor<R, C> visitor) {
        this.visitor = visitor;
    }

    public static <R, C> AstVisitorExtractor<R, C> by(AstVisitor<R, C> visitor) {
        return new AstVisitorExtractor<>(visitor);
    }

    @Override
    public final Stream<R> visitNode(Node node, C context) {
        Stream<R> nodeResult = Stream.of(visitor.process(node, context));
        Stream<R> childrenResult = node.getChildren().stream()
                .flatMap(child -> process(child, context));

        return Stream.concat(nodeResult, childrenResult)
                .filter(Objects::nonNull);
    }
}

// Custom extractor functions for traversal of the AST.
class Extractors {
    private static DataLineage data_lineage = new DataLineage("lineage_output.json");
    private static Stack<ArrayList<LineageNode>> lineage_nodes_stack = new Stack<>();
    private static Stack<ArrayList<Column>> columns_stack = new Stack<>();
    private static Stack<ArrayList<String>> table_names_stack = new Stack<>();

    public static void unexpectedWarning(String message) {
        System.out.println("\n\n!!! WARNING !!! - Unexpected: " + message + "\n\n");
        System.exit(1);
    }

    public static AstVisitor<Node, Object> sivtVisitor() {
        return new AstVisitor<Node, Object>()  {
            @Override
            protected Node visitQuerySpecification(QuerySpecification query_specification, Object context) {
                // If this is not a 'SELECT' query specificaiton, return.
                Optional<Relation> from = query_specification.getFrom();
                if (!from.isPresent()) return visitQueryBody(query_specification, context);

                System.out.println("Visiting select spec");

                System.out.println(columns_stack);

                ArrayList<Column> columns = new ArrayList<Column>();
                ArrayList<String> tables = new ArrayList<String>();
                columns_stack.push(columns);
                table_names_stack.push(tables);

                Node query_body = visitQueryBody(query_specification, context);

                System.out.println(columns_stack);

//                columns = columns_stack.pop();
//                for (Column column : columns) {
//                    System.out.println(column);
//                }

                return null;
            }

            @Override
            protected Node visitSelectItem(SelectItem selectItem, Object context) {
                System.out.println("visiting select item");
                if (!columns_stack.empty()) {
                    System.out.println("column");
                    ArrayList<Column> columns = columns_stack.pop();
                    columns.add(new Column(selectItem.toString()));
                    columns_stack.push(columns);
                }
                return visitNode(selectItem, context);
            }

            @Override
            protected Node visitNode(Node node, Object context) {
                return node;
            }

            @Override
            protected Node visitTable(Table node, Object context) {
                if (!table_names_stack.empty()) {
                    ArrayList<String> table_names = table_names_stack.pop();
                    table_names.add(node.toString());
                    table_names_stack.push(table_names);
                }
                return visitQueryBody(node, context);
            }

        };
    }



    public static AstVisitor<CreateView, Object> extractCreateViews() {
        return new AstVisitor<CreateView, Object>() {

            @Override
            protected CreateView visitCreateView(CreateView node, Object context) {
                System.out.println("\n\n");
                List<SelectItem> selectItems = new ArrayList<SelectItem>();

                // TODO: Think about whether we should consider isReplace (See CreateView.java class file)

                QualifiedName viewName = node.getName();
                List<Node> children = node.getChildren();
                if (children.size() > 0) {
                    // Get the first child (there should only be one here).
                    // There should ALWAYS be a query here, it is requiredNonNull
                    // in the constructor of a CreateView.
                    // Explicitly cast it to a query.
                    Iterator<Node> iterator = children.iterator();
                    Query childQuery = (Query)iterator.next();

                    // This should be a QuerySpecification.
                    QueryBody queryBody = childQuery.getQueryBody();

                    Optional<Relation> from;
                    if (queryBody.getClass() == QuerySpecification.class) {
                        from = ((QuerySpecification)queryBody).getFrom();
                        if (from.isPresent()) {
                            System.out.println("FROM: " + from);
                        } else {
                            unexpectedWarning("No FROM clause in query specification");
                        }
                    } else if (queryBody.getClass() == TableSubquery.class) {
                        // Handle
                    } else  {
                        unexpectedWarning("Query body was not a query specification (" + queryBody.getClass().getName() + ")");
                    }

                    List<? extends Node> queryBodyChildren = queryBody.getChildren();

                    for (Node child : queryBodyChildren) {
                        // Get the selected items.
                        if (child.getClass() == Select.class) {
                            Select select = (Select)child;
                            selectItems = select.getSelectItems();
                        }
                        //System.out.println("type: " + child.getClass().getName());
//                        System.out.println("val:  " + child);

                    }
                }

                System.out.println("VIEW NAME: " + viewName);
                System.out.println("SELECTED COLUMNS: " + selectItems);

                return node;
            }
        };
    }
}

