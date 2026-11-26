// Harta mică de pe pagina de stație (DS-autobuze-statie): stația pe tile-uri OSM (D-005), fără zoom
// și fără tragere. Coordonatele vin randate pe server (data-lat, data-lon, din C-002); harta stă
// într-un link spre harta mare, deci atribuirea e text simplu, fără alt link înăuntru.
(function () {
  const link = document.querySelector(".harta-mini");
  if (!link || typeof L === "undefined") return;
  const pos = [Number(link.dataset.lat), Number(link.dataset.lon)];
  const map = L.map(link.querySelector(".harta-leaflet"), {
    zoomControl: false, dragging: false, scrollWheelZoom: false, doubleClickZoom: false,
    boxZoom: false, keyboard: false, touchZoom: false, tap: false
  }).setView(pos, 16);
  map.attributionControl.setPrefix(false);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19, attribution: "© OpenStreetMap contributors"
  }).addTo(map);
  L.circleMarker(pos, { radius: 12, className: "marker-statie ales", interactive: false }).addTo(map);
})();
