import static spark.Spark.*;

public class Server {
    public static void main(String[] args) {
        staticFiles.location("public");
        get("/", (request, response) -> "index.html");

        // post("/upload_files", (request, response) -> {
        //     System.out.println(request.body());
        //     return "post method returning";
        // });
    }
}