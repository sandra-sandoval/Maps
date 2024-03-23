import { Dispatch, SetStateAction, useState } from "react";
import "../styles/main.css";
import { REPLHistory } from "./REPLHistory";
import { REPLInput } from "./REPLInput";
import { HistoryItem } from "../types/HistoryItem";
import WrappedMap from "./map/WrappedMap";
import { MapInput } from "./map/MapInput";

/**
 * React component allowing users to input commands;
 * Displays corresponding command history, and shows the results of each command.
 * Depending on the mode selected, can display either just the output,
 * or both the user's command and the output.
 */
export default function REPL() {
  const [history, setHistory] = useState<HistoryItem[]>([]);
  const [mode, setMode] = useState<string>("brief");
  const [commandResultMap, setCommandResultMap] = useState(new Map());
  const [showMap, setShowMap] = useState(true);

  /**
   * Update the command result and add it to the command history.
   * @param {string} command - The user's input command.
   * @param {[[]] | string } result - The result of the command execution.
   */
  function updateCommandResult(command: string, result: [[]] | string) {
    const historyItem: HistoryItem = {
      command: command,
      timestamp: new Date().getTime(),
    };
    commandResultMap.set(historyItem, result);
    setHistory((prevHistory) => [...prevHistory, historyItem]);
  }

  return (
    <div className="repl">
      <div className="button-container">
        <button onClick={() => setShowMap(!showMap)}>
          {showMap ? "Show REPL" : "Show Map"}
        </button>
      </div>

      {showMap ? (
        <div className="map-container">
          <WrappedMap />
        </div>
      ) : (
        <div className="repl-container">
          <REPLHistory
            commandHistory={history}
            mode={mode}
            commandResultMap={commandResultMap}
            ariaLabel="History Log Display to show past commands inputted"
          />
          <hr></hr>
          <REPLInput
            history={history}
            setHistory={setHistory}
            mode={mode}
            setMode={setMode}
            commandResultMap={commandResultMap}
            updateCommandResult={updateCommandResult}
            ariaLabel="Input Command Component to take in and process command inputs"
          />
        </div>
      )}
    </div>
  );
}
