
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.HashSet;
import java.util.Set;


public class SivtServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

       
        Set<String> allowedHeaders = new HashSet<>();
        
        allowedHeaders.add("Access-Control-Allow-Origin");
        
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders));
        
        router.post("/extract_lineage").handler(handler -> {
            handler.request().setExpectMultipart(true);
            handler.request().endHandler(endHandler -> {
                String sql = handler.request().formAttributes().get("sql");
                String lineage = LineageExtractor.extractLineageAsJson(sql);
                handler.response().end(lineage);
            });
        });
        
       
        HttpServer server = vertx.createHttpServer().requestHandler(router).listen(5000);
    }

}
