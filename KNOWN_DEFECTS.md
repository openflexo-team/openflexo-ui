# openflexo-ui — Known defects

Bugs of the user interface of the OpenFlexo platform (flexo-ui, the technology adapter controllers, the editors) that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work around it. Features and evolutions go to `BACKLOG.md`, not here.

Identifiers are stable and never reused: `UI-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

Fixed defects are moved to [`FIXED_DEFECTS.md`](./FIXED_DEFECTS.md) once their status is `DONE`.

---

### UI-D-2 — A concept-typed project nature has no icon on the paths that do not go through the module controller  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-ui#4](https://github.com/openflexo-team/openflexo-ui/issues/4)

**Symptom.** `Sorry, no icon defined for FormoseProjectNature[…]` (`FlexoController.statelessIconForObject`), logged when the inspector of the nature is built. The module controller of Formose does give the icon (`FMSController.iconForObject`).

**Read, not reproduced in isolation.** `statelessIconForObject(Object)` is static and cannot see the overrides of the controller of the module; the instance method `iconForObject` calls it. The caller that uses the static one for the nature was not identified.

**Fixed (2026-10-09).** `ginaTest()`, `flexoTestResources()` and `flexoFoundationTest()` are `testImplementation` in `fml-technologyadapter-ui/build.gradle`. The three production sources only quote the path `src/test/resources/TestResourceCenter` in `main()` demonstrations: none imports a class of these libraries (checked with `grep` on the imports, then by compiling and running the tests).
- Measured on the start of formod-app, before the application window: lines naming the test center 13 -> 0, `makeDelegateRC` 1 -> 0, `No MigrationVM.fml` / `No InsideVM.fml` 2 -> 0, resource centers loaded from the classpath 3 -> 1 (`formod-rc` only). `flexo-test-resources` and `gina-test` are gone from the runtime classpath of `formod-app` (`./gradlew :formod:formod-app:dependencies --configuration runtimeClasspath`).
- Tests of the module unchanged: `test` 191 (0 failure, 67 skipped); `uiTest` 68 with the same 6 failures before and after (`TestFMLEditor23`, five of `TestMultiProcessChallengeFMLEditor`, not related). The tests of the consumers pass: `fml-rt-technologyadapter-ui` 15, `fml-gina-extension` 27, `owl-ta-ui` 39, `xlsx-ta-ui` 14, `b-ta-ui` 19, `fme-module` 33, `gina-ta-ui` 8, `om-module` 3, `diagram-ta-ui` 56, `integration-tests` 37, formod 32 + 17 + 56. Their `uiTest` tasks were not run.
- The `Could not find org.openflexo.technologyadapter.xml.XMLModelSlot` warning is still logged: it comes from `openflexo-integration-tests/digital-twin-demo`, a resource center registered in the user preferences (`Flexo.prefs`) that needs the XML adapter, which formod-app does not have. Not this defect.

**Same pattern in three other modules, fixed the same day (2026-10-09/10).** `openflexo-diagram/diagram-ta` (`implementation flexoFoundationTest()`), `openflexo-utils/flexo-p2pp` (`implementation testUtils()`) and `openflexo-utils/flexo-utils` (`implementation testUtils()`, the one that brought JUnit and `testutils` to every application through `pamela-core`, `flexolocalization` and `flexo-foundation`): each is now `testImplementation`. None of their production sources imports a test class (`grep` on `import org.openflexo.test|org.junit|org.openflexo.foundation.test`). Tests before/after: `diagram-ta` 24 (2 skipped), `flexo-p2pp` 5, `flexo-utils` 44 (10 skipped), identical. Runtime classpath of `formod-app`: `flexo-test-resources`, `gina-test`, `flexo-foundation-test`, `testutils` and JUnit were all in it, none is any more.

### UI-D-4 — A project imported into another one is loaded as a root project  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-ui#5](https://github.com/openflexo-team/openflexo-ui/issues/5)

**Symptom.** File → Import project schedules `makeLoadProjectTask(directory, true)`, but `LoadProjectTask.performTask` calls `ProjectLoader.loadProject(projectDirectory)`, which is `loadProject(projectDirectory, false)`: the `asImportedProject` flag of the task is never passed on.

**Verified.** By reading the code only (2026-11-05), not reproduced in the application.

**Expected.** `loadProject(projectDirectory, asImportedProject)`: an imported project is not added to the root projects nor to the recent projects.

### UI-D-6 — `TestMultiProcessChallengeFMLEditor` loads a VirtualModel that no longer exists  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-ui#6](https://github.com/openflexo-team/openflexo-ui/issues/6)

**Symptom.** Its five UI tests fail (`uiTest` of fml-technologyadapter-ui): `loadFMLResource` finds no VirtualModel
`http://www.openflexo.org/test/MultiProcessChallengeMetaModel.fml`, the four next steps fail with a `NullPointerException`.

**Verified (2026-11-08).** No model of the workspace has this URI. The multi-process challenge now lives in
openflexo-integration-tests (`http://openflexo.org/multi-process-challenge/FML/MetaModel.fml`), which fml-technologyadapter-ui does
not depend on. Unrelated to CORE-F-8: without that model the test cannot pass, whatever the platform does.

**Options.** Repoint it to a model of flexo-test-resources of the same size, or delete it.

### UI-D-7 — A saved window position outside the screens is not corrected (negative `x`)  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-ui#3](https://github.com/openflexo-team/openflexo-ui/issues/3)

**Symptom.** The preference `BoundsForFrame_OMFrame` saved with `x=-1683` (a second screen that is no longer there) opens the window outside of the visible area.

**Verified (2026-11-17, reading the code).** `FlexoFrame`'s constructor corrects the bounds only when there is exactly ONE screen and only when
`screen.width <= bounds.x` or `screen.height <= bounds.y`: a negative `x` is never corrected. `WindowBoundsSaver` checks that the location is inside
SOME screen (and resets the location otherwise), which is the right test.

**Proposed fix.** Apply the `WindowBoundsSaver` test (the rectangle must intersect a screen, otherwise default location) to `FlexoFrame`, whatever the number of screens.

**Original report** by `ChahrazedBoudj`, 2024-02-15 (issue gina#3):

> ```xml
> <CoreProperty id="106" name="BoundsForFrame_OMFrame" value="[x=-1683,y=41,width=1440,height=924]" userID="CHA" flexoID="-1" />
> <CoreProperty id="107" name="showPaletteInOMFrame" userID="CHA" flexoID="-1" />
> <CoreProperty id="108" name="BoundsForFrame_JFIBInspector"  value="[x=800,y=400,width=400,height=400]" userID="CHA" flexoID="-1" />
> ```
> 
> 
> "BoundsForFrame_OMFrame" has bad values. 
> I suggest to add a condition on the positioning values (do not exceed the screen size).
> These values worked for me : 
> 
> ```xml
> <CoreProperty id="106" name="BoundsForFrame_OMFrame" value="[x=78,y=78,width=1440,height=810]" userID="CHA" flexoID="-1" />
> ```

### UI-D-8 — Double-click in the FML textual editor selects the whole FML element and loses the selection  ·  `TODO`

**GitHub:** [openflexo-team/openflexo-ui#1](https://github.com/openflexo-team/openflexo-ui/issues/1)

**Problem.** If part of an FML entity is selected (for example the type of a variable), a double-click selects the whole element and the initial selection is lost; this is
annoying in large FML files.

**Verified (2026-11-17, reading the code).** `FMLEditor` still carries the comment "Double-click generates a bad user experience" at the mouse listener; the
element-focus feature is bound to the triple-click.

**Original report** by `sylvain-openflexo`, 2025-01-20 (issue openflexo-ui#1):

> Double-click user experience is somehow very annoying. If you select a part of a FML entity (for example a type of a variable), it select the whole element and you loose your initial selection which was only a part of that entity. This can be very annoying on large FML files...
