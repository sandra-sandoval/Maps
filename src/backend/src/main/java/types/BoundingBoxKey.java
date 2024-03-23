package types;

import java.util.Objects;

/**
 * Class that represents and stores the bounds for the geojson data to be
 * filtered on. Contains
 * lower and upper bounds for latitude and longitude and setter/getter methods
 * to be acccessible
 * in the redliningHandler class
 */
public class BoundingBoxKey {
  private final Double minLat;
  private final Double maxLat;
  private final Double minLon;
  private final Double maxLon;

  /**
   * BoundingBoxKey class constructor that initializes the passed in bounds to
   * instance variables
   * 
   * @param minLat lower bound for latitude
   * @param maxLat upper bound for latitude
   * @param minLon lower bound for longitude
   * @param maxLon upper bound for longitude
   */
  public BoundingBoxKey(Double minLat, Double maxLat, Double minLon, Double maxLon) {
    this.minLat = minLat;
    this.maxLat = maxLat;
    this.minLon = minLon;
    this.maxLon = maxLon;
  }

  /**
   * Method that retrieves the lower bound latitude for the data to be bounded by
   * 
   * @return the lower bound
   */
  public Double getMinLat() {
    return this.minLat;
  }

  /**
   * Method that retrieves the upper bound latitude for the data to be bounded by
   * 
   * @return the upper bound
   */
  public Double getMaxLat() {
    return this.maxLat;
  }

  /**
   * Method that retrieves the lower bound longitude for the data to be bounded by
   * 
   * @return the lower bound
   */
  public Double getMinLon() {
    return this.minLon;
  }

  /**
   * Method that retrieves the lower bound longitude for the data to be bounded by
   * 
   * @return the upper bound
   */
  public Double getMaxLon() {
    return this.maxLon;
  }

  /**
   * Method that retrieves the hashcode
   * 
   * @return the hashcode variable
   */
  @Override
  public int hashCode() {
    return Objects.hash(minLat, maxLat, minLon, maxLon);
  }

  /**
   * Method that checks if the object passed in is equal to the current
   * boundingboxkey
   * 
   * @return a boolean indicating if it is equal
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    BoundingBoxKey that = (BoundingBoxKey) o;
    return Objects.equals(minLat, that.minLat) &&
        Objects.equals(maxLat, that.maxLat) &&
        Objects.equals(minLon, that.minLon) &&
        Objects.equals(maxLon, that.maxLon);
  }
}
