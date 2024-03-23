package server;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.JsonAdapter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;
import types.BoundingBoxKey;
import types.Feature;
import types.FeatureCollection;
import types.GeoJsonProperties;
import types.Geometry;

import java.nio.file.Files;

/**
 * This is the FilteringHandler class that handles a /filter request to our
 * server. This class implements the Route interface and contains a write-only
 * cache. It loads the geojson data and uses
 * the user's inputs of filter in based on sought-after keyword.
 *
 * Contains a Cache instance variable that maps a keyword to
 * the data that corresponds to such query, acting as a write-only history. Not
 * used to return
 * data to the frontend.
 */
public class FilteringHandler implements Route {

  private Map<String, FeatureCollection> history = new HashMap<>();

  /**
   * FilteringHandler class' constructor
   */
  public FilteringHandler() {
  }

  /**
   * Method that handles a /filter request to our Server. It takes in request
   * from the user
   * containing the sought-after keywords. Cache is write-only; not used to return
   * responses.
   * 
   * @param request  the Request object passed by client, should contain request
   *                 parameter keyword
   * @param response The response object providing functionality for modifying the
   *                 response
   * @return the serialized filtered data by the keyword inputted by the user or
   *         an error message
   */
  @Override
  public Object handle(Request request, Response response) {
    try {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FeatureCollection> jsonAdapter = moshi.adapter(FeatureCollection.class);
      String directory = System.getProperty("user.dir");
      String json = new String(Files.readAllBytes(Paths.get(directory +
          "/src/backend/src/main/java/data/fullDownload.json")));
      FeatureCollection originalData = jsonAdapter.fromJson(json);
      String searchKeyword = request.queryParams("keyword");
      if (searchKeyword == null) {
        return new FilteringFailureResponse(
            "error_bad_request", "Missing required parameter: keyword")
            .serialize();
      }
      FeatureCollection filteredData = filterDataByKeyword(searchKeyword, originalData);
      return new FilteringSuccessResponse(filteredData).serialize();
    } catch (IOException e) {
      return new FilteringFailureResponse("error_internal_server_error", e.getMessage()).serialize();
    }
  }

  /**
   * Filters a given FeatureCollection based on a specified keyword present in the
   * GeoJsonProperties' area descriptions.
   *
   * @param keyword      The keyword used for filtering the data.
   * @param originalData The original FeatureCollection to be filtered.
   * @return A new FeatureCollection containing only the features that match the
   *         provided keyword.
   */
  public FeatureCollection filterDataByKeyword(String keyword, FeatureCollection originalData) {
    if (keyword == null) {
      return originalData;
    }
    FeatureCollection filteredData = new FeatureCollection();
    filteredData.setType(originalData.getType());
    List<Feature> filteredFeatures = new ArrayList<>();

    for (Feature feature : originalData.getFeatures()) {
      GeoJsonProperties properties = feature.getProperties();
      if (properties != null) {
        Map<String, String> areaDescriptionSet = properties.getAreaDescriptionData();
        if (areaDescriptionSet != null) {
          for (String key : areaDescriptionSet.keySet()) {
            String description = areaDescriptionSet.get(key);
            if (description != null) {
              if (description.contains(keyword)) {
                filteredFeatures.add(feature);
                break; // No need to check further once a match is found
              }
            }
          }
        }
      }
    }

    filteredData.setFeatures(filteredFeatures);
    history.put(keyword, filteredData);
    return filteredData;
  }

  /**
   * Getter method returning the server's current filtering history
   *
   * @return a map where each key is a string representing the sought-after
   *         keyword and the corresponding value is a FeatureCollection associated
   *         with that keyword.
   */
  public Map<String, FeatureCollection> getHistory() {
    return this.history;
  }

  /**
   * A record representing a failed call to the /filtering handler, containing a
   * result with an
   * error code and an error message.
   *
   * @param result        the String containing an error code
   * @param error_message the String containing a more specific error message
   */
  public record FilteringFailureResponse(String result, String error_message) {
    /**
     * This method serializes a failure response object.
     *
     * @return this failure response object, serialized as JSON
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FilteringHandler.FilteringFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /redlining handler, containing
   * a result of
   * success, the date and time, and the serialized data information.
   *
   * @param result the String "success"
   * @param data   the String containing the serialized filtered data
   */
  public record FilteringSuccessResponse(String result, FeatureCollection data) {
    /**
     * The constructor for the RedliningSuccessResponse class.
     *
     * @param data the String containing the serialized diltered data
     */
    public FilteringSuccessResponse(FeatureCollection data) {
      this("success", data);
    }

    /**
     * This method serializes a success response object.
     *
     * @return this success response object, serialized as JSON
     */
    public String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FilteringHandler.FilteringSuccessResponse.class).toJson(this);
    }
  }
}
