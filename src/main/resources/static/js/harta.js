// Harta stațiilor (DS-autobuze-harta). Stațiile vin din GET /api/statii (C-003); căutarea după nume
// se face aici, fără diacritice și fără majuscule, pe „conține” (D-054); „aproape de mine” întreabă
// GET /api/statii/aproape cu locația din browser (D-034). Tile-uri OSM cu atribuire (D-005).
// ?statie=<stop_id> deschide harta pe stația aceea (harta mică și „Vezi pe hartă” de pe pagina de stație).
(function () {
  const $ = id => document.getElementById(id);
  const rezultate = $("rezultate");
  const cauta = $("cauta");
  const aproape = $("aproape");
  const harta = $("harta");

  const map = L.map("harta-leaflet", { zoomControl: true }).setView([45.7983, 24.1256], 13);
  map.attributionControl.setPrefix(false);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  let statii = [];
  let ales = null;          // stația aleasă
  let apropiate = null;     // lista „aproape de mine”, sau null
  let eu = null;            // markerul locației
  const markere = new Map(); // stopId → { punct, tinta }
  let eticheta = null;

  // Fără diacritice și fără majuscule (D-054)
  const fara = s => s.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();

  function el(tag, attrs, ...copii) {
    const e = document.createElement(tag);
    for (const [k, v] of Object.entries(attrs || {})) e.setAttribute(k, v);
    for (const c of copii) e.append(c);
    return e;
  }
  function icon(id) {
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("class", "ic");
    svg.setAttribute("aria-hidden", "true");
    const use = document.createElementNS("http://www.w3.org/2000/svg", "use");
    use.setAttribute("href", "#i-" + id);
    svg.append(use);
    return svg;
  }
  function mesaj(text, eroare) {
    const p = el("p", { class: eroare ? "mesaj eroare" : "mesaj" }, text);
    if (eroare) p.setAttribute("role", "alert");
    rezultate.replaceChildren(p);
  }
  // Distanța rotunjită la 10 m, ca în mock
  const metri = d => Math.round(d / 10) * 10 + " m";

  function lista(titlu, randuri) {
    const ul = el("ul", { class: "lista" });
    for (const { statie, dist } of randuri) {
      const b = el("button", { type: "button", "aria-current": String(ales === statie) },
        icon(dist === undefined ? "pin" : "locatie"), statie.name);
      if (dist !== undefined) b.append(el("span", { class: "dist" }, metri(dist)));
      b.addEventListener("click", () => alege(statie, true));
      ul.append(el("li", {}, b));
    }
    rezultate.replaceChildren(el("h2", {}, titlu), ul);
  }

  function listaCautare() {
    if (apropiate) {
      if (apropiate.length === 0) {
        mesaj("Nicio stație pe o rază de 1 km de tine. Caută stația după nume sau mută harta.");
      } else {
        lista("Cele mai apropiate stații", apropiate);
      }
      return;
    }
    const t = cauta.value.trim();
    if (!t) { mesaj("Scrie numele unei stații sau atinge o stație pe hartă."); return; }
    const gasite = statii.filter(s => fara(s.name).includes(fara(t)));
    if (gasite.length === 0) {
      mesaj("Nicio stație nu se potrivește cu „" + t + "”. Verifică numele sau caut-o pe hartă.");
      return;
    }
    lista(gasite.length === 1 ? "O stație" : gasite.length + " stații", gasite.map(statie => ({ statie })));
  }

  function popup() {
    const loc = $("popup-loc");
    if (!ales) { loc.replaceChildren(); return; }
    const inchide = el("button", { class: "inchide", type: "button", "aria-label": "Închide" }, icon("x"));
    inchide.addEventListener("click", () => alege(null));
    const vezi = el("a", { class: "btn btn-primar", href: "/statie/" + encodeURIComponent(ales.stopId) }, "Vezi plecările");
    loc.replaceChildren(el("div", { class: "popup", role: "dialog", "aria-label": "Stația " + ales.name },
      el("div", { class: "popup-cap" }, el("h2", {}, icon("pin"), ales.name), inchide), vezi));
  }

  function alege(statie, centreaza) {
    if (ales) {
      const vechi = markere.get(ales.stopId).punct;
      vechi.setRadius(8);
      vechi.getElement()?.classList.remove("ales");
    }
    if (eticheta) { eticheta.remove(); eticheta = null; }
    ales = statie;
    if (ales) {
      const m = markere.get(ales.stopId);
      m.punct.setRadius(12);
      m.punct.getElement()?.classList.add("ales");
      m.punct.bringToFront();
      eticheta = L.tooltip({ permanent: true, direction: "bottom", offset: [0, 8], className: "nume-statie" })
        .setLatLng([ales.lat, ales.lon]).setContent(ales.name).addTo(map);
      if (centreaza) map.panTo([ales.lat, ales.lon]);
    }
    popup();
    listaCautare();
  }

  function stare(titlu, text, reincearca) {
    harta.querySelector(".stare-harta")?.remove();
    if (!titlu) return;
    const card = el("div", {}, el("h2", {}, titlu), el("p", {}, text));
    if (reincearca) {
      const b = el("button", { class: "btn btn-primar", type: "button" }, "Reîncearcă");
      b.addEventListener("click", incarca);
      card.append(b);
    }
    const s = el("div", { class: "stare-harta" + (reincearca ? " eroare" : "") }, card);
    if (reincearca) s.setAttribute("role", "alert");
    harta.append(s);
  }

  function deseneaza() {
    for (const s of statii) {
      const punct = L.circleMarker([s.lat, s.lon], { radius: 8, className: "marker-statie", interactive: false });
      // Ținta de atingere: 44 × 44 px (DESIGN.md), transparentă
      const tinta = L.circleMarker([s.lat, s.lon], { radius: 22, className: "marker-tinta" });
      tinta.on("click", () => alege(s, false));
      punct.addTo(map);
      tinta.addTo(map);
      markere.set(s.stopId, { punct, tinta });
    }
  }

  function incarca() {
    stare(null);
    mesaj("Se încarcă stațiile…");
    fetch("/api/statii")
      .then(r => { if (!r.ok) throw new Error(r.status); return r.json(); })
      .then(j => {
        statii = j.stations;
        if (!j.feedImported) {
          rezultate.replaceChildren();
          stare("Orarul nu e încă importat", "Plecările apar după primul import al orarului Tursib. Revino peste câteva minute.");
          return;
        }
        deseneaza();
        const cerut = new URLSearchParams(location.search).get("statie");
        const statie = cerut && statii.find(s => s.stopIds.includes(cerut));
        if (statie) {
          map.setView([statie.lat, statie.lon], 17);
          alege(statie, false);
        } else {
          if (statii.length) map.fitBounds(statii.map(s => [s.lat, s.lon]), { padding: [16, 16] });
          listaCautare();
        }
      })
      .catch(() => {
        rezultate.replaceChildren();
        stare("Nu am putut încărca stațiile",
          "Verifică conexiunea și încearcă din nou. Dacă nu merge, revino peste câteva minute.", true);
      });
  }

  function cautaLocatia(gata) {
    aproape.disabled = gata ? false : true;
    aproape.querySelector("span").textContent = gata ? "Aproape de mine" : "Se caută locația…";
  }

  aproape.addEventListener("click", () => {
    if (!navigator.geolocation) {
      mesaj("Nu am acces la locația ta. Permite accesul din setările browserului sau caută stația după nume.", true);
      return;
    }
    cautaLocatia(false);
    mesaj("Se caută locația ta…");
    navigator.geolocation.getCurrentPosition(pos => {
      const { latitude, longitude } = pos.coords;
      fetch("/api/statii/aproape?lat=" + latitude + "&lon=" + longitude)
        .then(r => { if (!r.ok) throw new Error(r.status); return r.json(); })
        .then(j => {
          cautaLocatia(true);
          cauta.value = "";
          const dupaId = new Map(statii.map(s => [s.stopId, s]));
          apropiate = j.stations.filter(n => dupaId.has(n.stopId))
            .map(n => ({ statie: dupaId.get(n.stopId), dist: n.distanceM }));
          if (eu) eu.remove();
          eu = L.circleMarker([latitude, longitude], { radius: 8, className: "marker-eu", interactive: false }).addTo(map);
          const puncte = [[latitude, longitude], ...apropiate.map(a => [a.statie.lat, a.statie.lon])];
          if (apropiate.length) map.fitBounds(puncte, { padding: [48, 48], maxZoom: 17 });
          else map.setView([latitude, longitude], 15);
          listaCautare();
        })
        .catch(() => {
          cautaLocatia(true);
          mesaj("Nu am putut încărca stațiile. Verifică conexiunea și încearcă din nou.", true);
        });
    }, err => {
      cautaLocatia(true);
      mesaj(err.code === err.PERMISSION_DENIED
        ? "Nu am acces la locația ta. Permite accesul din setările browserului sau caută stația după nume."
        : "Nu am putut afla locația ta. Încearcă din nou sau caută stația după nume.", true);
    }, { timeout: 10000 });
  });

  cauta.addEventListener("input", () => { apropiate = null; listaCautare(); });

  incarca();
})();
