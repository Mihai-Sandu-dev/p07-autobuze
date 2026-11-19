// Butonul de temă din antet (D-039). Tema urmează sistemul până când utilizatorul apasă
// butonul; alegerea (light sau dark) se ține în localStorage și are prioritate.
// Alegerea salvată se aplică încă din <head> (vezi fragments/head.html), fără clipire.
(function () {
  const CHEIE = "tema";
  const root = document.documentElement;
  const sistemDark = window.matchMedia("(prefers-color-scheme: dark)");
  const buton = document.querySelector(".tema");
  if (!buton) return;

  function eDark() {
    return root.dataset.theme ? root.dataset.theme === "dark" : sistemDark.matches;
  }
  function actualizeaza() {
    buton.setAttribute("aria-pressed", String(eDark()));
  }

  buton.addEventListener("click", function () {
    const noua = eDark() ? "light" : "dark";
    root.dataset.theme = noua;
    try { localStorage.setItem(CHEIE, noua); } catch (e) { /* stocare blocată: tema ține până la reîncărcare */ }
    actualizeaza();
  });
  sistemDark.addEventListener("change", actualizeaza);
  actualizeaza();
})();
