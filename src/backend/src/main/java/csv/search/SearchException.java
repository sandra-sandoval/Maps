package csv.search;

/**
 * This is an error to catch any error that occurs when searching for values in
 * the CSV with
 * CsvSearcher.
 */
public class SearchException extends Exception {

  /**
   * Constructor for the SearchException class, thrown when failed to search the
   * parsed CSV data.
   *
   * @param message the error message supplied when searching fails
   */
  public SearchException(String message) {
    super(message);
  }
}
