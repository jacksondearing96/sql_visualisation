import com.facebook.presto.sql.TreePrinter;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.QualifiedName;
import com.facebook.presto.sql.tree.Statement;

import java.io.PrintStream;
import java.util.*;

import static com.facebook.presto.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DECIMAL;

/**
 * Sivt wrapper for the parsing functionality provided by Facebook's presto-parser.
 * Simplifies the API for parsing and printing SQL/ASTs.
 */
public class SivtParser {

    private static final SqlParser sqlParser = new SqlParser();
    private static final ParsingOptions parsingOptions = sivtParsingOptions();

    /**
     * The specific delimiter used by convention in Propic SQL scripts.
     */
    private static final String sqlStatementDelimiter = "###";

    /**
     * Parse an SQL statement.
     * @param statement The statement to be parsed. This statement must be one of the statements
     *                  returned by the StatementSplitter.
     * @return The parsed statement.
     */
    public static Statement parse(StatementSplitter.Statement statement) {
        return sqlParser.createStatement(statement.statement(), parsingOptions);
    }

    /**
     * Get the individual SQL statements from a string of concatenated SQL statements.
     * @param sql The concatenated SQL statements.
     * @return List of the individual statements.
     */
    public static List<StatementSplitter.Statement> getStatements(String sql) {
        Set<String> delimiters = new HashSet<String>();
        delimiters.add(sqlStatementDelimiter);
        StatementSplitter statementSplitter = new StatementSplitter(sql, delimiters);
        List<StatementSplitter.Statement> statements = statementSplitter.getCompleteStatements();
        return statements;
    }

    /**
     * Print to STDOUT the abstract syntax tree of the first statement in a list of statements.
     * @param statements The list of statements that have come from the StatementSplitter.
     */
    public static void printAstOfFirstStatement(List<StatementSplitter.Statement> statements) {
        Iterator<StatementSplitter.Statement> iterator = statements.iterator();
        StatementSplitter.Statement firstStatement = iterator.next();
        Statement statement = sqlParser.createStatement(firstStatement.statement(), parsingOptions);
        IdentityHashMap<Expression, QualifiedName> resolvedNameReferences = new IdentityHashMap<Expression, QualifiedName>();
        PrintStream printStream = new PrintStream(System.out);
        TreePrinter treePrinter = new TreePrinter(resolvedNameReferences, printStream);
        treePrinter.print(statement);
    }

    /**
     * Get the default parsing options.
     * @return The default parsing options fit for the parsing Propic SQL scripts.
     */
    private static ParsingOptions sivtParsingOptions() {
        ParsingOptions.Builder parsingOptionsBuilder = ParsingOptions.builder();
        parsingOptionsBuilder.setDecimalLiteralTreatment(AS_DECIMAL);
        return parsingOptionsBuilder.build();
    }
}
