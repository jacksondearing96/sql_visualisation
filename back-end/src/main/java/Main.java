import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.IdentityHashMap;
import java.io.PrintStream;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Objects;

import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.tree.Statement;
import com.facebook.presto.sql.TreePrinter;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.QualifiedName;
import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.AstVisitor;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.Table;
import com.facebook.presto.sql.tree.CreateView;
import com.facebook.presto.sql.tree.Select;
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
        // ----- TESTING, REMOVE ME WHEN READY -----
        // Building a list of columns
        Column columnA = new Column("testa", "test1a", "test2a");
        Column columnB = new Column("testb", "test1b", "test2b");
        columnA.addSource("test::APPLE");
        columnA.addSource("test::WAMBLE");
        columnB.addSource("test::BANANA");
        columnB.addSource("test::WAMBLE2");

        // Constructing a node (which contains columns)
        LineageNode nodeA = new LineageNode("TABLE", "Woosh", "Woosh1");
        LineageNode nodeB = new LineageNode("VIEW", "Spooky", "Spooky2");
        nodeA.addColumn(columnA);
        nodeA.addColumn(columnB);

        // Store and write out the lineage
        DataLineage lineage = new DataLineage("this_is_just_a_test.json");
        lineage.addNode(nodeA);
        lineage.addNode(nodeB);
        lineage.addNode(nodeA);
        lineage.toJSON();

        // All the SQL scripts have been concatenated into the AllStatements.sql file.
        String sql = FileReader.ReadFile("resources/AllStatements.sql");
        List<StatementSplitter.Statement> statements = getStatements(sql);

        // Print out the SQL statements to check they were read and split correctly.
        for (StatementSplitter.Statement statement : statements) {
            System.out.println(statement.statement());
        }

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

        // Note: what we refer to as 'column' is equivalent to the API's 'select'.
        // Will stay be consistent with the 'select' notation here.
        AstVisitorExtractor<Table, ?> tableVisitor = AstVisitorExtractor.by(Extractors.extractTables());
        AstVisitorExtractor<Select, ?> selectVisitor = AstVisitorExtractor.by(Extractors.extractSelects());
        AstVisitorExtractor<CreateView, ?> createViewVisitor = AstVisitorExtractor.by(Extractors.extractCreateViews());

        // Iterate through each statement.
        // Use the sqlParser to parse the statement and then extract its table names.
        for (StatementSplitter.Statement splitterStatement : statements) {
            Statement statement = sqlParser.createStatement(splitterStatement.statement(), parsingOptionsBuilder.build());

            List<Table> tables = statement.accept(tableVisitor, null)
                    .collect(Collectors.toList());
            List<CreateView> createViews = statement.accept(createViewVisitor, null)
                    .collect(Collectors.toList());
            List<Select> selects = statement.accept(selectVisitor, null)
                    .collect(Collectors.toList());

            System.out.println(tables);
            System.out.println(createViews);
            System.out.println(selects);
            System.out.println("");
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
    public static AstVisitor<Table, Object> extractTables() {
        return new AstVisitor<Table, Object>() {
            @Override
            protected Table visitTable(Table node, Object context) {
                return node;
            }
        };
    }

    public static AstVisitor<CreateView, Object> extractCreateViews() {
        return new AstVisitor<CreateView, Object>() {
            @Override
            protected CreateView visitCreateView(CreateView node, Object context) {
                return node;
            }
        };
    }

    // note: 'selects' is the same as our terminology for 'columns'.
    public static AstVisitor<Select, Object> extractSelects() {
        return new AstVisitor<Select, Object>() {
            @Override
            protected Select visitSelect(Select node, Object context) {
                return node;
            }
        };
    }
}

