/**
 * Selects the issue of the link in the source code view. The links on the issues page contain the line of the issue
 * as fragment of the URL (or no fragment at all). Such a fragment is replaced by the ID of the marker of the selected
 * issue, so the source code view of the Prism API plugin selects exactly this issue. A fragment that already
 * contains the ID of a marker is not changed, so the selection survives a reload of the page.
 */
(function () {
  const source = document.getElementById("wng-source");
  const hash = window.location.hash;
  if (source && (hash.length <= 1 || /^#\d+$/.test(hash))) {
    history.replaceState(null, "", "#" + source.dataset.selectedMarker);
  }
})();
