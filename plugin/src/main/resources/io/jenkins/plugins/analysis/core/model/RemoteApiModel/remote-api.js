/**
 * Client side behavior of the remote API page of the Warnings tab:
 * <ul>
 *   <li>Shows the endpoints of the selected tool (or of the aggregation of all tools). The selection is stored in
 *   the local storage of the browser. Links can preselect a tool in the fragment of the URL, e.g.,
 *   'remote-api#tool=pmd'. Such a selection is not stored, only selections that the user changes on the page are
 *   stored.</li>
 *   <li>Calls the main endpoint of the selected tool in the browser (with the session of the current user) and
 *   shows the formatted JSON document.</li>
 *   <li>Sizes the response so that it fills the browser viewport, the same way the log page sizes its cards (see
 *   log-model.js).</li>
 * </ul>
 */
(function () {
  const TOOL_SELECTOR = "#wng-remote-api-tool";
  const TOOL_ICON_SELECTOR = ".wng-app-bar__tool-icon";
  const PANEL_SELECTOR = ".wng-remote-api__panel";
  const RESPONSE_SELECTOR = "#wng-remote-api-response";
  const FOOTER_SELECTOR = ".page-footer";
  const HIDDEN_CLASS = "jenkins-hidden";

  const MIN_HEIGHT = 150;
  const BOTTOM_MARGIN = 40;

  // The remote API page shows all tools of a run, so the setting is not stored per report ID.
  const STORAGE_KEY = "wng-remote-api-tool";

  // Name of the setting in the fragment of the URL, e.g., 'remote-api#tool=pmd'
  const TOOL_PARAMETER = "tool";

  let resizeTimeout;
  let request = 0;

  function readStorage() {
    try {
      return localStorage.getItem(STORAGE_KEY);
    } catch (e) {
      return null;
    }
  }

  function writeStorage(value) {
    try {
      localStorage.setItem(STORAGE_KEY, value);
    } catch (e) {
      // ignore: the setting is not persisted if the storage is not available
    }
  }

  function hasTool(select, value) {
    return value !== null && Array.from(select.options).some((option) => option.value === value);
  }

  /**
   * Selects the tool that is given in the fragment of the URL. If there is no such tool, the tool that has been
   * stored the last time is selected. Otherwise, the preselected aggregation of all tools remains.
   */
  function restoreSelection(select) {
    const requested = new URLSearchParams(window.location.hash.replace(/^#/, "")).get(TOOL_PARAMETER);
    const value = [requested, readStorage()].find((candidate) => hasTool(select, candidate));
    if (value !== undefined) {
      select.value = value;
    }
  }

  function getFooterHeight() {
    const footer = document.querySelector(FOOTER_SELECTOR);
    if (!footer) {
      return 0;
    }
    return footer.getBoundingClientRect().height;
  }

  function fitToViewport(response) {
    const bottomBoundary = window.innerHeight - getFooterHeight() - BOTTOM_MARGIN;
    const top = response.getBoundingClientRect().top;
    response.style.height = Math.max(bottomBoundary - top, MIN_HEIGHT) + "px";
  }

  /**
   * Calls the remote API and shows the formatted JSON. The text is set via textContent so that the values of the
   * response are never interpreted as HTML. Responses of previous selections are ignored.
   */
  function loadResponse(response, url) {
    const current = ++request;
    response.textContent = response.dataset.loading;
    fetch(url, { credentials: "same-origin", headers: { Accept: "application/json" } })
      .then((result) => {
        if (!result.ok) {
          throw new Error(result.status + " " + result.statusText);
        }
        return result.json();
      })
      .then((json) => {
        if (current === request) {
          response.textContent = JSON.stringify(json, null, 2);
        }
      })
      .catch((error) => {
        if (current === request) {
          response.textContent = response.dataset.error + " " + error.message;
        }
      });
  }

  /**
   * Shows the icon and the endpoints of the selected tool, hides the icons and endpoints of all other tools, and
   * loads the response of the main endpoint of the selected tool.
   */
  function showSelectedTool(select, response) {
    const tool = select.value;
    document.querySelectorAll(TOOL_ICON_SELECTOR + ", " + PANEL_SELECTOR).forEach((element) => {
      element.classList.toggle(HIDDEN_CLASS, element.dataset.tool !== tool);
    });
    const panel = document.querySelector(PANEL_SELECTOR + '[data-tool="' + CSS.escape(tool) + '"]');
    if (panel) {
      loadResponse(response, panel.dataset.url);
    }
    fitToViewport(response);
  }

  function initialize() {
    const select = document.querySelector(TOOL_SELECTOR);
    const response = document.querySelector(RESPONSE_SELECTOR);
    if (!select || !response) {
      return;
    }

    restoreSelection(select);
    showSelectedTool(select, response);

    select.addEventListener("change", () => {
      writeStorage(select.value);
      showSelectedTool(select, response);
    });

    window.addEventListener("resize", function () {
      clearTimeout(resizeTimeout);
      resizeTimeout = setTimeout(function () {
        fitToViewport(response);
      }, 150);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initialize);
  } else {
    initialize();
  }
})();
