import "../styles/main.css";
import { Dispatch, SetStateAction, useEffect } from "react";


/**
 * Props for the ControlledInput component.
 */
interface ControlledInputProps {
  value: string;
  setValue: Dispatch<SetStateAction<string>>;
  ariaLabel: string;
  onKeyDown?: (e: React.KeyboardEvent) => void; //new optional prop to handle pressing submit from controlled input
}



/**
 * A controlled input component corresponding to user's input in the command box.
 * Allows for its value to be managed externally.
 * @param {ControlledInputProps} props - The props for the ControlledInput component.
 */
export function ControlledInput({
  value,
  setValue,
  onKeyDown,
  ariaLabel,

}: ControlledInputProps) {

  return (
    <input
      type="text"
      className="repl-command-box"
      value={value}
      placeholder="Enter command here!"
      onChange={(ev) => setValue(ev.target.value)}
      onKeyDown={onKeyDown}
      aria-label={ariaLabel}
    ></input>
  );
}
