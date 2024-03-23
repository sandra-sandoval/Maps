package server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.sun.source.tree.AssertTree;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sources.CensusData;
import sources.CensusSource;
import sources.mocks.EchoMockCensusSource;
import spark.Spark;
import types.FeatureCollection;

public class TestFilteringHandler {
  private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
  // private JsonAdapter<FeatureCollection> adapter;
  private JsonAdapter<Map<String, Object>> adapter;

  @BeforeEach
  public void setup() {
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void tearDown() {
    Spark.unmap("/filter");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (Note: this would be
   *                better if it had more
   *                structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:3232/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestProperty("Content-Type", "application/json");
    clientConnection.setRequestProperty("Accept", "application/json");

    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testValidFilter() throws IOException {
    Spark.get("/filter", new FilteringHandler());
    Spark.awaitInitialization();

    String params = "keyword=layout";
    HttpURLConnection loadConnection = tryRequest("filter?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);

    assertEquals("success", body.get("result"));
    assertTrue(body.get("data").toString().contains(" layout of subdivisions adds charm and appeal"));
    assertTrue(body.get("data").toString()
        .contains(" It has a very attractive layout with the minimum traffic flow through the district."));

  }

  @Test
  public void testValidFilter2() throws IOException {
    Spark.get("/filter", new FilteringHandler());
    Spark.awaitInitialization();

    String params = "keyword=%205%20story";
    HttpURLConnection loadConnection = tryRequest("filter?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);

    assertEquals("success", body.get("result"));
    assertTrue(body.get("data").toString().contains(
        "Some 12-20 family 5 story walk up 25-30 years old 3-5 rooms renting $4-$5 per room. P.W.A. slum clearance project completed E. of Leonard Street."));
    assertTrue(body.get("data").toString().contains(
        "New 8th Avenue subway. Proposed highway to be built along Flushing Avenue at Naval Hostpital to connect downtown Brooklyn with Queens at Meeker Avenue Bridge over Newtown Creek"));
    assertTrue(body.get("data").toString().contains("Brooklyn"));
  }

  /**
   * Tests the case of an invalid request due to missing parameter resulting in an
   * error
   * 
   * @throws IOException
   */
  @Test
  public void testMissingParameter() throws IOException {
    Spark.get("/filter", new FilteringHandler());
    Spark.awaitInitialization();

    String params = "";
    HttpURLConnection loadConnection = tryRequest("filter?");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Missing required parameter: keyword", body.get("error_message"));
  }

  /**
   * Test that if there is no keyword, the entire data set is returned
   * 
   * @throws IOException
   */
  @Test
  public void testNoFiltering() throws IOException {
    Spark.get("/filter", new FilteringHandler());
    Spark.awaitInitialization();

    String params = "keyword=";
    HttpURLConnection loadConnection = tryRequest("filter?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("success", body.get("result"));
    assertEquals(17086554, body.get("data").toString().length());
  }

  /**
   * Tests the case in which an inputted keyword is not found in the data set
   * 
   * @throws IOException
   */
  @Test
  public void testNoResults() throws IOException {
    Spark.get("/filter", new FilteringHandler());
    Spark.awaitInitialization();

    String params = "keyword=noresults";
    HttpURLConnection loadConnection = tryRequest("filter?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("success", body.get("result"));
    assertEquals("{features=[], type=FeatureCollection}", body.get("data").toString());
  }
}
