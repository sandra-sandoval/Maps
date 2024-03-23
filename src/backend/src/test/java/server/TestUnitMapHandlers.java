package server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import types.BoundingBoxKey;
import types.Feature;
import types.FeatureCollection;
import types.GeoJsonProperties;
import types.Geometry;

public class TestUnitMapHandlers {
  private static FeatureCollection fullData;
  private static FeatureCollection mockData;
  private static RedliningHandler redliningHandler;
  private static FilteringHandler filteringHandler;

  @BeforeAll
  static void setupOnce() throws IOException {
    String fullJson = new String(Files.readAllBytes(Paths.get(
        "/Users/zdzilowska/Desktop/university/year 2/cs0320/maps-jzdzilow-spsandov/src/backend/src/main/java/data/fullDownload.json")));
    String mockJson = new String(Files.readAllBytes(Paths.get(
        "/Users/zdzilowska/Desktop/university/year 2/cs0320/maps-jzdzilow-spsandov/src/backend/src/main/java/data/mockGeoJson.json")));
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<FeatureCollection> jsonAdapter = moshi.adapter(FeatureCollection.class);

    // Serialize the GEOJson into FeatureCollection
    fullData = jsonAdapter.fromJson(fullJson);
    mockData = jsonAdapter.fromJson(mockJson);
    redliningHandler = new RedliningHandler(
        CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    filteringHandler = new FilteringHandler();
  }

  @Test
  public void testNoFiltering() {
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(-90, 90, -180, 180, mockData);
    // Expect the result to be the same as the original data since no filtering is
    // applied
    assertEquals(mockData.getFeatures().size(), result.getFeatures().size());
  }

  @Test
  public void testFilteringWithinBoundingBox() {
    double minLon = -83;
    double maxLon = -82;
    double minLat = 35;
    double maxLat = 36;

    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);
    assertEquals(1, result.getFeatures().size()); // Expect the result to contain only features within the specified
                                                  // bounding box
    for (Feature feature : result.getFeatures()) {
      Geometry geometry = feature.getGeometry();
      assertNotNull(geometry);

      for (List<List<List<Double>>> multipolygon : geometry.getCoordinates()) {
        for (List<List<Double>> polygon : multipolygon) {
          for (List<Double> point : polygon) {
            double featureLat = point.get(1);
            double featureLon = point.get(0);

            assertTrue(featureLat >= minLat && featureLat <= maxLat);
            assertTrue(featureLon >= minLon && featureLon <= maxLon);
          }
        }
      }
    }
  }

  @Test
  public void testFilteringOutsideBoundingBox() {
    // bounding box w features outside
    double minLat = 50;
    double maxLat = 60;
    double minLon = -90;
    double maxLon = -80;

    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);
    // Expect the result to be an empty FeatureCollection
    assertTrue(result.getFeatures().isEmpty());
  }

  @Test
  public void testPartialLatitudeFilteringEmptyResult() {
    double minLat = 33.49;
    double maxLat = 33.5;
    double minLon = -180;
    double maxLon = 180;
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);

    assertNotNull(result);
    assertEquals(0, result.getFeatures().size()); // Expect the result to contain only features within the specified

  }

  @Test
  public void testPartialLongtitudeFilteringEmptyResult() {
    double minLat = -90;
    double maxLat = 90;
    double minLon = 120;
    double maxLon = 121;
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);

    assertNotNull(result);
    assertEquals(0, result.getFeatures().size()); // Expect the result to contain only features within the specified
  }

  @Test
  public void testPartialLatitudeFilteringNonEmptyResult() {
    double minLat = 35.58;
    double maxLat = 35.64;
    double minLon = -180;
    double maxLon = 180;
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);

    assertNotNull(result);
    assertEquals(1, result.getFeatures().size()); // Expect the result to contain only features within the specified
    assertEquals("Asheville", result.getFeatures().get(0).getProperties().getCity());
  }

  @Test
  public void testPartialLongtitudeFilteringNonEmptyResult() {
    double minLat = -90;
    double maxLat = 90;
    double minLon = -86.762269;
    double maxLon = -86.745796;
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);

    assertNotNull(result);
    assertEquals(1, result.getFeatures().size()); // Expect the result to contain only features within the specified
    assertEquals("Birmingham", result.getFeatures().get(0).getProperties().getCity());
  }

  @Test
  public void testExactDataValuesAsInputted() {
    double minLat = -90;
    double maxLat = 90;
    double minLon = -86.762268;
    double maxLon = -86.745797;
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);

    assertNotNull(result);
    assertEquals(1, result.getFeatures().size()); // Expect the result to contain only features within the specified
    assertEquals("Birmingham", result.getFeatures().get(0).getProperties().getCity());
  }

  @Test
  public void testNoMaxLatNoMinLon() {
    double minLat = -90;
    double maxLat = 90;
    double minLon = -180;
    double maxLon = -86.745797;
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);

    assertNotNull(result);
    assertEquals(2, result.getFeatures().size()); // Expect the result to contain only features within the specified
    assertEquals("Birmingham", result.getFeatures().get(0).getProperties().getCity());
  }

  /**
   * Test with random bound inputs into our redliningHandler. Ensures that it
   * handles different valid
   * number and doesn't throw an exception
   */
  @Test
  public void testFuzzingBoundsNoException() {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder();

    Random random = new Random();
    for (int i = 0; i < 50; i++) {
      int minLat = random.nextInt((180) + 1) - 90;
      int maxLat = random.nextInt((180) + 1) - 90;
      int minLon = random.nextInt((360) + 1) - 180;
      int maxLon = random.nextInt((360) + 1) - 180;

      assertDoesNotThrow(() -> redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, fullData));
    }
  }

  /**
   * Test with random bound inputs into our redliningHandler. Ensures that it
   * handles different valid
   * number and returns expected features
   */
  @Test
  public void testFuzzingBoundsProperBounds() {
    Random random = new Random();
    double minLon = random.nextDouble() * (-87 - (-180)) + (-180);
    double maxLon = random.nextDouble() * (90 - (-86.72)) + (-86.72);
    double minLat = random.nextDouble() * (33 - (-90)) + (-90);
    double maxLat = random.nextDouble() * (90 - 35) + 35;

    assertDoesNotThrow(() -> redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, fullData));
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);
    assertEquals("Birmingham", result.getFeatures().get(0).getProperties().getCity());
  }

  /**
   * Test with random bound inputs into our redliningHandler.
   * Ensures that it returns an empty dataset
   */
  @Test
  public void testFuzzingBoundsImproperBounds() {
    Random random = new Random();

    // Generate valid parameters
    int minLat = random.nextInt((180) + 1) - 90;
    int maxLat = random.nextInt((180) + 1) - 90;
    int minLon = random.nextInt((360) + 1) - 180;
    int maxLon = random.nextInt((360) + 1) - 180;

    assertDoesNotThrow(() -> redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, fullData));
    FeatureCollection result = redliningHandler.filterDataByBoundingBox(minLat, maxLat, minLon, maxLon, mockData);
    assertEquals(0, result.getFeatures().size());
  }

  @Test
  void testFilterDataByKeywordSpecified() {
    String keyword = "Bells";
    FeatureCollection filteredData = filteringHandler.filterDataByKeyword(keyword, fullData);
    assertNotNull(filteredData);
    List<Feature> filteredFeatures = filteredData.getFeatures();
    assertNotNull(filteredFeatures);

    for (Feature feature : filteredFeatures) {
      GeoJsonProperties properties = feature.getProperties();
      assertNotNull(properties);
      Map<String, String> areaDescriptionSet = properties.getAreaDescriptionData();
      assertNotNull(areaDescriptionSet);

      boolean containsKeyword = areaDescriptionSet.values().stream()
          .anyMatch(description -> description.contains(keyword));
      assertTrue(containsKeyword);
    }
  }

  @Test
  void testFilterDataByKeywordRandom() {
    String[] providedKeywords = {
        "Bells", "apartments", "Oakland", "Italians", "Hungarian", "relief", "Laborers", "river",
        "uniformity", "style", "northern", "portion", "land", "swamp", "standpoint",
        "residents", "Fairfield", "Southwest%20side%20of%20area%20adjoins%20vacant"
    };
    int randomIndex = (int) (Math.random() * providedKeywords.length);
    String keyword = providedKeywords[randomIndex];

    FeatureCollection filteredData = filteringHandler
        .filterDataByKeyword("Southwest%20side%20of%20area%20adjoins%20vacant", fullData);
    assertNotNull(filteredData);
    List<Feature> filteredFeatures = filteredData.getFeatures();
    assertNotNull(filteredFeatures);

    for (Feature feature : filteredFeatures) {
      GeoJsonProperties properties = feature.getProperties();
      assertNotNull(properties);
      Map<String, String> areaDescriptionSet = properties.getAreaDescriptionData();
      assertNotNull(areaDescriptionSet);
      boolean containsKeyword = areaDescriptionSet.values().stream()
          .anyMatch(description -> description.contains(keyword));
      assertTrue(containsKeyword);
    }
  }

  @Test
  void testFilterDataByKeywordAddsToHistory() {
    String keyword = "Bells";
    FeatureCollection filteredData = filteringHandler.filterDataByKeyword(keyword, fullData);
    assertNotNull(filteredData);

    assertTrue(filteringHandler.getHistory().containsKey(keyword));
    assertEquals(filteredData, filteringHandler.getHistory().get(keyword));
  }

  @Test
  void testFilterDataByKeywordWithNullKeywordDoesNotAddToHistory() {
    // null keyword - not added to history
    FeatureCollection filteredData = filteringHandler.filterDataByKeyword(null, mockData);
    assertNotNull(filteredData);
    assertTrue(filteringHandler.getHistory().isEmpty());

    FeatureCollection filteredData2 = filteringHandler.filterDataByKeyword("swamp", fullData);
    assertTrue(filteringHandler.getHistory().containsKey("swamp"));
    assertEquals(filteredData2, filteringHandler.getHistory().get("swamp"));

    // search for the same keyword
    FeatureCollection filteredData3 = filteringHandler.filterDataByKeyword("swamp", fullData);
    assertTrue(filteringHandler.getHistory().containsKey("swamp"));
    assertEquals(filteredData3.getFeatures().get(0).getProperties().getCity(),
        filteringHandler.getHistory().get("swamp").getFeatures().get(0).getProperties().getCity());

    // keyword that couldnt be found, but empty featurecollection still added to
    // history
    filteringHandler.filterDataByKeyword("thisisatestbecauseutsjdjdjd", fullData);
    assertTrue(filteringHandler.getHistory().containsKey("thisisatestbecauseutsjdjdjd"));
    assertEquals(2, filteringHandler.getHistory().size());
  }
}
