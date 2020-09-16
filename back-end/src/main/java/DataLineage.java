import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores a list of nodes (tables or view) and writes out the information to
 * a JSON file.
 */
public class DataLineage {
    private ArrayList<LineageNode> nodeList = new ArrayList<LineageNode>();
    private String fileName;

    /**
     * @param fileName name of the JSON file.
     */
    public DataLineage(String fileName) {
        this.fileName = fileName;
    }

    public List<LineageNode> getNodeList() {
        return nodeList;
    }

    /**
     * Writes out the data lineage to a JSON file.
     */
    public void toJSON() {
        try {
            // Create object mapper instance
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            // Convert Nodes to JSON file
            writer.writeValue(Paths.get(this.fileName).toFile(), this.nodeList);
        } catch (Exception ex) {
            ex.printStackTrace();
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
    }

    /**
     * Add a single node to the list of nodes.
     * @param newNode: node to add to the data lineage.
     */
    public void addNode(LineageNode newNode) {
        for (LineageNode existingNode : nodeList) {
            if (existingNode.getName().equals(newNode.getName())) {
                consolidateNodes(existingNode, newNode);
                return;
            }
        }
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

    /**
     * @param fileName name of the JSON file.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static String makeId(String source, String target) {
        return source.concat("::").concat(target);
    }
}
