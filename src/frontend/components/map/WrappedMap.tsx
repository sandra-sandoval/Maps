import Map, {
  Layer,
  LngLat,
  MapLayerMouseEvent,
  MapLayerTouchEvent,
  MapRef,
  PointLike,
  Source,
  ViewStateChangeEvent,
} from "react-map-gl";
import "mapbox-gl/dist/mapbox-gl.css";
import { ACCESS_TOKEN } from "../../private /api";
import React, { useEffect, useRef, useState } from "react";
import {
  fetchFilteredData,
  fetchMockedData,
  fetchRedliningData,
  filteredLayer,
  geoLayer,
} from "./overlays";
import { Popup } from "react-map-gl";
import { MapInput } from "./MapInput";
import { PopupInfo } from "../../types/PopupInfo";

const WrappedMap = () => {
  const mapRef = useRef<MapRef>(null);
  const [viewState, setViewState] = useState({
    longitude: 19.944544,
    latitude: 50.049683,
    zoom: 1,
  });
  const [redliningOverlay, setRedliningOverlay] = useState<
    GeoJSON.FeatureCollection | undefined
  >(undefined);
  const [filteredOverlay, setFilteredOverlay] = useState<
    GeoJSON.FeatureCollection | undefined
  >(undefined);
  const [popupInfo, setPopupInfo] = useState<PopupInfo | null>(null);

  useEffect(() => {
    fetchRedliningData().then((redlining) => setRedliningOverlay(redlining));
  }, []);

  const focusMap = (state: string, county: string, broadband: string) => {
    fetch(
      `https://api.mapbox.com/geocoding/v5/mapbox.places/${county},${state}.json?access_token=${ACCESS_TOKEN}`
    )
      .then((response) => response.json())
      .then((data) => {
        // Extract coordinates from the response
        const coordinates = data.features[0].geometry.coordinates;
        if (mapRef.current) {
          // Use the obtained coordinates to set the map focus
          mapRef.current.flyTo({
            center: coordinates,
            zoom: 10, // Adjust the zoom level as needed
          });
          const popupContent = (
            <div>
              <p>State: {state || "N/A"}</p>
              <p>County: {county || "N/A"}</p>
              <p>Broadband Access Percent: {broadband || "N/A"}</p>
            </div>
          );

          setPopupInfo({
            longitude: coordinates[0],
            latitude: coordinates[1],
            content: popupContent,
          });
        }
      })
      .catch((error) => alert("Error encountered: " + error));
  };

  async function onMapClick(e: MapLayerMouseEvent) {
    // don't want to call if no overlay
    if (!redliningOverlay || !mapRef.current) {
      return;
    }
    const bbox: [PointLike, PointLike] = [
      [e.point.x, e.point.y],
      [e.point.x, e.point.y],
    ];
    const features = mapRef.current.queryRenderedFeatures(bbox, {
      layers: ["redlining"],
    });
    if (features.length > 0) {
      const feature = features[0];
      if (feature.properties) {
        const state = feature.properties.state;
        const city = feature.properties.city;
        const name = feature.properties.name;
        const lngLat = e.lngLat;
        const lon = lngLat.lng;
        const lat = lngLat.lat;

        const broadband = await fetchBroadband(lat, lon);
        const popupContent = (
          <div>
            <p>State: {state || "N/A"}</p>
            <p>City: {city || "N/A"}</p>
            <p>Broadband Access Percent: {broadband || "N/A"}</p>
            <p>Name: {name || "N/A"}</p>
          </div>
        );

        setPopupInfo({
          longitude: lon,
          latitude: lat,
          content: popupContent,
        });
      }
    } else {
      // No features found, hide the popup
      setPopupInfo(null);
    }
  }

  async function fetchBroadband(
    lat: number,
    lon: number
  ): Promise<number | undefined> {
    const countyResponse = await fetch(
      "https://geo.fcc.gov/api/census/area?lat=" +
        lat +
        "&lon=" +
        lon +
        "&censusYear=2020&format=json"
    );
    if (countyResponse.ok) {
      const data = await countyResponse.json();
      const countyFull: string = data.results[0].county_name;
      const county: string = countyFull.replace(" County", "").trim();
      const state = data.results[0].state_name;
      const broadbandResponse = await fetch(
        `http://localhost:3232/broadband?state=${state}&county=${county}`
      );
      if (broadbandResponse.ok) {
        const data2 = await broadbandResponse.json();
        const resultMessage =
          data2.result === "success"
            ? data2.broadband_access_percent
            : data2.error_message;
        return resultMessage;
      }
      return undefined;
    }
  }

  const addFilteredLayer = (keyword: string) => {
    if (keyword == "mock") {
      fetchMockedData().then((filteredData) => {
        setFilteredOverlay(filteredData);
        alert("Mocked data overlayed successfully");
      });
    } else {
      fetchFilteredData(keyword).then((filteredData) => {
        if (filteredData?.features.length === 0) {
          alert("No areas matching sought-after keyword found");
          setFilteredOverlay(undefined);
        } else setFilteredOverlay(filteredData);
      });
    }
  };

  return (
    <div className="map-container">
      <Map
        ref={mapRef}
        mapboxAccessToken={ACCESS_TOKEN}
        longitude={viewState.longitude}
        latitude={viewState.latitude}
        zoom={viewState.zoom}
        onMove={(ev: ViewStateChangeEvent) => setViewState(ev.viewState)}
        onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
        style={{ width: window.innerWidth, height: window.innerHeight }}
        mapStyle={"mapbox://styles/mapbox/streets-v12"}
      >
        <Source id="redliningLayer" type="geojson" data={redliningOverlay}>
          <Layer {...geoLayer} />
        </Source>
        {filteredOverlay && (
          <Source id="filteredLayer" type="geojson" data={filteredOverlay}>
            <Layer {...filteredLayer} />
          </Source>
        )}
        {popupInfo && (
          <Popup
            longitude={popupInfo.longitude}
            latitude={popupInfo.latitude}
            closeButton={true}
            closeOnClick={false}
            onClose={() => setPopupInfo(null)}
          >
            {popupInfo.content}
          </Popup>
        )}
      </Map>
      <MapInput
        ariaLabel="Command Box for Inputting Map Requests"
        focusMap={focusMap}
        addFilteredLayer={addFilteredLayer}
      />
    </div>
  );
};

export default WrappedMap;
