import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";
import { isGeneratorFunction } from "util/types";
import * as fs from "fs/promises";
import * as mockedJson from "./mockGeoJson.json";

const propertyName = "holc_grade";
export const geoLayer: FillLayer = {
  id: "redlining",
  type: "fill",
  paint: {
    "fill-color": [
      "match",
      ["get", propertyName],
      "A",
      "#5bcc04",
      "B",
      "#04b8cc",
      "C",
      "#e9ed0e",
      "D",
      "#d11d1d",
      "#ccc",
    ],
    "fill-opacity": 0.2,
  },
};

export const filteredLayer: FillLayer = {
  id: "filteredLayer",
  type: "fill",
  paint: {
    "fill-color": "#fe3fb3",
    "fill-opacity": 0.6,
  },
};

function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}

export async function fetchRedliningData(): Promise<
  GeoJSON.FeatureCollection | undefined
> {
  try {
    const response = await fetch(
      "http://localhost:3232/redlining?minLat=-90&maxLat=90&minLon=-180&maxLon=180"
    );
    if (response.ok) {
      const data = await response.json();
      // return isFeatureCollection(data) ? data : undefined;
      if (data.result === "success") {
        return isFeatureCollection(data.data) ? data.data : undefined;
      }
    }
  } catch (error) {
    return undefined;
  }
}

export async function fetchFilteredData(
  keyword: String
): Promise<GeoJSON.FeatureCollection | undefined> {
  try {
    const words = keyword.replace(/_/g, " ");
    const response = await fetch(
      `http://localhost:3232/filter?keyword=${words}`
    );
    if (response.ok) {
      const data = await response.json();
      if (data.result === "success") {
        return isFeatureCollection(data.data) ? data.data : undefined;
      }
    }
  } catch (error) {
    return undefined;
  }
}

export async function fetchMockedData(): Promise<
  GeoJSON.FeatureCollection | undefined
> {
  try {
    return isFeatureCollection(mockedJson) ? mockedJson : undefined;
  } catch (error) {
    return undefined;
  }
}
