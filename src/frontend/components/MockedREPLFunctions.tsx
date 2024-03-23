import { REPLFunction } from "../types/REPLFunction";

const loadFilePath = "http://localhost:3000/load"
const viewFilePath = "http://localhost:3000/view"
const searchFilePath = "http://localhost:3000/search"
const broadbandFilePath = "http://localhost:3000/broadband"
/**
 * Function mocking handle of loading the file
 * @param {string[]} args - The file path of a file to be loaded (must be within the data directory)
 */
export const handleMockLoad: REPLFunction = async (
  args: string[]
): Promise<string> => {
  if (args.length !== 1) {
    return "Invalid usage of 'mockload' command. Usage: mockload <URL>";
  }
  const filepath = args[0].trim();
  try {
    const response = await fetch(loadFilePath);
    if (response.ok) {
      const data = await response.json();
      const resultMessage =
        data.result === "success"
          ? "File " + filepath + " loaded successfully"
          : data.error_message;
      return resultMessage;
    } else return "Failed to fetch data from the mocked backend";
  } catch (error) {
    // Implement the logic to load the file based on the provided 'url'.
    // Return an appropriate result message.
    return "An error ocurred while loading the file: " + error;
  }
};
/**
 * Function mocking handle of viewing the loaded dataset
 * @param {string[]} args - None (empty)
 */
export const handleMockView: REPLFunction = async (
  args: string[]
): Promise<string> => {
  if (args.length !== 0) {
    return "Invalid usage of 'mockview' command. Usage: mockview";
  }
  try {
    const response = await fetch(viewFilePath);
    if (response.ok) {
      const data = await response.json();
      const resultMessage =
        data.result === "success" ? data.data : data.error_message;
      return resultMessage;
    } else return "Failed to fetch data from the mocked backend";
  } catch (error) {
    return "An error occurred while viewing the file: " + error;
  }
};
/**
 * Function mocking handle of searching within the loaded dataset
 * @param {string[]} args - The search query parameters
 */
export const handleMockSearch: REPLFunction = async (
  args: string[]
): Promise<string> => {
  if (args.length !== 3) {
    return "Invalid mocksearch command. Usage: mocksearch <hasHeaders> <value> <columnId>";
  }

  try {
    const response = await fetch(searchFilePath);
    if (response.ok) {
      const data = await response.json();
      const resultMessage =
        data.result === "success" ? data.data : data.error_message;
      return resultMessage;
    } else return "Failed to fetch data from the mocked backend";
  } catch (error) {
    return "An error occurred while searching through the file: " + error;
  }
};

/**
 * Function mocking handle of retrieving broadband access percentage
 * @param {string[]} args - The broadband query parameters
 */
export const handleMockBroadband: REPLFunction = async (
  args: string[]
): Promise<string> => {
  if (args.length !== 2) {
    return "Invalid mockbroadband retrieval command. Usage: mockbroadband <state> <county>";
  }

  try {
    const response = await fetch(broadbandFilePath);
    if (response.ok) {
      const data = await response.json();
      const resultMessage =
        data.result === "success"
          ? `time of retrieval: ${data.date_time} broadband access percent: ${data.broadband_access_percent}`
          : data.error_message;
      return resultMessage;
    } else return "Failed to fetch data from the mocked backend";
  } catch (error) {
    return "An error occurred while fetching broadband data: " + error;
  }
};
