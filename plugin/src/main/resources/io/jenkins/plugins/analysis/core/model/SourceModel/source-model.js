/**
 * Client side behavior of the source code page of the Warnings tab:
 * <ul>
 *   <li>Selects the issue of the link. The links on the issues page contain the line of the issue as fragment of the
 *   URL (or no fragment at all). Such a fragment is replaced by the ID of the marker of the selected issue, so the
 *   source code view of the Prism API plugin selects exactly this issue. A fragment that already contains the ID of
 *   a marker is not changed, so the selection survives a reload of the page.</li>
 *   <li>Filters the issues by the settings of the app bar: all issues of the file or only the selected issue, the
 *   minimum severity, and the baseline (new or outstanding issues). Hidden issues are marked with the attribute
 *   'hidden', afterwards the event 'prism-markers-changed' notifies the source code view. The settings are stored in
 *   the local storage of the browser.</li>
 * </ul>
 */
(function () {
  const SHOW_SELECTOR = "#wng-source-show";
  const SEVERITY_SELECTOR = "#wng-source-severity";
  const BASELINE_SELECTOR = "#wng-source-baseline";

  // The source code page shows the issues of all tools, so the settings are not stored per report ID.
  const STORAGE_PREFIX = "wng-source-";

  // Severities of the issues, see SourceModel#asMarker (data-marker-severity)
  const SEVERITY_RANKS = { low: 1, normal: 2, high: 3, error: 4 };

  const source = document.getElementById("wng-source");
  const hash = window.location.hash;
  if (source && (hash.length <= 1 || /^#\d+$/.test(hash))) {
    history.replaceState(null, "", "#" + source.dataset.selectedMarker);
  }

  function readStorage(key) {
    try {
      return localStorage.getItem(STORAGE_PREFIX + key);
    } catch (e) {
      return null;
    }
  }

  function writeStorage(key, value) {
    try {
      localStorage.setItem(STORAGE_PREFIX + key, value);
    } catch (e) {
      // ignore: settings are not persisted if the storage is not available
    }
  }

  function getValue(selector) {
    const select = document.querySelector(selector);
    return select ? select.value : "";
  }

  function getSelectedId() {
    return decodeURIComponent(window.location.hash.substring(1));
  }

  function getRank(severity) {
    // custom severities are always shown
    return SEVERITY_RANKS[severity] || Number.MAX_SAFE_INTEGER;
  }

  function isVisible(marker, settings) {
    if (settings.show === "selected" && marker.id !== getSelectedId()) {
      return false;
    }
    if (getRank(marker.dataset.markerSeverity) < getRank(settings.severity || "low")) {
      return false;
    }
    return (
      settings.baseline === "" ||
      settings.baseline === "all" ||
      marker.dataset.markerState === settings.baseline
    );
  }

  function filterMarkers() {
    const settings = {
      show: getValue(SHOW_SELECTOR),
      severity: getValue(SEVERITY_SELECTOR),
      baseline: getValue(BASELINE_SELECTOR),
    };
    document.querySelectorAll(".prism-source .prism-marker").forEach((marker) => {
      marker.hidden = !isVisible(marker, settings);
    });
    document.dispatchEvent(new CustomEvent("prism-markers-changed"));
  }

  function initializeSelect(selector, key) {
    const select = document.querySelector(selector);
    if (!select) {
      return;
    }
    const stored = readStorage(key);
    if (stored !== null && Array.from(select.options).some((option) => option.value === stored)) {
      select.value = stored;
    }
    select.addEventListener("change", () => {
      writeStorage(key, select.value);
      filterMarkers();
    });
  }

  function initialize() {
    initializeSelect(SHOW_SELECTOR, "show");
    initializeSelect(SEVERITY_SELECTOR, "severity");
    initializeSelect(BASELINE_SELECTOR, "baseline");
    filterMarkers();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initialize);
  } else {
    initialize();
  }
  // in mode 'selected' the visible issue depends on the fragment of the URL
  window.addEventListener("hashchange", filterMarkers);
})();
