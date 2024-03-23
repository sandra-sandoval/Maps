package server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import types.BoundingBoxKey;
import types.FeatureCollection;

public class TestRedliningHandler {

  private LoadingCache<BoundingBoxKey, Object> cache;
  private final Type mapStringObject = Types.newParameterizedType(Map.class, String.class,
      Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  @BeforeEach
  public void setup() {
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  @AfterEach
  public void tearDown() {
    Spark.unmap("/redlining");
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

  /**
   * Tests the case in which the user inputs 0 as the bounds for the latitude and
   * longitude
   * and the entire dataset is returned due to not filtering being needed
   * 
   * @throws IOException
   */
  @Test
  public void testNoFilteringOnRedliningData() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();

    String params = "minLat=-90&maxLat=90&minLon=-180&maxLon=180";
    HttpURLConnection loadConnection = tryRequest("redlining?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(
        new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    // assertEquals(dateTimeFormatted, body.get("date_time"));
    assertEquals("success", body.get("result"));
    assertNotNull(body.get("data"));
    assertEquals(17086554, body.get("data").toString().length());
    loadConnection.disconnect();
  }

  /**
   * Tests the case in which the user inputs invalid number of parameters which
   * results in an
   * error message or null parameters
   * 
   * @throws IOException
   */
  @Test
  public void testInvalidNumberofParameters() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();

    String params = "minLat=0"; // missing parameters
    HttpURLConnection loadConnection = tryRequest("redlining?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(
        new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Null parameters - invalid input", body.get("error_message"));
    loadConnection.disconnect();
  }

  /**
   * Tests the case in which the user inputs a value that is ill formatted
   * numerically
   * 
   * @throws IOException
   */
  @Test
  public void testInvalidRequestFormat() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();

    String params = "minLat=-2..3&maxLat=-100000000&minLon=10000000&maxLon=-1000000000"; //
    HttpURLConnection loadConnection = tryRequest("redlining?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(
        new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("error_bad_request", body.get("result"));
    // System.out.println(body.get("error_message"));
    assertEquals("Invalid parameter format", body.get("error_message"));
    loadConnection.disconnect();
  }

  /**
   * Tests for vali request and checks that the correct information is returned
   * within the bounds
   * provided
   * 
   * @throws IOException
   */
  @Test
  public void testValidRequest() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();
    String params = "minLat=33.470&maxLat=33.51&minLon=-86.774&maxLon=-86.723"; //
    HttpURLConnection loadConnection = tryRequest("redlining?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(
        new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("success", body.get("result"));
    assertTrue(body.get("data").toString().contains("state=AL"));
    assertTrue(body.get("data").toString().contains("name=White dairy section (outside city limits)"));
    assertTrue(body.get("data").toString().contains("name=Colonial Hills"));
    assertTrue(
        body.get("data").toString().contains("Mountain Brook Estates and Country Club Gardens (outside city limits)"));

    // System.out.println(body.get("data").toString());
  }

  /**
   * Tests that when there is no data within the inputted bounds, an empty
   * features list will be
   * returned
   * 
   * @throws IOException
   */
  @Test
  public void testNoResults() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();
    String params = "minLat=1&maxLat=1&minLon=1&maxLon=1"; //
    HttpURLConnection loadConnection = tryRequest("redlining?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(
        new Buffer().readFrom(loadConnection.getInputStream()));
    assertNotNull(body);
    assertEquals("success", body.get("result"));
    assertTrue(body.get("data").toString().contains("features=[]"));
    // System.out.println(body.get("data"));
  }

  /**
   * Test with random bound inputs into our redliningHandler. Ensures that it
   * handles different valid
   * number and results in a success. Checks for validity.
   * 
   * @throws IOException
   */
  @Test
  public void testFuzzingBounds() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();

    Random random = new Random();
    for (int i = 0; i < 50; i++) {
      int minLat = random.nextInt((180) + 1) - 90;
      int maxLat = random.nextInt((180) + 1) - 90;
      int minLon = random.nextInt((360) + 1) - 180;
      int maxLon = random.nextInt((360) + 1) - 180;

      String params = "minLat=" + String.valueOf(minLat) + "&maxLat=" + String.valueOf(maxLat) + "&minLon="
          + String.valueOf(minLon) + "&maxLon=" + maxLon; // missing parameters
      HttpURLConnection loadConnection = tryRequest("redlining?" + params);
      assertEquals(200, loadConnection.getResponseCode());
      Map<String, Object> body = adapter.fromJson(
          new Buffer().readFrom(loadConnection.getInputStream()));

      Assertions.assertNotNull(body);
      assertTrue(body.containsKey("result"));
      assertEquals("success", body.get("result"));
      assertTrue(body.containsKey("data"));
      assertTrue(body.containsKey("date_time"));
    }
  }

  /**
   * Tests with randomly generated ill-formatted parameters to ensure that the
   * redliningHandler
   * properly handles this error with an error message
   * 
   * @throws IOException
   */
  @Test
  public void testInvalidParameterFuzzing() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    Spark.get("/redlining", new RedliningHandler(cacheBuilder));
    Spark.awaitInitialization();

    for (int i = 0; i < 50; i++) {
      String params = generateIllFormattedParameters();
      HttpURLConnection loadConnection = tryRequest("redlining?" + params);
      assertEquals(200, loadConnection.getResponseCode());
      Map<String, Object> body = adapter.fromJson(
          new Buffer().readFrom(loadConnection.getInputStream()));

      Assertions.assertNotNull(body);
      assertTrue(body.containsKey("result"));
      assertEquals("error_bad_request", body.get("result"));
      assertEquals("Null parameters - invalid input", body.get("error_message"));
    }
  }

  /**
   * Helper function that generates ill-formatted parameters for the redlining
   * request such as
   * missing parameters
   * 
   * @return ill-formatted parameters that should result in an error
   */
  private String generateIllFormattedParameters() {
    Random random = new Random();

    // Generate valid parameters
    int minLat = random.nextInt((180) + 1) - 90;
    int maxLat = random.nextInt((180) + 1) - 90;
    int minLon = random.nextInt((360) + 1) - 180;
    int maxLon = random.nextInt((360) + 1) - 180;

    String[] parameterNames = { "minLat", "maxLat", "minLon", "maxLon" };

    String removeParameter = parameterNames[random.nextInt(parameterNames.length)];

    String params = "";
    for (String paramName : parameterNames) {
      if (!paramName.equals(removeParameter)) {
        int paramValue;
        switch (paramName) {
          case "minLat":
            paramValue = minLat;
            break;
          case "maxLat":
            paramValue = maxLat;
            break;
          case "minLon":
            paramValue = minLon;
            break;
          case "maxLon":
            paramValue = maxLon;
            break;
          default:
            paramValue = 0;
        }

        params += paramName + "=" + paramValue + "&";
      }
    }
    return params;
  }
}
