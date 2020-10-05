
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
        
        router.post("/uploader").handler(hndlr -> {
            hndlr.request().setExpectMultipart(true);
            hndlr.request().endHandler(endHndlr -> {
                MultiMap formAttr = hndlr.request().formAttributes();
                String sqlString = formAttr.get("sqlString");
                String lineage = LineageExtractor.extractLineageAsJson(sqlString);
                hndlr.response().end(lineage);
            });
        });
        
       
        HttpServer server = vertx.createHttpServer().requestHandler(router).listen(5000);
    }

}
