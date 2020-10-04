
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.HashSet;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gabriel a1673698
 */
public class SivtServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Initialise server
        Set<String> allowedHeaders = new HashSet<>();
        // Allowing cross-origin
        allowedHeaders.add("Access-Control-Allow-Origin");
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders));
        // Setting up /uploader route handler
        router.post("/uploader").handler(hndlr -> {
            hndlr.request().setExpectMultipart(true);
            hndlr.request().endHandler(endHndlr -> {
                MultiMap formAttr = hndlr.request().formAttributes();
                String sqlString = formAttr.get("sqlString");
                String lineage = LineageExtractor.extractLineageAsJson(sqlString);
                hndlr.response().end(lineage);
            });
        });
        
        // Creating server and listening
        HttpServer server = vertx.createHttpServer().requestHandler(router).listen(5000);
    }

}
