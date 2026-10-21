# openflexo-ui — Known defects

Bugs of the user interface of the OpenFlexo platform (flexo-ui, the technology adapter controllers, the editors) that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work around it. Features and evolutions go to `BACKLOG.md`, not here.

Identifiers are stable and never reused: `UI-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### UI-D-1 — The user interface of the FML technology adapter ships the test resources and test libraries to every application  ·  `DONE`

**Symptom.** Every application that includes `fml-technologyadapter-ui` — formod-app, the Modeller — loads the `TestResourceCenter` of `flexo-test-resources` as one of its resource centers, and logs warnings about it at the start: `makeDelegateRC not implemented for [ClasspathResourceLocatorImpl]jar:…/flexo-test-resources-…jar!/TestResourceCenter/PRJ/TestSingleInheritance.prj`, `No MigrationVM.fml in …`, `No InsideVM.fml in …`, and `Could not find org.openflexo.technologyadapter.xml.XMLModelSlot` (a fixture of that center needs a technology the application does not have).

**Verified (2026-10-09).** `./gradlew :formod:formod-app:dependencies --configuration runtimeClasspath` shows `flexo-test-resources` reached through `fml-technologyadapter-ui` <- `b-ta-ui` <- `formod-module`. `openflexo-ui/fml-technologyadapter-ui/build.gradle` declares `implementation ginaTest()`, `implementation flexoTestResources()` and `implementation flexoFoundationTest()` as production dependencies (the `testImplementation` ones are commented out above them). The log also carries 14 lines naming `TestResourceCenter`.

**Not verified.** Whether the three production sources `FIBCompilationUnitBrowser`, `FIBVirtualModelSelector` and `FIBFlexoConceptSelector` really need a class of these libraries (they mention `TestResource…` or `org.openflexo.foundation.test`; read only by `grep`), or only their `main` methods do.

**Impact.** The test center, and JUnit, are in every distribution; its fixtures are visible in the resource center browsers of the user, and compete with real ones for URIs (a test fixture already named like a user model masks it silently: see the workspace `CLAUDE.md`).

### UI-D-2 — A concept-typed project nature has no icon on the paths that do not go through the module controller  ·  `TODO`

**Symptom.** `Sorry, no icon defined for FormoseProjectNature[…]` (`FlexoController.statelessIconForObject`), logged when the inspector of the nature is built. The module controller of Formose does give the icon (`FMSController.iconForObject`).

**Read, not reproduced in isolation.** `statelessIconForObject(Object)` is static and cannot see the overrides of the controller of the module; the instance method `iconForObject` calls it. The caller that uses the static one for the nature was not identified.

**Fixed (2026-10-09).** `ginaTest()`, `flexoTestResources()` and `flexoFoundationTest()` are `testImplementation` in `fml-technologyadapter-ui/build.gradle`. The three production sources only quote the path `src/test/resources/TestResourceCenter` in `main()` demonstrations: none imports a class of these libraries (checked with `grep` on the imports, then by compiling and running the tests).
- Measured on the start of formod-app, before the application window: lines naming the test center 13 -> 0, `makeDelegateRC` 1 -> 0, `No MigrationVM.fml` / `No InsideVM.fml` 2 -> 0, resource centers loaded from the classpath 3 -> 1 (`formod-rc` only). `flexo-test-resources` and `gina-test` are gone from the runtime classpath of `formod-app` (`./gradlew :formod:formod-app:dependencies --configuration runtimeClasspath`).
- Tests of the module unchanged: `test` 191 (0 failure, 67 skipped); `uiTest` 68 with the same 6 failures before and after (`TestFMLEditor23`, five of `TestMultiProcessChallengeFMLEditor`, not related). The tests of the consumers pass: `fml-rt-technologyadapter-ui` 15, `fml-gina-extension` 27, `owl-ta-ui` 39, `xlsx-ta-ui` 14, `b-ta-ui` 19, `fme-module` 33, `gina-ta-ui` 8, `om-module` 3, `diagram-ta-ui` 56, `integration-tests` 37, formod 32 + 17 + 56. Their `uiTest` tasks were not run.
- The `Could not find org.openflexo.technologyadapter.xml.XMLModelSlot` warning is still logged: it comes from `openflexo-integration-tests/digital-twin-demo`, a resource center registered in the user preferences (`Flexo.prefs`) that needs the XML adapter, which formod-app does not have. Not this defect.

**Same pattern in three other modules, fixed the same day (2026-10-09/10).** `openflexo-diagram/diagram-ta` (`implementation flexoFoundationTest()`), `openflexo-utils/flexo-p2pp` (`implementation testUtils()`) and `openflexo-utils/flexo-utils` (`implementation testUtils()`, the one that brought JUnit and `testutils` to every application through `pamela-core`, `flexolocalization` and `flexo-foundation`): each is now `testImplementation`. None of their production sources imports a test class (`grep` on `import org.openflexo.test|org.junit|org.openflexo.foundation.test`). Tests before/after: `diagram-ta` 24 (2 skipped), `flexo-p2pp` 5, `flexo-utils` 44 (10 skipped), identical. Runtime classpath of `formod-app`: `flexo-test-resources`, `gina-test`, `flexo-foundation-test`, `testutils` and JUnit were all in it, none is any more.

### UI-D-3 — On a Java runtime without its class library files, the application dies at startup behind its splash screen  ·  `DONE`

**Symptom.** The splash screen stays forever. The log ends with `[ERROR]: Cannot locate JRE jar in …/jre` then `Exception in thread "main" java.lang.IllegalArgumentException: info cannot be null` at `JarManager.addClassFileSource(JarManager.java:155)` ← `JarManager.addCurrentJreClassFileSource` ← `FMLTechnologyAdapterController.activate(FMLTechnologyAdapterController.java:214)` ← `DefaultTechnologyAdapterControllerService.initialize` ← `ApplicationContext.<init>` ← `Flexo.main`.

**Verified (2026-10-18).** formod-app launched by `./gradlew :formod:formod-app:run` from openflexo-dev, on the JRE that jenv selects for the workspace (`~/.jenv/versions/17` → the Eclipse JustJ JRE in `~/.p2/pool`, a JRE without `jmods`): Gradle runs the application on the JVM of its daemon. `LibraryInfo.getMainJreJarInfo()` answers `null` there, and `addClassFileSource(null)` throws. `activate()` only catches `IOException`, so the exception kills the main thread while the splash screen is up. The same launch with `JAVA_HOME` on a full JDK 17 (Homebrew 17.0.16) starts normally. Independent of the PAMELA proxy backend.

**Work-around.** Run the application on a JDK (`JAVA_HOME=<a JDK 17> ./gradlew …:run`), or point jenv's `17` to a JDK.

**Fixed (2026-10-21).** The cause was deeper than the missing JRE of jenv: a "modern" Java was recognized by its `jmods` directory, which JREs do not have and which JDK builds since Java 24 may not have either (the Temurin 25 of the workspace: same symptom on a full JDK). The `jmods` directory was also read as a directory of class files, but it holds `.jmod` archives, so the classes of the JDK were never offered on Java 9+. `ModulesLibraryInfo` now reads the runtime image through the `jrt:/` file system, found by the presence of `lib/modules`, and `addCurrentJreClassFileSource()` no longer throws when no class library is found (it warns). Verified: formod and Flexo Maintainer start on JDK 25. The code completion of JDK types was not checked by hand.

**Original proposal.** In `activate()`, skip the JRE class-file source (with a warning) when `getMainJreJarInfo()` is null, so that only the completion of Java types in the FML editor is degraded; more generally, an exception during the initialization of a service should not leave the splash screen up with a dead main thread.
