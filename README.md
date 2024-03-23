## Sprint 4: Repl ReadMe

[Repo](https://github.com/cs0320-f23/maps-jzdzilow-spsandov.git)  
In a nutshell: Web-based interactive command-line interface with two modes. The REPL part of the interface allowing for the
loading, retrieval, and search of data through command input as well as fetching of broadband access percent via interaction with the simultaneously running backend (Server), by calls to its appropriate endpoints. Displays either that input's result, or both the result and the command itself. On the other hand, the Map mode/part of the interface displays a world map with redlining
data retrieved from the backend server. In the map mode of the interface, the user is able to input the broadband command
which will redirect them to the state and county from their request and display the broadband data visually on the map, as well as search for areas
associated with the sought-after keyword.

Team members: jzdzilow and spsandov. We worked on the vast majority of the project asynchronously by implementing different functionalities on separate branches, as well as by pair programming, where we sat together and switched roles as the driver/navigator.

Total estimated time: ~30 hours

### Running the program:

0. Upon building the project, run Server.java located on the src/backend/src/main/java/server directory. Install the node package manager via npm install or sudo npm install in case of restriction. Open a new terminal and install express an cors through npm install. Then, in a new terminal, run the frontend via npm start; redirect to the newly opened local host. Inaccurate number of arguments for all commands below will result in an error response (additional arguments aren't ignored - see design choices).

1. Loading data:  
   On the web app, there are two modes of the interface which the user can switch through the button the left hand corner. To load data, upon loading the page, click on "Show REPL" button. Then input "load your_filepath", with the your_filepath
   representing the location containing the to-be-loaded CSV data. Upon successful load, the Command History should contain the result of the performed actions and the filepath of loaded data. Filepath cannot be empty, and must be located in the 'data' directory. If no filepath is passed in, or the filepath either can't be found or is located in an inaccessible directory, the server will return an informative response outlining the issue. Only one file is stored at the time - running command repeatedly overrides the previously fetched content.

   > example: "load data/custom/zillow.csv"

2. Viewing data:  
   Ensure that the web app is in REPL mode. Then, input "view" to display the contents of a most recently loaded dataset.
   Possible only with data previously loaded, and can be only performed on the most recently inputted filepath (only one dataset stored at the time). If successful, Command History should, in addition to previous commands, display a table with the entirety of CSV file's contents as a table; otherwise will return an informative response (either CSV file not loaded, or
   no data to display in case of an empty CSV file).

   > example: "view"

3. Searching through data:  
   Ensure that the web app is in REPL mode, then input "search has_headers your_value your_column_identifier".
   Has_headers corresponds to whether the loaded csv has headers or not, and although they're parsed (and thus displayed) the same way as rows, different input for has_headers influences results of the search (can't search for a value within a row, if it's considered a header). Any input other than true or false implies that the headers aren't present.
   Column identifier corresponds to either the index (starting with 0) or header name to look for if the loaded csv has non-numeric headers as specified by the user via the command. Allows for searching through all columns with the "\*" input.
   The sought-after value must be included, and if is comprised of more than a single word, requires using underscore as an equivalent of whitespace. Applicable also to header names, if non-numeric. Possible only with data previously loaded.
   If successful, Command History will display the result of performed search (all rows containing a sought-after value); otherwise (if any of the parameters are missing, are invalid, or the file hasn't been properly loaded) will return an informative message outlining the issue.
   If no rows satisfying the query been found, will return an informative result (no data to display).

   > example: "search true Alice 0"

4. Fetching broadband data:  
   This can be done on both parts of the webapp.
   For the REPL part of the webapp, input "broadband your_state your_county".
   By default, all commands are using real data by connecting to the API; broadband percent is accessed by passing the request via the Repl backend server to the ACS API.

   County must be within the provided state to return an appropriate broadband access percent. Server utilizes caching - if the data for a particular state/county pair had been retrieved previously, the susbequent request won't trigger a call to the ACS API.

   If successful, Command History will display the the broadband access percent as well as time of retrieval (remains the same upon the second request due to caching - displays the time of the initial data fetch); otherwise (if any of the parameters are missing or are invalid) will return an informative message outlining the issue.

   For the Map part of the webapp, user can similarly input "broadband your_state your_county". Same functionality from the REPL part of the webapp, but rather than displaying the broadband data in the command history, the webapp redirects the user to the location they inputted for the broadband data on the map. Further, a popup on the map will appear with the broadband information fetched from the backend server.

   Alternatively, the user can also click on a desired area, and if it contains redlining overlay features (which allow for the searching of state and city), a popup will appear with the information regarding the state and city (retrieved from the redlining data), as well
   as the broadband percent (with the county retrieved from geo.fcc.api based on the lon lat of the click) of that particular county.

   > example: "broadband North_Carolina Durham"

5. Searching with keyword on map:  
   This functionality is available on the map part of the web app. Input "search your-keyword". Your-keyword being the word you want the map to be filtered by. The front-end calls on the backend server that handles the filtering of the dataset by the inputted keyword and returns all areas containing that word. If the inputted keyword is not found on the backend, an alert
   will appear informing the user that no areas were found with that keyword.

> example: "search school"

6. Redlining:  
   On the Map part of the web app, the map displays redlining data that it fetches from the backend server. No user input is neeed for this data to be displayed on the map, and is rendered by default.

7. Changing the mode:  
   On the REPL part of the web app, Input "mode your-mode". Mode can be either brief or verbose, with the former
   being the default. Upon changing the mode, all elements of the history are rerendered to contain relevant elements of the output. In brief mode, it's exclusively the command's result; in verbose mode, it's both the command, and the result. If mode entered is not valid, or not provided, the Command History will display an informative message. Mode changes are displayed in the Command History as any other command input.

> example: "mode verbose"

8. Registering commands:  
   Input "register command_name function_to_execute".
   Command name is the call required from the user as a REPL prompt for executing a function (just as load, view, search, etc.). Must be a single word value.
   Function to execute is the name (string type) of the handle(...):REPLFunction that is performed whenever user inputs the appropriate command name via the CLI (just as handleLoad, handleView, etc.). Must be included in the REPLInput component; if can't be found by the eval() functions associating that function with a command name, will return an appropriate error response (ReferenceError: your_function is not defined).

   By default, load, view, search, broadband, mode, and register commands are pre-registered (for testing purposes), but can be easily removed from Repl's functionality by not being added to the REPLInput's commandRegistry map upon mount (can be done via commenting out appropriate code lines in one of the useEffect hooks).

   If the command is already in the map, it's not possible to override it (each command name is associaed with only one function to be performed upon actual call). If the to-be-executed function is already associated with a command, it can be used subsequently regardless (can have load1 and load2 both point to handleLoad, but not load1 to point to multiple functions).

> example: "register load2 handleLoad"

9. Mocking:
   Input "mockload any_filepath" to display a mocked json of a successfully loaded csv message.
   No matter, what filepath is inputed, the same response will always generate, because the
   json is mocked in public/load.json.

   Input "mockview" to view a mocked json of csv data. This data will display as a table.
   This command will work regardless of whether you already loaded a file, because it always
   retreives its information from the public/view.json.

   Input "mocksearch hasHeaders any_val any_colID" to view a mocked json of a csv search
   result. No matter what the hasHeaders, any_val, or any_colID argument are the same rows
   of csv data will be displayed as a table. This is because the command will always retreive
   from public/search.json.

   Input "mockbroadband any_state any_county" to return a mocked json of broadband data.
   This command will always return the broadband percent of Durham, North Carolina regardless
   of what any_state and any_county arguments you enter, because the response points to the same
   json in public/broadband.json.

   We've also mocked the backend BroadbandHandler class, creating a CensusSource interface allowing developers to
   check the class's functionality by either retrieving the actual broadband data from the ACS API, or from a predefined,
   hard-coded json file storing appropriate values (in our case, those of North Carolina's counties). See **Structure of the program** for further
   explanation.

10. Accessibility:
    All components and their contents can be vocalized via a ScreenReader, with descriptive aria labels accessed by the program outlining the REPL's elements' functionalities or ways of use. The interface is also fully usable on any Zoom level, due to the utilization of flex boxes and relative (dynamic) sizing using viewport units rather than predefined values.

Keyboard shortcuts provided for simplified, and user-friendly interactions with the REPL interface:

- Ctrl+b; navigates cursor to command box in REPL
- Enter; submits the command without requiring the user to click the button in both Map and REPL

### Design Choices

**The datatypes** corresponding to inputs to the history are the **CommandResultMap** and **history**. The former (a map) stores commands as HistoryItems and associates them with their outputs, while the latter (an array) stores exclusively the HistoryItems, and iterates though them in the REPLHistory class to display them chronologically, while retrieving their corresponding outputs from the CommandResultMap. **commandRegistry**, on the other hand, is a map storing all the currently registered commands that can be accessed via the end-user of the REPL.

**HistoryItem** is an element representing a single command input - it's an interface containing the command (a string), and the time of user's input (a number). It allows for differentiation between the same command values posted at different times, thus potentially with different outputs (f.e. prevents the reassigning of outputs
associated to the same load data/custom/zillow.csv command in the CommandResultMap, depending on whether it has, or hasn't been loaded at the specified time).

**Mode changes** are displayed as a command in the history despite not interacting with the API itself. Upon changing the mode, all previous commands and their outputs are rerendered to display the relevant information (either just the output in mode brief, or both the otuput and the command in mode verbose). The default mode is brief.

**Inputting an erroneous command (either not yet registered, or with invalid inputs)** is still considered a "valid" call: displayed in command history with informative message regarding its erroneous nature.

**Inputting either a command with too many, or too little arguments** is considered an erroneous command, as additional parameters aren't ignored, and missing ones aren't substituted with default values.

**Creating a local server** was needed to host mock jsons. The local server accesses jsons in the file
system that mocks server data. Without the real server backend, this local server mocks the returns
of the real server. fetch statements only take in urls, so creating a local server was necessary to mock how the program would fetch data from a url, parse it into a json, and return it as a promise.

**Accessing broadband percent** is possible both by clicking on a desired area (where the popup displays state, city, name, and broadband percent retrieved from the /broadband endpoint with county data fetched from the geo gov api), or by inputting an appropriate command into the map's command line - the popup then redirect the user to the sought-after area and instead contains the state, county (rather than city), name, and broadband percent. The popup can be then closed with an X symbol.

### High-Level Design

- `App`: The highest-level component that sets up the REPL interface.
- `REPL`: The main component responsible for managing the command history and input.
- `REPLHistory`: A component for displaying the command history and corresponding results.
- `REPLInput`: Handles user input, command execution, and updates to the history.
- `ControlledInput`: A user's input component associated with the command input box.
- `MockedREPLFunctions`: A component for setting up mocked server commands.
- `MapInput`: Handles user input in Map part of the web app and displays on the map.
- `overlays`: A component for setting up the overlays of the map.
- `WrappedMap`: A component for setting up the display of the map.

### Relationships Between Modules/Interfaces

- `App` renders the `REPL` component.
- `REPL` contains `REPLHistory` and `REPLInput`.
- `REPLInput` communicates with `REPLHistory` and updates the command history.
- `REPL` and `REPLHistory` interact with the `commandResultMap`; a `Map` that associates each command with its result, allowing for efficient command history management. `commandResultMap` is passed into `REPLInput`, allowing for the updates to be made. `registerCommandMap` is an REPLInpput-specific dataset containing the names of currently registered command names mapping to the respective, to-be-executed upon call functions. By default, register, load, view, search, and broadband functions as well as their mock equivalents are all included (see "running the program - registering commands")
- `MapInput` and `overlays` interact with `WrappedMap` as `WrappedMap` takes them in to get the data that is to be displayed on the map, including the filered, redlining, and broadband data.
- `MockedREPLFunctions` functions are all imported into `REPLInput` and registered to use.
- `HistoryItem` interface representing a single command input into the history. See "Design Choices" for further explanation.

### Structure of the program:

**The main Server class** is responsible for initializing and running the Spark web server; manages
the /loadcsv, /viewcsv, /searchcsv, and /broadband endpoints and their corresponding handlers,
allowing the user to interact with the program via HTTP requests. Enables the customization
of caching behavior, with a non-empty CacheBuilder input implying the expected use of cache.
Empty (null) CacheBuilder input signifies disabled caching.

**LoadCsvHandler** class is a part of the web server application handling /loadcsv requests
to the server, and is responsible for loading and parsing of CSV data from a specified path.
It's constructed with a CsvDataWrapper object (representing the rows of CSV data as
Lists of Lists of Strings), that, with a successful load request (proper filepath query parameter),
is populated with row data parsed from the specified file, and has its loadedInPast boolean
field set to true (crucial for error handling in the ViewCsvHandler class).
If the filepath query parameter is missing, invalid, or leads to an inaccessible directory (outside
'data' or its subdirectories), server returns a failure response with an informative error
message outlining the issue converted to Json via serialize().

**ViewCsvHandler** class handles /viewcsv requests to the server, and is responsible for displaying
the previously loaded data. It's constructed with a CsvDataWrapper object shared among other handler
classes, that, if previously loaded with parsed data, is returned through a success response
containing all the rows of the loaded CSV file. Otherwise, if data hasn't been previously
loaded (checked via CsvDataWrapper's loadedInPast field), returns a failure response including
an error code and corresponding message converted to Json via serialize().

**SearchCsvHandler** class handles /searchcsv requests to the server, and is responsible for
searching for a user-specified value through the previously loaded data retrieved from the
CsvDataWrapper object shared among other handlers. Request requires headers and value
query parameters (see **Running the Program**), and an optional colId parameter
which are extracted through the handle() method, in accordance with which the search is performed.
If data hasn't been previously loaded (checked via CsvDataWrapper's loadedInPast field),
required parameters are missing, or are invalid, returns a failure response including
an error code and corresponding message converted to Json via serialize(). Otherwise, returns
the result of performed search, provided parameters, and the CSV file's rows matching given criteria.

**BroadbandHandler** class handles /broadband requests to the server, and its primary purpose
is to query and retrieve broadband access information for a user-specified state and county -
either from a cache (depending on the CacheBuilder constructor parameter),
or directly from a data source determined by the CensusSource object
with which it's constructed. The CacheBuilder, if null, implies disabled caching
(all requests query the CensusSource for data); otherwise, if specified, prompts the
previously sought-after state-and-county-broadband to be stored in cache and uses it
upon repeated requests.  
Requests to /broadband require state and county query parameters, which, if valid and found
in the source, are used to return the corresponding broadband access
percentage along with a CensusData object including a Double representing broadband access, and the
time of data retrieval from source by the server. If missing or improper,
they prompt the server to return a failure response with an error code and informative message
outlining the issue.
The getCache() method is used for testing purposes of BroadbandHandler's functionality,
and returns the rows stored within cache (if enabled) or an empty ArrayList (if disabled).

**AcsCensusSource** class is used for obtaining the broadband access percentage through calls
to the ACS API. It implements the CensusSource interface, and overrides its
getBroadbandAccess method. An object of this class is constructed with no arguments, and when the
getBroadbandAccess method is called, an object of this class returns a level of broadband access
(wrapped in a CensusData object) of the given state and county upon making an API call to the census.
If the broadband access level cannot be found, the method
(or a method that it calls) throws a DataSourceException.

**CsvDataWrapper** class is a wrapper class responsible for managing, and modifying the
parsed rows of CSV data. If loaded, has its boolean loadedInPast field set to true by the
LoadCsvHandler class, which is later used for error-checking in other handler classes
(to ensure that there's data to view / search through / get broadband access percentage from).

- Note that these are the error codes we used in the "result" field of an error response object:
  - "error_bad_json" if the request was ill-formed;
  - "error_bad_request" if the request was missing a needed field, or the field was ill-formed; and
  - "error_not_loaded" if the CSV file hasn’t been loaded;

**RedliningHandler** class handles /redlining requests to the server, and its primary purpose
is to query and retrieve geoJson features of areas contained within a specified bounding box -
either from a cache (depending on the CacheBuilder constructor parameter),
or directly from a data source depending on whether the bounding box has been previously queried.
Requests to /redlining require minLon maxLon minLat and maxLat parameters, which, if valid - not null - are used to return
the corresponding features with their location appropriate to the query
along with a time of data retrieval from source by the server, and a response (success or failure).
If missing or improper, they prompt the server to return a failure response with an error code and informative message
outlining the issue. Inputting -90, 90, -180, 180 as respectively minLat maxLat minLon maxLon values implies returning the entirety
of the original dataset, as is used by the **overlays.ts** class to render the whole redlining data on the frontend by default.
The query for redlining within a bounding box can't be utilized on the frontend, and is exclusively designated for potential
developers using the API.

**FilteringHandler** class handles /filter requests to the server, and its primary purpose
is to query and retrieve geoJson features associated with the sought-after keyword.
Requests to /filter require a keywword which if valid - not null - returns
the corresponding features with their area descriptions appropriate to the query
along with a time of data retrieval from source by the server, and a response (success or failure).
If the keyword is null (on the backend - on the frontend, empty input is disabled, as 1 parameter must be provided), returns the entirety of the original geoJson data but isn't stored in the local history; if the keyword doesn't correspond to any areas in the dataset, the class returns an empty
dataset and stores that empty dataset as a value to the keyword. History is persistent and read-only; can be accessed through the getHistory getter method,
which is used for testing (ensuring the persistency of the structure).

**BoundingBox** represents and stores the bounds for the geojson data to be filtered on. Contains lower and upper bounds for
latitude and longitude and setter/getter methods to be acccessible in the redliningHandler class

**FeatureCollection, Feature, GeoJsonProperty, and Geometry** represent the json values within the provided geoJson files; are used to parse
the json into an object and filter based on boundingboxes or keywords.

### Running tests

To run the front-end centered tests for this project, run the java Server.js, install Playwright (cd into repl; input npx playwright install), and run its tests (npx playwright test). For a user-friendly display, input npx playwright test --ui. Testing suite will ensure the reliability of the REPL, checking the state of its rendered components. There are separate test files for the load, view, search, broadband, and mode commands; register is included in the main App.spec testing suite. There is also a test file to check that the page loads correctly (App.spec.ts). You can run these files individually using (npx playwright test <filename>).

`IMPORTANT: Tests using real data WILL FAIL if ran in a different order than specified within test suites (previously loaded data remains stored in the backend server - thus, results of some commands can be different, especially if they were meant to be performed on a non-loaded dataset). To avoid "erronous fails", DON'T execute view/search tests simultaneously, as they'd be interfering with the backend at the same time and modifying the data at the same time as others.`

In addition, there's also a separate test suite that tests the mock functions. To run it, cd into repl, and call node server.js as well as npm start in another terminal, which will set up separate servers for preserving mocked data. This tests that no matter what arguments you enter into "mockload", "mockview", "mocksearch", "mockbroadband", you always output the same data. This is because the json data on the local server is hardcoded to return the same result. The only way to display an error message is if the right number of arguments are not
used.

Examples of front-end tests provided:

<!-- All types of possible commands are mocked, but not all possible combinations within a specified dataset. In case of the command being technically proper (corrent number of parameters, etc.) but not having been mocked, Command History will display a relevant message.
Examples of mocked commands/responses: -->

- load commands with proper filepaths
- load commands with nonexistent files
- load commands with filepaths in inaccessible directories (other than data/)
- load commands with no filepath included
- view commands with empty datasets
- view commands with regular (y x y, with y!=0) datasets
- view commands with single-column datasets
- view commands with single-row datasets
- view commands with no datasets loaded
- view commands with datasets with headers
- view commands with datasets with no headers
- search commands with all proper values (either header titles or indices)
- search commands with nonexistent column titles
- search commands with column indices out of bounds
- search commands with multiple rows as a result
- search commands with single row as a result
- search commands with no sought-after rows found
- search commands on not loaded datasets
- search commands on empty datasets
- broadband command with valid parameters
- broadband command with missing parameters
- broadband command with parameters not found
  Back-end tests checking the appropriateness of load/view/search/broadband functions utilized in the front-end are provided within the backend/src/test/server directory, and can be run via right-clicking on the desired suite.
- map functionalities (whether it's displayed, whether popups appear as appropriate error messages)

### Whose Labor?

1. Spark java (backend) - for setting up the endpoints; allowing for interactions with the API
2. React Map Gl (frontend) - for providing the map and it's functionalities; allows for integrating Mapbox GL into the frontend
3. Google Guava Cache (backend) - for preserving data queried beforehand in BroadbandHandler and RedliningHandler classes
4. Moshi (backend) - for serializing and deserializing json data for filtering by bounding boxes and keywords, as well as returning appropriate
   responses from the backend API - including response codes and messages - for informative interaction and efficient debugging
5. GeoJSON (frontend) - for interactions with the provided, encoded GeoJSON data (allows for the rendering of redlining areas, as well as looking for keywords within area descriptions of the dataset's features)
6. Mapbox GL (frontend) - for providing interactive and customizeable maps via the browser app
7. Playwright (frontend) - for frontend, automated interaction testing
8. java.io (backend) - for elemental input/output operations on the backend
9. java.util (backend) - for fundamental data structures and utility classes required for ensuring the functionality of the program
10. Kotlin.pair (backend) - for CSV parsing
11. Okio (backend) - for efficient I/O operations (and error handling)
12. JUnit (backend) - for unit testing of the backend

### Errors/Bugs

None that we know of!
