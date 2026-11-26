// Formularele de cont (DS-autobuze-login, DS-autobuze-inregistrare): un câmp gol își arată mesajul
// (data-gol) fără să trimită formularul; altfel butonul arată că se trimite (data-trimite).
// Serverul verifică oricum totul (D-032).
(function () {
  const form = document.querySelector("form.card[data-trimite]");
  if (!form) return;
  form.addEventListener("submit", e => {
    let gol = false;
    for (const input of form.querySelectorAll("input[data-gol]")) {
      const eroare = document.getElementById("eroare-" + input.id);
      const lipsa = !input.value;
      if (lipsa) {
        eroare.textContent = input.dataset.gol;
        eroare.hidden = false;
        input.setAttribute("aria-invalid", "true");
        gol = true;
      } else if (eroare.textContent === input.dataset.gol) {
        eroare.hidden = true;
        input.removeAttribute("aria-invalid");
      }
    }
    if (gol) {
      e.preventDefault();
      return;
    }
    const buton = form.querySelector("button[type=submit]");
    buton.disabled = true;
    buton.textContent = form.dataset.trimite;
  });
})();
