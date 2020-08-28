import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.awt.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JsonWriter {
    private ArrayList<JsonNode> nodeList = new ArrayList<JsonNode>();
    private String fileName;

    public JsonWriter(String fileName) {
        this.fileName = fileName;
    }

    public void WriteToJson() {
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

    public void addNode(JsonNode node) {
        this.nodeList.add(node);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
