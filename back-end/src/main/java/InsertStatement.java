import java.util.*;
import java.util.stream.Collectors;

import com.facebook.presto.sql.tree.Insert;
import com.facebook.presto.sql.tree.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertStatement {

    final static Logger LOGGING = LoggerFactory.getLogger(InsertStatement.class);

    private LineageNode target;
    private Optional<LineageNode> source;

    private List<String> targetColumnNames;
    private List<String> sourceColumnNames;

    // Some INSERT statements define a subset of column names from the target which will be inserted into.
    // This flag is set 'true' when a set of predefined column names exists.
    private boolean hasPredefinedColumnNames;

    // Flag to represent whether there is a 1:1 mapping of columns between the source and target tables.
    // 'true' indicates the number of columns in source is equal to the number of columns in target.
    private boolean targetAndSourceAreColumnSymmetric;

    // TODO: Add this to the configuration file when it becomes available.
    private final String UNCERTAIN_SOURCE_COLUMN = "";

    InsertStatement(Insert insert, ArrayList<LineageNode> sources) {
        this.target = new LineageNode("TABLE", insert.getTarget().getSuffix());
        extractSourceForInsertStatement(sources);

        hasPredefinedColumnNames = insert.getColumns().isPresent();

        source.ifPresent(source -> sourceColumnNames = source.getColumnNames());
        if (hasPredefinedColumnNames) {
            targetColumnNames = insert.getColumns().get().stream().map(Identifier::getValue).collect(Collectors.toList());
        }

        targetAndSourceAreColumnSymmetric =
                source.isPresent()
                        && hasPredefinedColumnNames
                        && source.get().getColumns().size() == insert.getColumns().get().size();
    }

    /**
     * Get the source table that is used as content for INSERT statements.
     * @return The source table.
     */
    private void extractSourceForInsertStatement(ArrayList<LineageNode> sources) {
        switch (sources.size()) {
            case 1:
                // Standard case of a single source table.
                source = Optional.of(sources.get(0));
                break;
            case 0:
                // The valid case in which there is no generated source table.
                source = Optional.empty();
                break;
            default:
                LOGGING.warn("INSERT statement is deriving from a non-single source");
        }
    }

    /**
     * Sets the source for a column in the target table.
     *
     * The source will depend on whether there is a 1:1 mapping between the target and source tables. If a 1:1 mapping
     * exists, the source lineage can be confidently set. However, if our knowledge of the target table is incomplete
     * an we do not have a 1:1 mapping, there is no reasonable way to infer which lineage connections should be made.
     * In this case, simply leave the column field as uncertain.
     * @param column The column (of the target table)
     * @param columnIndex The index of the column in the target table.
     */
    private void addSourceToColumn(Column column, int columnIndex) {
        if (!source.isPresent()) return;
        String sourceColumnName = (targetAndSourceAreColumnSymmetric)
                ? sourceColumnNames.get(columnIndex) : UNCERTAIN_SOURCE_COLUMN;
        column.addSource(DataLineage.makeId(source.get().getName(), sourceColumnName));
    }

    /**
     * Get the resultant lineage nodes from the INSERT statement.
     * @return A list of the completed lineage nodes from the INSERT statement.
     */
    public ArrayList<LineageNode> getLineageNodes() {

        if (hasPredefinedColumnNames) {
            // Iterate through each column in the subset of predefined columns.
            for (int i = 0; i < targetColumnNames.size(); ++i) {
                Column column = new Column(targetColumnNames.get(i));
                addSourceToColumn(column, i);
                target.addColumn(column);
            }
        } else {
            // If there is no explicit subset of column names provided by the INSERT statement, simply copy over
            // all the columns we find in the source table.
            source.ifPresent(source -> Util.deriveEveryColumnFromSource(target, source));
        }

        ArrayList<LineageNode> nodes = new ArrayList<>(Collections.singletonList(target));
        source.ifPresent(nodes::add);
        return nodes;
    }
}
