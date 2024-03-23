import { Dispatch, SetStateAction, useState } from "react";
import { ControlledInput } from "../ControlledInput";
import { useEffect } from "react";

/**
 * Props for the MapInput component
 */
interface MapInputProps {
  ariaLabel: string;
  focusMap: (state: string, county: string, broadband: string) => void;
  addFilteredLayer: (keyword: string) => void;
}

/**
 * React component responsible for handling user input and executing commands
 * @param {REPLInputProps} props - The properties required for rendering the component
 */
export function MapInput(props: MapInputProps) {
  const [mapCommandString, setmapCommandString] = useState<string>("");
  const { ariaLabel, focusMap, addFilteredLayer } = props;

  /**
   * Function handling retrieving broadband access percentage via the Map's commandline and displaying the
   * appropriate popup
   *
   * @param {string[]} args - The broadband query parameters
   */
  const handleBroadband = async (args: string[]) => {
    if (args.length !== 2) {
      alert(
        "Invalid broadband retrieval command. Usage: broadband <state> <county>"
      );
      return;
    }
    const state = args[0].replace(/_/g, " ");
    const county = args[1].replace(/_/g, " ");

    try {
      const response = await fetch(
        `http://localhost:3232/broadband?state=${state}&county=${county}`
      );
      if (response.ok) {
        const data = await response.json();
        if (data.result === "success") {
          const percentage = data.broadband_access_percent;
          focusMap(state, county, percentage);
        } else {
          alert("Failed to retrieve broadband data: " + data.error_message);
        }
      } else alert("Failed to fetch data from the backend");
    } catch (error) {
      alert("An error occurred while fetching broadband data: " + error);
    }
  };

  const handleFilter = async (args: string[]) => {
    if (args.length !== 1) {
      alert("Invalid search command. Usage: search your_keyword");
      return;
    }
    addFilteredLayer(args[0]);
  };

  const handleMockFilter = async (args: string[]) => {
    if (args.length !== 1) {
      alert("Invalid search command. Usage: search your_keyword");
      return;
    }
    addFilteredLayer(args[0]);
  };

  /**
   * Function triggered when the "Submit" button is clicked to process the user's command
   * @param {string} commandString - The whole user input
   */
  function handleSubmit(commandString: string) {
    const trimmedCommand = commandString.trim();
    if (trimmedCommand === "") {
      alert("Command cannot be empty");
      return;
    }

    // array of all words entered by user in the command input
    const args = trimmedCommand.split(/\s+/);
    const queries = args.slice(1);
    // checking if call for broadband - assuming the end user is not a developer as mentioned, can be "hard-coded"
    if (args[0] === "broadband") {
      handleBroadband(queries);
    } else if (args[0] === "search") {
      handleFilter(queries);
    } else if (args[0] === "mocksearch") {
      handleMockFilter(queries);
    } else {
      alert("Improper command");
    }
    setmapCommandString("");
  }

  // All keyboard shortcuts here
  // ------------------------------------------------------------------------------
  /**
   * Handles keyboard shortcut to submit by pressing Enter in command box
   * @param e keyboard event of pressing Enter key
   */
  function handleEnterPress(e: React.KeyboardEvent) {
    if (e.key === "Enter") {
      handleSubmit(mapCommandString);
    }
  }

  //---------------------------------------------------------------------------------

  return (
    <div className="map-input" aria-live="polite" aria-label={ariaLabel}>
      <fieldset>
        <legend>Enter a command:</legend>
        <ControlledInput
          value={mapCommandString}
          setValue={setmapCommandString}
          ariaLabel={"Command Input Box to type in commands"}
          onKeyDown={handleEnterPress}
        />
      </fieldset>
      <button onClick={() => handleSubmit(mapCommandString)}>Submit</button>
    </div>
  );
}
