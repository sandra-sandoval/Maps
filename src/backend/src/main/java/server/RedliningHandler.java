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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
 * This is the RedliningHandler class that handles a /redlining request to our
 * server. This class
 * implements the Route interface and contains a cache. This class loads the
 * geojson data and uses
 * the user's inputs of lower and upper bounds for the longitude and latitude to
 * filter the data
 * that is going to be returned.
 *
 * Contains a Cache instance variable that maps a bounding key to
 * the data that corresponds to those bounds. Used to reduce the repitions of
 * filtering on same
 * bounds.
 */
public class RedliningHandler implements Route {

  private LoadingCache<BoundingBoxKey, Object> cache;

  /**
   * This is the RedliningHandler class' constructor that takes in a CacheBuilder
   * and instantiates
   * a new cacheLoader.
   * 
   * @param cacheBuilder parameter for the handler
   */
  public RedliningHandler(CacheBuilder cacheBuilder) {
    this.cache = cacheBuilder.build(
        new CacheLoader<BoundingBoxKey, Object>() {
          @Override
          public Object load(BoundingBoxKey key) throws Exception {
            return handleCacheMiss(key);
          }
        });
  }

  /**
   * Method that handles a /redlining request to our Server. It takes in request
   * from the user
   * containing bounds for filtering including lower and upper bounds for latitude
   * and longitude.
   * Used to create a boundingBoxKey which is used to check if the cache contains
   * the value. In the
   * case that the cache contains the value, the response from the cache
   * corresponding to the bounding
   * keys will be returned, otherwise the helper method to filter the data based
   * on these bounds is
   * called.
   * 
   * @param request  the Request object passed by client, should contain request
   *                 parameters min Lat, maxLat, minLon, and maxLon
   * @param response The response object providing functionality for modifying the
   *                 response
   * @return The cached response if available; otherwise, the result of
   *         handleCacheMiss method.
   */
  @Override
  public Object handle(Request request, Response response) {
    try {
      double minLat = Double.parseDouble(request.queryParams("minLat"));
      double maxLat = Double.parseDouble(request.queryParams("maxLat"));
      double minLon = Double.parseDouble(request.queryParams("minLon"));
      double maxLon = Double.parseDouble(request.queryParams("maxLon"));
      BoundingBoxKey key = new BoundingBoxKey(minLat, maxLat, minLon, maxLon);

      // try get the cached response
      return cache.get(key);
    } catch (NumberFormatException e) {
      return new RedliningFailureResponse("error_bad_request", "Invalid parameter format").serialize();
    } catch (NullPointerException e) {
      return new RedliningFailureResponse("error_bad_request", "Null parameters - invalid input").serialize();
    } catch (Exception e) {
      return new RedliningFailureResponse("error_bad_request", e.getMessage()).serialize();
    }
  }

  /**
   * Method that takes in the bounds for filtering and the bouningBoxKey
   * initialized in the handle
   * method. It is called when the data corresponding to those bounds are not
   * already in the cache.
   * It serializes the entire geojson data to then call the filter helper method
   * on it and
   * find the data that correspond to those bounds. The filtered data is then
   * serialized and added
   * to the cache to ensure that the user is able to access this information again
   * and reduce the
   * need for filtering on bounds already done.
   * 
   * @param key the bounding box key used to add the the newly filtered data to
   *            the cache with the
   *            key.
   * @return the serialized filtered data by the bounds inputted by the user or an
   *         error message
   */
  private Object handleCacheMiss(BoundingBoxKey key) {
    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    try {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FeatureCollection> jsonAdapter = moshi.adapter(FeatureCollection.class);
      String directory = System.getProperty("user.dir");
      String json = new String(Files.readAllBytes(Paths.get(directory +
          "/src/backend/src/main/java/data/fullDownload.json")));
      FeatureCollection originalData = jsonAdapter.fromJson(json);
      FeatureCollection filteredData = filterDataByBoundingBox(key.getMinLat(), key.getMaxLat(),
          key.getMinLon(), key.getMaxLon(), originalData);
      return new RedliningSuccessResponse(dateTimeFormatted, filteredData).serialize();
    } catch (IOException e) {
      return new RedliningFailureResponse("error_internal_server_error", e.getMessage()).serialize();
    }
  }

  /**
   * Method that takes in the dataset that is to be filtered by the lower and
   * upper bounds passed
   * in. If the bounds are 0, there will be no filtering and the entire data set
   * will be returned.
   * 
   * @param minLat       lower latitude bound used to filter the data set
   * @param maxLat       upper latitude bound used to filter the data set
   * @param minLon       lower longitude bound used to filter the data set
   * @param maxLon       upper longitude bound used to filter the data set
   * @param originalData dataset to be filtered by the inputted bounds
   * @return the data corresponding to the bounds
   */
  public FeatureCollection filterDataByBoundingBox(double minLat, double maxLat, double minLon, double maxLon,
      FeatureCollection originalData) {
    if (minLat == -90 && maxLat == 90 && minLon == -180 && maxLon == 180) {
      // No filtering, return the original data as is
      return originalData;
    }

    FeatureCollection filteredData = new FeatureCollection();
    filteredData.setType(originalData.getType());
    List<Feature> filteredFeatures = new ArrayList<>();

    for (Feature feature : originalData.getFeatures()) {
      Geometry geometry = feature.getGeometry();
      if (geometry == null) {
        continue; // Skip features with null geometry
      }
      List<List<List<List<Double>>>> multipolygons = geometry.getCoordinates();
      boolean withinBoundingBox = isWithinBox(multipolygons, minLat, maxLat, minLon, maxLon);
      if (withinBoundingBox) {
        filteredFeatures.add(feature);
      }
    }
    filteredData.setFeatures(filteredFeatures);
    return filteredData;
  }

  /**
   * Checks if any point within a set of coordinates
   * is within a specified bounding box. Iterates through the nested lists of
   * coordinates and compares each
   * point's latitude and longitude with the given bounding box limits. If any
   * point is found outside the
   * specified bounding box, returns false.
   *
   * @param coords The coordinates representing a multipolygon.
   * @param minLat The minimum latitude of the bounding box.
   * @param maxLat The maximum latitude of the bounding box.
   * @param minLon The minimum longitude of the bounding box.
   * @param maxLon The maximum longitude of the bounding box.
   * @return True if all points within the coordinates are within the specified
   *         bounding box, false otherwise.
   */
  public boolean isWithinBox(List<List<List<List<Double>>>> coords, double minLat, double maxLat, double minLon,
      double maxLon) {
    for (List<List<List<Double>>> multipolygon : coords) {
      for (List<List<Double>> polygon : multipolygon) {
        for (List<Double> point : polygon) {
          double featureLat = point.get(1); // Latitude at index 1
          double featureLon = point.get(0);

          if (featureLat < minLat || featureLat > maxLat ||
              featureLon < minLon || featureLon > maxLon) {
            return false; // No need to check further once a point is inside the bounding box
          }
        }
      }
    }
    return true;
  }

  /**
   * A record representing a failed call to the /redlining handler, containing a
   * result with an
   * error code and an error message.
   *
   * @param result        the String containing an error code
   * @param error_message the String containing a more specific error message
   */
  public record RedliningFailureResponse(String result, String error_message) {
    /**
     * This method serializes a failure response object.
     *
     * @return this failure response object, serialized as JSON
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(RedliningHandler.RedliningFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /redlining handler, containing
   * a result of
   * success, the date and time, and the serialized data information.
   *
   * @param result    the String "success"
   * @param date_time the String date and time of the query to get the
   *                  redlining data
   * @param data      the String containing the serialized redlining data
   */
  public record RedliningSuccessResponse(String result, String date_time, FeatureCollection data) {
    /**
     * The constructor for the RedliningSuccessResponse class.
     *
     * @param date_time the String representing the date and time that the
     *                  redlining data returned was accessed
     * @param data      the String containing the serialized redlining data
     */
    public RedliningSuccessResponse(String date_time, FeatureCollection data) {
      this("success", date_time, data);
    }

    /**
     * This method serializes a success response object.
     *
     * @return this success response object, serialized as JSON
     */
    public String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(RedliningHandler.RedliningSuccessResponse.class).toJson(this);
    }
  }
}
