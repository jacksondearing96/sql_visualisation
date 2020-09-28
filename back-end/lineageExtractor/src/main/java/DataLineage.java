import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Stores a list of nodes (tables or view) and writes out the information to
 * a JSON file.
 */
public class DataLineage {
    private ArrayList<LineageNode> nodeList = new ArrayList<LineageNode>();

    public List<LineageNode> getNodeList() {
        return nodeList;
    }

    public DataLineage() {}

    /**
     * Replace the source of a column at index with a list of other sources.
     * @param column The column to have its source replaced.
     * @param index The index of the source to replace.
     * @param sources The list of new sources to replace with.
     */
    private void replaceSourceWithSources(Column column, int index, ArrayList<String> sources) {
        column.getSources().remove(index);
        --index;
        for (String source : sources) {
            column.getSources().add(++index, source);
        }
    }

    /**
     * Removes all the anonymous tables in the nodeList, updating the sources accordingly.
     */
    public void bypassAnonymousTables() {

        // Populate a map containing mappings from IDs to their list of sources.
        HashMap<String, ArrayList<String>> idToSources = new HashMap<>();
        for (LineageNode node : nodeList) {
            for (Column column : node.getColumns()) {
                idToSources.put(column.getID(), column.getSources());
            }
        }

        // There may be multiple levels of anonymous tables. Therefore, repeat the process until no
        // more changes are made.
        boolean madeChange = true;

        while (madeChange) {
            madeChange = false;

            for (LineageNode node : nodeList) {
                for (Column column : node.getColumns()) {
                    for (int i = 0; i < column.getSources().size(); ++i) {
                        String source = column.getSources().get(i);

                        // If a source points to a column in an anonymous table, replace this source with the sources
                        // of the column in the anonymous table. This bypasses the anonymous table in the graph structure.
                        if (source.contains(Constants.Node.TYPE_ANON)) {
                            madeChange = true;
                            replaceSourceWithSources(column, i, idToSources.get(source));
                        }
                    }
                }
            }
        }

        // Delete the now redundant anonymous nodes.
        nodeList.removeIf(node -> node.getType().equals(Constants.Node.TYPE_ANON));
    }


    /**
     * Writes out the data lineage to a JSON file.
     */
    public String getNodeListAsJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(nodeList);
            return jsonStr;
        } catch (IOException e) {}
        return "";
    }

    /**
     * Rename columns in the existingNode based on staged renames in the newNode.
     * Some columns in the newNode may have a rename staged. This is when the rename must be applied.
     * @param existingColumns The existing columns in the LineageNode.
     * @param newColumns The new columns that may have staged renames.
     */
    private void applyRenamesToColumns(List<Column> existingColumns, List<Column> newColumns) {
        for (Column newColumn : newColumns) {
            for (Column existingColumn : existingColumns) {
                if (newColumn.getName().equals(existingColumn.getName())) {
                    newColumn.getRename().ifPresent(existingColumn::renameAndUpdateId);
                }
            }
        }
    }

    /**
     * Consolidate two nodes that represent the same node.
     * Adds the columns of the new node to the existing node. The addColumn method takes care
     * of ensuring these additions are all unique.
     * @param existingNode The existing node.
     * @param newNode The new node.
     */
    private void consolidateNodes(LineageNode existingNode, LineageNode newNode) {
        existingNode.addListOfColumns(newNode.getColumns());
        applyRenamesToColumns(existingNode.getColumns(), newNode.getColumns());
    }

    /**
     * Add a single node to the list of nodes.
     * @param newNode: node to add to the data lineage.
     */
    public void addNode(LineageNode newNode) {
        for (LineageNode existingNode : nodeList) {
            if (existingNode.getName().equals(newNode.getName())) {
                consolidateNodes(existingNode, newNode);
                newNode.getRename().ifPresent(existingNode::rename);
                return;
            }
        }
        newNode.getRename().ifPresent(newNode::rename);
        this.nodeList.add(newNode);
    }

    /**
     * Add a list of nodes.
     * @param nodes: list of nodes to add to the data lineage.
     */
    public void addListOfNodes(ArrayList<LineageNode> nodes) {
        for (LineageNode node : nodes) {
            addNode(node);
        }
    }

    public static String makeId(String source, String target) {
        return Util.removeDatabasePrefix(source).concat(Constants.Node.SEPARATOR).concat(target);
    }
}
