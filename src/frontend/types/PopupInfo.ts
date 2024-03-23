/**
 * Interface responsible for popup info.
 * @param {number} longtitude - location lon of popup
 * @param {number} latitude - location lat of popup
 * @param {React.ReactNode} content - content of popup
 */
export interface PopupInfo {
  longitude: number;
  latitude: number;
  content: React.ReactNode;
}
