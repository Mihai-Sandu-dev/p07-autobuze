// Hărțile mici, fără zoom și fără tragere, pe tile-uri OSM (D-005): pe pagina de stație, stația
// (data-lat, data-lon, din C-002); pe „Acasă”, favoritele (data-puncte: [[lat, lon], ...], D-057).
// Coordonatele vin randate pe server; harta stă într-un link spre harta mare, deci atribuirea e text simplu.
(function () {
  const link = document.querySelector(".harta-mini");
  if (!link || typeof L === "undefined") return;
  const puncte = link.dataset.puncte
    ? JSON.parse(link.dataset.puncte)
    : [[Number(link.dataset.lat), Number(link.dataset.lon)]];
  const map = L.map(link.querySelector(".harta-leaflet"), {
    zoomControl: false, dragging: false, scrollWheelZoom: false, doubleClickZoom: false,
    boxZoom: false, keyboard: false, touchZoom: false, tap: false
  });
  if (puncte.length === 1) map.setView(puncte[0], 16);
  else map.fitBounds(puncte, { padding: [24, 24], maxZoom: 16 });
  map.attributionControl.setPrefix(false);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19, attribution: "© OpenStreetMap contributors"
  }).addTo(map);
  for (const p of puncte) {
    L.circleMarker(p, { radius: puncte.length === 1 ? 12 : 8, className: "marker-statie ales", interactive: false }).addTo(map);
  }
})();
