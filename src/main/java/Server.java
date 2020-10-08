import static spark.Spark.*;

public class Server {
    public static void main(String[] args) {
        staticFiles.location("public");
        get("/", (request, response) -> "index.html");
        post("/lineage_extractor", (request, response) ->  LineageExtractor.extractLineageAsJson(request.body()));
        post("/verify_sql", (request, response) -> VerifierSQL.verifySQL(request.body()));
    }
}