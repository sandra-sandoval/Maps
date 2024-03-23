package types;

/**
 * Feature class that contains instances of the Geometry and GeoJsonProperties classes. Contains
 * various setter and getter methods for these values.
 */
public class Feature {
  private String type;
  private Geometry geometry;
  private GeoJsonProperties properties;

  /**
   * Method that retrieves the type of the feature
   * @return type value
   */
  // Getters and setters
  public String getType() {
      return type;
  }

  /**
   * Method that sets the value of the type of the feature to a new type (passed in)
   * @param type value that will be the new value of the type
   */
  public void setType(String type) {
      this.type = type;
  }

  /**
   * Retrieval method for the coordinates of the feature
   * @return geometry class instance
   */
  public Geometry getGeometry() {
      return geometry;
  }

  /**
   * Method that sets the current geometry instance to a new value
   * @param geometry geometry value to take the place of the current geometry
   *                 value for the feature.
   */
  public void setGeometry(Geometry geometry) {
      this.geometry = geometry;
  }

  /**
   * Retrieval method for the properties of the geoJson
   * @return instance of the GeoJsonProperties class
   */
  public GeoJsonProperties getProperties() {
      return properties;
  }

  /**
   * Method that sets the GeoJsonProperties to a new value passed in
   * @param properties new GeoJsonProperties values to takes place of the current
   *                   GeoJsonProperties value for the feature.
   */
  public void setProperties(GeoJsonProperties properties) {
      this.properties = properties;
  }
}