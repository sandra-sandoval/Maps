package sources;

/**
 * This class represents an exception that is thrown if there is an error
 * fetching broadband access
 * data from the source.
 */
public class DataSourceException extends Exception {

  /**
   * This is a one-parameter constructor for the DataSourceException class.
   *
   * @param message the error message that the caller of the exception passes
   */
  public DataSourceException(String message) {
    super(message);
  }
}
