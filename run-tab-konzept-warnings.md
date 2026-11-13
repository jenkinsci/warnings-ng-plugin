# Run-Tab-Konzept: vom Coverage Plugin auf das Warnings Plugin übertragen

Stand: 12.11.2026. Basis: `jenkinsci/coverage-plugin` @ `34a2946` (main), Branch `run-column` (`f277746`) und `jenkinsci/warnings-ng-plugin` @ `0755424` (main).

Dieses Dokument fasst zusammen, wie der neue Run Tab im Coverage Plugin aufgebaut ist, welche Erkenntnisse wir dabei gewonnen haben und welche Konventionen für den Code gelten. Es ist die Vorlage für die Umsetzung im Warnings Plugin.

---

## 1. Ausgangslage

**Beide Plugins haben bereits eine Klasse `RunTab`.** Sie erweitert jeweils `jenkins.model.Tab` und wird per `TransientActionFactory<Run>` (`RunTabFactory`) an jeden Build angehängt. Der Coverage-`RunTab` stammt ursprünglich aus dem Warnings Plugin. Daran erinnert noch der Javadoc „Renders a dynamic warning action of the Warnings tab“ in `coverage-plugin/.../RunTab#getDynamic`.

Der Unterschied liegt in der Navigation:

| | Warnings (heute) | Coverage (neu) |
|---|---|---|
| Sidebar-Einträge | ein Eintrag **pro Tool** (`ResultAction`), jeweils mit Badge | ein Eintrag **pro Ansicht** eines Ergebnisses (Overview, Trend, Treemap …) |
| Inhalt einer Seite | die komplette alte Detailseite (`IssuesDetail` → `filled.jelly`) mit Bootstrap-Tabs und Karten | eine eigene Seite pro Ansicht, jeweils mit eigenem Model und eigener Jelly-View |
| Legacy-UI | dieselbe Seite, nur ohne Sidebar (`d:taglib`-Trick in `IssuesDetail/index.jelly`) | getrennte Views: `run-tab.jelly` gegenüber `legacy.jelly`. Die Legacy-Seite bleibt unverändert. |

Das Ziel ist, im Warnings Plugin denselben Schritt zu gehen wie im Coverage Plugin: weg von „alte Seite in neuer Hülle“, hin zu eigenständigen, fokussierten Ansichten.

---

## 2. Architektur des Coverage Run Tab

### 2.1 URL-Schema und Objektgraph (Stapler)

```
/job/x/42/coverage/                 → RunTab#doIndex      → Redirect auf erstes Ergebnis + /overview
/job/x/42/coverage/<id>/            → CoverageViewModel#doIndex → Redirect auf .../<id>/overview
/job/x/42/<id>/overview             → CoverageViewModel#getDynamic("overview") → OverviewModel
/job/x/42/<id>/trend                → TrendModel
/job/x/42/<id>/treemap              → TreeMapModel
/job/x/42/<id>/scatter              → ScatterModel
/job/x/42/<id>/hotspots             → HotspotsModel
/job/x/42/<id>/table                → FilesModel
/job/x/42/<id>/<file-hash>          → FileDetailsModel (neue UI) bzw. SourceViewModel (Legacy)
/job/x/42/<id>/log                  → LogModel
/job/x/42/<id>/help                 → HelpModel
```

- **Ein Model pro Seite.** Jedes Model implementiert `ModelObject` (bei Tabellen zusätzlich `DefaultAsyncTableContentProvider`) und hat eine `index.jelly` im gleichen Paketpfad unter `resources`.
- **Gemeinsame Getter in jedem Model:** `getId()`, `getObject()` (der `Run`, wird von `l:run-subpage` und `cov:help-link` benötigt), `getTab()` (liefert `new RunTab(owner)`) und `getFormatter()`.
- **Delegation statt Duplikation:** Die Models bekommen den `CoverageViewModel` als `parent` und delegieren Lookups dorthin (z. B. `FilesModel#getTableModel`).
- **`getDynamic` im Kind-Model:** Seiten, deren relative Links weiter in den Baum führen (z. B. Datei-Links in der Files-Tabelle), müssen unbekannte Links an `parent.getDynamic(link)` weitergeben. Sonst landen Links bei URLs mit abschließendem `/` im Nichts.
- **Umschalten neue/alte UI** über das Experimental Flag `NewBuildPageUserExperimentalFlag`, gekapselt in `CoverageViewModel.UsePropertyFacade#isRunTabEnabled()`. Die Fassade lässt sich in Tests mocken. Die URL-Konstanten der neuen Seiten werden nur aufgelöst, wenn das Flag aktiv ist.
- **Redirects** verwenden `HttpResponses.redirectViaContextPath(owner.getUrl() + id + "/overview")`. Relative Redirects lösen sich unterschiedlich auf, je nachdem ob die Request-URL mit `/` endet.

### 2.2 Seitenrahmen (Jelly)

Jede Seite hat dasselbe Gerüst:

```xml
<l:run-subpage>
  <st:adjunct includes="…RunTab.styles"/>
  <l:app-bar title="${it.displayName}">
    <!-- seitenbezogene Bedienelemente: Selects, Checkboxen, Zahlenfelder -->
  </l:app-bar>
  <div class="coverage-body">
    <cov:sidepanel id="${it.id}"/>          <!-- gemeinsames Tag, siehe resources/coverage/sidepanel.jelly -->
    <div class="content"> … <l:card> … </l:card> … </div>
  </div>
  <st:bind var="proxy" value="${it}"/>       <!-- nur bei Seiten mit @JavaScriptMethod -->
  <st:adjunct includes="…TrendModel.trend-model"/>
</l:run-subpage>
```

- **Sidebar als Taglib** (`/coverage` → `sidepanel.jelly`) mit dem optionalen Attribut `active`. Damit bleibt bei Unterseiten (z. B. der Datei-Detailseite unter „Files“) der richtige Eintrag markiert.
- **Workaround `active-tab.js`:** Jenkins Core markiert Tab und Sidebar-Task nur, wenn die URL exakt *endet* (`Functions.hyperlinkMatchesCurrentPage`). Das Skript markiert Tab und Task bei Unterseiten clientseitig. Es kann entfallen, sobald Core auch Unterseiten erkennt. Für Warnings 1:1 übernehmen und nur `TAB_URL` anpassen.
- **Bedienelemente in der App-Bar**, nicht im Inhalt. Es werden Jenkins-Komponenten verwendet (`jenkins-select`, `jenkins-checkbox`, `jenkins-input`), jeweils mit `tooltip`.
- **Hilfe-Link** (`cov:help-link anchor="…"`) als kleines Icon an Karten, das auf `…/help#anchor` verlinkt. Die Help-Seite wird **aus dem Model erzeugt** (Baselines, Aggregationen, Metriken), damit die Erklärungen immer zur tatsächlichen Berechnung passen.

### 2.3 Widget auf der Build-Übersicht

`RunTab#getWidget()` erzeugt die Karte auf der Build-Seite (`RunTab/widget.jelly`):
- **Ein einziges Ergebnis:** Die Kennzahlen werden direkt als farbige Badges gezeigt (dieselben Metriken wie die Gauge-Karten der Overview), weil ein Klick eindeutig ist.
- **Mehrere Ergebnisse:** eine Pill pro Ergebnis, jeweils einzeln anklickbar.

Warnings hat bereits ein `Widget`. Dort lässt sich prüfen, ob die Logik „ein Ergebnis → Kennzahlen, mehrere → Pills“ übertragbar ist (z. B. bei einem Tool die Anzahl pro Schweregrad plus neue Warnungen).

### 2.4 Badge am Tab

- Coverage: Anzahl **fehlgeschlagener Quality Gates** als `Badge.Severity.WARNING`, sonst kein Badge.
- Warnings (heute): Gesamtzahl der Warnungen. Hier ist zu entscheiden, ob neue Warnungen oder fehlgeschlagene Quality Gates aussagekräftiger sind.

### 2.5 Clientseitiges JavaScript

- **Ein Skript pro Seite** im Model-Paket (`trend-model.js`, `scatter-model.js` …), eingebunden per `st:adjunct`. Gemeinsame Helfer liegen eine Ebene höher (`color-scale.js`).
- **Daten über `@JavaScriptMethod`** am Model (`proxy.getTrendChart(json)`), Charts mit `echarts` (Adjunct `io.jenkins.plugins.echarts`). Wenn die Chart-Optionen nicht aus Java kommen (Scatter), liefert Java nur die Rohdaten. Den Chart baut dann das JS.
- **Einstellungen im `localStorage`**, der Schlüssel enthält immer die Report-ID (`data-report-id="${it.id}"`). So behält jedes Ergebnis seine eigenen Einstellungen. Bei Warnings mit vielen Tools ist das besonders wichtig.
- **Charts füllen den Viewport** (`fitChartToViewport` mit Footer-Höhe und Mindesthöhe) und reagieren auf Resize mit Debounce.
- **Tabellen** über das data-tables-API-Plugin (`DefaultAsyncTableContentProvider#getTableModel(id)`, `<dt:table>`). Der Wechsel zwischen mehreren Tabellen läuft per Select-Box clientseitig, nicht über Bootstrap-Tabs.

### 2.6 CSS

- **Eine `styles.css`** unter `RunTab/`, BEM-artig mit Plugin-Präfix (`coverage-dashboard__metric-card`, `coverage-ring__arc`). Für Warnings bietet sich `wng-` an, das dort schon verwendet wird.
- **Gestaltung nur mit Jenkins-Variablen**, damit Dark Mode und Themes funktionieren.
- **Lehre aus #793** (siehe `claude/source-view-css-performance-793.md`): keine relationalen Selektoren der Form `:has()` + `~` + `:hover` über große Tabellen. Das ist O(n²) bei jeder Hover-Änderung. Block-Logik gehört in ein kleines JS-Modul mit einmaligem Scan.

### 2.7 Build-History der Job-Seite (Branch `run-column`)

`CoverageHistoryPageEntryDecorator` und `CoverageHistorySummary` zeigen in der neuen Build-History eine Pill, analog zu JUnit-Plugin PR #1271:
- Schritt 1: nur für die Default-ID `coverage`, Line- und Branch-Coverage inklusive Delta.
- Pill grün bzw. rot, wenn die Deltas insgesamt steigen bzw. fallen, sonst grau. Ohne Coverage wird nichts angezeigt.
- Schritt 2: konfigurierbar.

Für Warnings bietet sich dasselbe an, z. B. „+3 / −5“ für neue und behobene Warnungen.

---

## 3. Seitenkatalog: Coverage → Vorschlag für Warnings

| Coverage-Seite | Inhalt bei Coverage | Entsprechung bei Warnings (Vorschlag) |
|---|---|---|
| **Overview** | Quality Gates (mit Hinweis je Gate), Gauge-Karten je Metrik und Baseline mit Delta-Badge, Tabelle „Further metrics“ inkl. Worst Values, Referenz-Build in der App-Bar | Quality Gates; Karten für Gesamt, neu, behoben und offen; Verteilung nach Schweregrad; „modified vs. unchanged“; Referenz-Build-Link. Das ersetzt die Karten aus `filled.jelly`. |
| **Trend** | Linien- oder Flächendiagramm, Metrikgruppe, Aggregation, Y-Achse ab 0, Anzahl Builds | Trends nach Schweregrad, Neu vs. Behoben, Tools und Health. Die vorhandenen Trend-Charts von Warnings (Job-Seite) lassen sich wiederverwenden. |
| **Treemap** | Hierarchie (Pakete, Dateien) nach Metrik eingefärbt, Schwellen grün/rot | Hierarchie Modul → Paket → Datei; Fläche = LOC oder Anzahl Warnungen, Farbe = Warnungsdichte oder höchster Schweregrad |
| **Scatter Plot** | zwei frei wählbare Metriken pro Datei | eher nicht übernehmen, außer in Kombination mit Forensics (Churn × Warnungen ist sinnvoll) |
| **Hotspots** | Dateien mit den meisten fehlenden Elementen, komplex und schlecht getestet, Histogramm | Dateien, Kategorien und Typen mit den meisten (neuen) Warnungen; Histogramm der Warnungen pro Datei |
| **Files** (`table`) | Tabellen für alle Dateien, geänderte Zeilen und indirekte Änderungen, Umschaltung per Select | **Issues**: eine Tabelle mit Umschaltung alle / neu / offen / behoben (heute getrennte Seiten `new`, `fixed`, `outstanding`) |
| — | — | **Eigenschaften**: die Property-Tabs von heute (Tools, Module, Pakete, Ordner, Dateien, Kategorien, Typen) als eine Seite mit Select statt Bootstrap-Tabs |
| — | — | **SCM**: Blames und Forensics (nur wenn sichtbar, `isBlameVisible`, `isForensicsVisible`) |
| **Datei-Details** | Metriken der Datei, Methodentabelle, Quelltext mit Hervorhebung | Detailseite pro Datei: Warnungen der Datei als Tabelle und darüber Quelltext mit Markern (heute `SourceDetail`) |
| **Log Messages** | Info- und Fehlermeldungen als eigene Seite | dasselbe (heute `info`-Link) |
| **Help** | aus dem Model erzeugte Erklärungen | Baselines (neu, offen, behoben), Schweregrade, Quality-Gate-Typen, Referenz-Build-Ermittlung |

---

## 4. Offene Designentscheidungen für Warnings

1. **Tool-Navigation.** Bei Coverage gibt es meist nur 1–2 Ergebnisse, bei Warnings oft 5–15 Tools. Die Sidebar kann nicht gleichzeitig Tools und Ansichten zeigen. Mögliche Varianten:
   - **A:** Sidebar zeigt die Ansichten des aktuellen Tools, gewechselt wird das Tool per Select in der App-Bar (URL `/<tool-id>/<ansicht>` bleibt).
   - **B:** Sidebar zeigt die Tools wie heute, die Ansichten liegen als Reiter oder Select im Inhalt.
   - **C:** Eine zusätzliche Übersicht über alle Tools unter `/warnings/` (Tabelle Tool × Schweregrad × neu/behoben) statt Redirect auf das erste Tool, kombiniert mit A.
2. **Baselines.** Coverage kennt Project, Modified Lines, Modified Files und Indirect Changes. Bei Warnings sind es „neu / offen / behoben“ und optional „in geänderten Zeilen“ (`hasIssuesInModifiedCode`). Wo diese Achse auftaucht, muss überall einheitlich sein.
3. **Badge am Tab:** Gesamtzahl, neue Warnungen oder fehlgeschlagene Quality Gates?
4. **Aggregierte Ergebnisse** (`aggregatingResults`, ID `analysis`): Hier ist der Tool-Filter innerhalb eines Ergebnisses die eigentliche Achse. Der Abschnitt „Eigenschaften“ deckt das teilweise ab.
5. **Legacy-UI:** wie bei Coverage nicht mehr anfassen. `IssuesDetail/index.jelly` sollte dann nur noch zwischen einer neuen `run-tab.jelly` und der alten Seite umschalten.
6. **API-Stabilität:** Anders als das Coverage Plugin ist warnings-ng **kein Leaf-Plugin**. Andere Plugins bauen auf `analysis-core`-Klassen auf (z. B. eigene Tools, `IssuesDetail`, Columns, Dashboard-Portlets). Öffentliche Klassen und Konstruktoren daher nicht leichtfertig ändern. Neue Models sollten package-private bleiben, wo es geht. Bei Coverage war das egal, hier vorher prüfen.

---

## 5. Erkenntnisse aus dem Coverage-Umbau

- **Schrittweise vorgehen**, jede Seite einzeln umsetzen und im Browser prüfen (neues Build-UI-Flag aktivieren).
- **Datei-Details als eigene Seite**, keine Master-Detail-Ansicht (Tabelle und Quelltext nebeneinander) im neuen Tab.
- **Redirects absolut** über den Context Path (siehe 2.1).
- **Kind-Models leiten `getDynamic` weiter**, sonst brechen relative Links bei `/` am URL-Ende.
- **Tab- und Sidebar-Markierung bei Unterseiten** braucht vorerst `active-tab.js` (siehe 2.2).
- **Einstellungen pro Report-ID speichern.**
- **CSS-Performance:** keine `:has()`/`~`-Kaskaden über große Tabellen (siehe 2.6).
- **Help-Seite aus dem Model erzeugen**, statt Texte doppelt zu pflegen.
- **Inspiration:** SonarQube (Overview mit Quality Gate oben, Hotspots, Treemap).
- **Einheitliche Schriftgrößen** über alle Run-Tab-Seiten, das war ein eigener Commit (`ad278d2`). Lieber gleich von Anfang an zentral in `styles.css` festlegen.

---

## 6. Konventionen für den Code

### 6.1 Kein `null`

- **Kein `null` zurückgeben oder als Parameter übergeben.** Stattdessen das **NullObject-Pattern** verwenden (ggf. mit Factory). Warnings hat dafür schon Beispiele: `NullAnalysisHistory`, `NullFileNameFilter`. Alternativ `Optional` zurückgeben oder den Eintrag weglassen.
- **Keine null-Prüfungen** als Ersatz für ein fehlendes NullObject.
- **Ausnahme Jenkins-API:** Einige Core-Verträge verlangen `null` (z. B. `Action#getIconFileName()` zum Ausblenden, `Tab#getBadge()` für „kein Badge“, Stapler `getDynamic` für „nicht gefunden“, `doIndex` nach `sendError`). Dort mit `@CheckForNull` annotieren und auf diese Stellen beschränken.
- **In Jelly** werden Attribute mit `null`-Wert weggelassen (z. B. `data-active-task="${attrs.active}"`). Das ist Absicht und darf so bleiben.

### 6.2 Tests

- **Keine Instanzfelder in Testklassen.** Stattdessen lokale Variablen, Methodenparameter (`@TempDir`) und Hilfsmethoden. `static final`-Konstanten sind erlaubt.
- **Keine AssertJ-BDD-Assertions (`then`).** Statt `org.assertj.core.api.Assertions` die **generierten Assertions** des jeweiligen Projekts verwenden, auch für `assertThatThrownBy` usw.:
  - coverage-model: `edu.hm.hafner.coverage.assertions.Assertions`
  - analysis-model: `edu.hm.hafner.analysis.assertions.Assertions`
  - warnings-ng: `io.jenkins.plugins.analysis.core.assertions.Assertions`. Generiert für `core.columns`, `core.scm`, `core.model`, `core.util` und `warnings` (siehe `assertj-assertions-generator-maven-plugin` in `plugin/pom.xml`). Neue Model-Klassen in einem dieser Pakete bekommen damit automatisch eigene Assertions.
- **Bei Methoden ohne Parameter die generierte Assertion verwenden**, z. B. `assertThat(counters).hasNoLines()` statt `assertThat(counters.getLines()).isEmpty()`.
- **Keine Reflection in Tests.** Wenn es nicht anders geht, einen package-private Konstruktor mit `@VisibleForTesting` anlegen, der die Felder setzt.
- **UI-Logik testbar machen**, indem Abhängigkeiten in Fassaden gekapselt werden (Muster `UsePropertyFacade`) und diese mit Mockito ersetzt werden. Für neue Seiten zusätzlich die UI-Tests im Modul `ui-tests` ergänzen.

### 6.3 Dokumentation und Sonstiges

- **JavaDoc:** Jeder Methodenparameter bekommt `@param`, dazu `@return` und `@throws`, wo es passt. Das gilt **auch für Testmethoden**.
- **Model-Klassen** bekommen einen Klassen-Javadoc, der beschreibt, welche Frage die Seite beantwortet, welche Bedienelemente es clientseitig gibt (mit Name des JS) und an wen delegiert wird. Vorbild sind die Javadocs von `TrendModel` und `HotspotsModel`.
- **Serialisierbare Klassen** (z. B. neue Klassen in analysis-model, die im Build gespeichert werden) aus Sicherheitsgründen in `src/main/resources/META-INF/hudson.remoting.ClassFilter` eintragen. Bei jeder neuen serialisierbaren Klasse daran denken.
- **Commit-Messages:** keine Zeile länger als 72 Zeichen.
- **Jelly:** `escape-by-default='true'`, Texte in `.properties` (`${%key}`), Tooltips an allen Bedienelementen.
- **PMD-, Checkstyle- und SpotBugs-Unterdrückungen** gezielt per `@SuppressWarnings` mit Begründung, wie in `CoverageViewModel#getDynamic`.

---

## 7. Vorgeschlagene Reihenfolge

1. Gerüst: `run-tab.jelly` und `legacy.jelly` in `IssuesDetail` trennen, `wng:sidepanel`-Tag, `active-tab.js`, `styles.css`, Redirects. Vorher Entscheidung zu 4.1 treffen.
2. Overview (ersetzt die Karten aus `filled.jelly`).
3. Issues-Tabelle mit Umschaltung neu / offen / behoben.
4. Datei-Details mit Quelltext.
5. Trend.
6. Eigenschaften, SCM, Log, Help.
7. Hotspots und Treemap.
8. Widget, Badge und Build-History-Pill.

## Referenzen im Coverage Plugin

- Java: `plugin/src/main/java/io/jenkins/plugins/coverage/metrics/steps/` – `RunTab`, `RunTabFactory`, `CoverageViewModel` (`doIndex`, `getDynamic`, `UsePropertyFacade`), `OverviewModel`, `TrendModel`, `TreeMapModel`, `ScatterModel`, `HotspotsModel`, `FilesModel`, `FileDetailsModel`, `LogModel`, `HelpModel`
- Jelly, JS und CSS: `plugin/src/main/resources/io/jenkins/plugins/coverage/metrics/steps/<Model>/index.jelly`, `…/RunTab/{styles.css,widget.jelly,active-tab.js,dashboard-metrics.js,widget-badges.js}`, `…/coverage/{sidepanel,help-link}.jelly`, `…/metrics/color-scale.js`
- Tests: `RunTabTest`, `OverviewModelTest`, `HotspotsModelTest`, `ScatterModelTest`, `FileDetailsModelTest`, `HelpModelTest`, `WidgetTest`
- Build-History: Branch `run-column` – `CoverageHistoryPageEntryDecorator`, `CoverageHistorySummary`
