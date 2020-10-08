import com.facebook.presto.sql.parser.ParsingException;
import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.Statement;

import java.util.List;

public class VerifierSQL {

    /**
     * Verify SQL
     * Method attempts to run parser to check for syntax errors
     *
     * @param statements - List<StatementSplitter.Statement> with statements from sql file
     * @return boolean: true = SQL correct, false = SQL syntax error
     */
    public static boolean verifySQL(List<StatementSplitter.Statement> statements){

        // Iterate through each statement.
        // Try running parser - catch errors
        for (StatementSplitter.Statement statement : statements) {

            // Try to parse the statement
            // Return false on caught error
            try {
                SivtParser.parse(statement);
            } catch (ParsingException e){
                // There was an error
                System.err.println("Parsing Err: " + e.toString());
                return false;
            }
        }

        // There wasn't an error
        return true;
    }

}
