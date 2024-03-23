package server;

import static spark.Spark.after;

import com.google.common.cache.CacheBuilder;
import sources.AcsCensusSource;
import sources.mocks.StaleMockCensusSource;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import spark.Spark;

/**
 * The top-level class for the Maps project. Contains the main() method which
 * starts Spark and
 * runs the various handlers for our endpoints: /loadcsv, /viewcsv, /searchcsv,
 * /broadband, /mockbroadband,
 * /redlining, and /filter
 *
 * Also allows a developer using these endpoints to create their own
 * CacheBuilder, or pass a null
 * CacheBuilder to the constructor, to specify how they want responses from the
 * source to be cached,
 * or for responses not to be cached at all (the null case).
 */
public class Server {

  static final int port = 3232;

  /**
   * The constructor for the Server class containing all the handlers : load, view, search,
   * redlining, broadband, and mockBroadband.
   *
   * @param source       the CensusSource object representing the source to get
   *                     broadband data from
   * @param cacheBuilder the CacheBuilder object representing null, to specify no
   *                     cache, or a
   *                     CacheBuilder object specifying how requests should be
   *                     cached
   */

  public Server() {
    CsvDataWrapper csvData = new CsvDataWrapper(new ArrayList<>(), false);
    Spark.port(port);
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    // Setting up the handler for the GET /order and /mock endpoints
    Spark.get("loadcsv", new LoadCsvHandler(csvData));
    Spark.get("viewcsv", new ViewCsvHandler(csvData));
    Spark.get("searchcsv", new SearchCsvHandler(csvData));
    Spark.get(
        "broadband",
        new BroadbandHandler(
            new AcsCensusSource(),
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES)));
    Spark.get(
        "mockbroadband",
        new BroadbandHandler(
            new StaleMockCensusSource(),
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES)));
    Spark.get("redlining",
        new RedliningHandler(CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES)));
    Spark.get("filter",
        new FilteringHandler());
    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * The main method of the Server class which starts the server and then exits.
   *
   * @param args the command line arguments, which are not accessed
   */
  public static void main(String[] args) {
    new Server();
    System.out.println("Server started at http://localhost:" + port);
  }
}
