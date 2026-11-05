# openflexo-ui — Backlog

Features and evolutions of the user interface of the OpenFlexo platform (flexo-ui, the technology adapter controllers, the editors). Bugs go to `KNOWN_DEFECTS.md`.

Identifiers are stable and never reused: `UI-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### UI-F-1 — Editable colouring themes for the FML editor  ·  `IN PROGRESS`

**Problem.** The FML editor loaded one hard-coded RSyntaxTextArea theme and overrode the annotation colour with a constant. The user could not change any colour.

**Done.**
- A theme gives a colour, bold and italic to each token type of `FMLTokenMaker` (`FMLTokenKind`), plus the editor background and the current-line highlight. It is a PAMELA entity (`FMLEditorTheme`, `FMLTokenStyle`) in `fml-technologyadapter-ui`, outside the preferences model.
- One file per theme, in the RSyntaxTextArea format (`theme.dtd`, readable by `Theme.load`), in `<application data directory>/FMLThemes`. The shipped themes `default` (with the gold annotation colour), `eclipse` and `dark` are bundled in `FMLThemes/`; they are read-only, cannot be deleted or renamed, and can be duplicated.
- `FMLPreferences` (the preferences of the FML technology adapter) keeps only the name of the active theme, and offers the page "FML editor" (`Fib/Prefs/FMLPreferences.fib`), at the top of the preferences tree (a technology adapter controller contributes such a page with `getApplicationPreferencesClass()`): list of themes (new, duplicate, delete, use, import, export; a check mark shows the used theme, which the open editors take when the preferences are applied: `PreferencesContainer.applyPreferences()`, called by the Apply button), details of the selected theme with the colour of each token in the table, and a live preview.
- `FMLEditor` follows the active theme, live, and no longer holds a colour of its own.

**Left.**
- Sharing the themes with the B editors of formod (`b-ta-ui`): out of scope for now.
- Checking the page in the running application.

**Acceptance.** Creating, duplicating, editing and deleting a theme works from the preferences; a change of the active theme or of its colours is seen at once in every open FML editor; the shipped themes cannot be altered.

---

### UI-F-2 — Faster startup and linear progress bars for long tasks  ·  `IN PROGRESS`

**Problem.** Long tasks (startup, module loading, project loading, resource center and technology activation, validation, FML creation actions) show progress bars that jump back, stop early or stay indeterminate:
- `Progress` only acts when the current thread is a `FlexoTaskThread`: the whole startup (23 s on `main`, before the Welcome) reports nothing, and the splash is a still image;
- steps are counted, not weighted, and `setExpectedProgressSteps` resets the current progress, so nested phases make the bar jump back;
- announced counts are wrong: a module announces 150 steps (`Module.getExpectedProgressLoadingSteps`) for about twenty reported ones.

**Measured** (Eclipse, Free Modelling Editor, JFR + log timestamps, 2026-11-05):
- before the Welcome: about 8 s building the GINA FIB editor (palettes, inspectors) that nobody asked for, about 4.7 s activating the FML technology (resource center scan);
- Welcome → module opened: 12.3 s in `LoadModuleTask`, 78 % of it in `TechnologyAdapterController.activate`, mostly the Diana inspectors built in advance by the Diagram technology adapter controller.

**Done.**
- The application FIB editor is built on first use (Tools menu, Alt+click on a component), no longer at startup: the Welcome appears after 15 s instead of 24 s.
- Progress linear in time, replayed from the previous execution (flexo-foundation, `ProgressHistory`): a `FlexoTask` that returns a `getProgressHistoryKey()` records its milestones (its `Progress.progress(name)` calls) with their times, in `<application data directory>/ProgressHistory/<key>.properties`. The next execution sets its bar, at each milestone reached again, at the fraction of the total duration it had the previous time, and moves it with time in between; the bar never goes back and stays below 99 % until the end. Without history, the bar tends towards 100 % along `getDefaultExpectedDuration()`. Tasks without a key keep their counted steps.
- Used by the loading of a module (one history per module; 220 milestones for the Free Modelling Editor) and by the loading of a project (one history per module and project directory, since the module decides what is loaded with the project). The task panel shows these bars with a percentage, refreshed every 100 ms.
- Loading a project no longer freezes the user interface in the Free Modelling Editor: its views loaded and validated the virtual models and instances of the project in the event dispatch thread (8 s for `TestNewFME5`). A module now loads, in the loading task and before the project is shown, what it will display (`FlexoModule.loadProjectContents`, called through the hook `ProjectLoader.loadProjectContents`; nothing by default, virtual models and instances for the Free Modelling Editor).
- A task can stay out of the task panel (`FlexoTask.isShownInTaskPanel()`): the selection of the default object of a module no longer shows a bar.
- File → Open, File → Recent projects and the "Open" button of a module welcome panel loaded the project in the event dispatch thread, without a task: they now go through a loading task (`ApplicationContext.openProject`), as the Welcome dialog does, and `LoadProjectTask` reports a project that cannot be opened.
- The splash screen shows the progress of the startup: a thin bar at its bottom and the name of the current step. The startup is a task that no task manager executes (`FlexoTask.begin()`/`end()`), bound to the main thread (`Progress.bindToCurrentThread`), replayed from the previous startup like the other timed tasks (history `Startup`), and ended when the splash screen closes, just before the Welcome. Its milestones are the initialization of each service (`FlexoServiceManager.registerService`) and the existing steps of resource centers and technology adapters.
- Measured on `TestNewFME5`: 9.1 s in the Free Modelling Editor (virtual models and instances preloaded and validated), 2.1 s in the Openflexo Modeller (nothing preloaded, nothing heavy left to the event dispatch thread).

**Left.**
- Work done on the event dispatch thread still reports nothing (`Progress` follows the task thread, or a task bound to the current thread).
- Other modules (Openflexo Modeller, the EA editor, formod): check what their views load in the event dispatch thread when a project is shown, and override `loadProjectContents` if needed.
- A project opened from the command line (`Flexo.main`) may start loading before its module is active: no preloading then, and a history without module.
- Default histories shipped with the application, for the first execution (not through `ResourceLocator.locateResource`, which prints a stack trace for every resource it does not find).
- The other long tasks (resource centers, technology activation, validation, FML creation actions).
- Build the Diana inspectors of the Diagram controller on first use.

**Acceptance.** Between the Welcome and the opened module, and while a project loads, the bar moves forward without going back, and in near-linear time from the second execution on.
