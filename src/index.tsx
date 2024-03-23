import React from "react";
import ReactDOM from "react-dom/client";
import "./frontend/styles/index.css";
import App from "./frontend/components/App";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
