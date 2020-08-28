import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.file.Paths;
import java.util.ArrayList;

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
     * Add a single node to the list of nodes.
     * @param node: node to add to the data lineage.
     */
    public void addNode(LineageNode node) {
        this.nodeList.add(node);
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
}
