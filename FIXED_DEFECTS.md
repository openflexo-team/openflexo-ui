# openflexo-ui — Fixed defects

Defects of openflexo-ui that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

### UI-D-1 — The user interface of the FML technology adapter ships the test resources and test libraries to every application  ·  `DONE`

**Symptom.** Every application that includes `fml-technologyadapter-ui` — formod-app, the Modeller — loads the `TestResourceCenter` of `flexo-test-resources` as one of its resource centers, and logs warnings about it at the start: `makeDelegateRC not implemented for [ClasspathResourceLocatorImpl]jar:…/flexo-test-resources-…jar!/TestResourceCenter/PRJ/TestSingleInheritance.prj`, `No MigrationVM.fml in …`, `No InsideVM.fml in …`, and `Could not find org.openflexo.technologyadapter.xml.XMLModelSlot` (a fixture of that center needs a technology the application does not have).

**Verified (2026-10-09).** `./gradlew :formod:formod-app:dependencies --configuration runtimeClasspath` shows `flexo-test-resources` reached through `fml-technologyadapter-ui` <- `b-ta-ui` <- `formod-module`. `openflexo-ui/fml-technologyadapter-ui/build.gradle` declares `implementation ginaTest()`, `implementation flexoTestResources()` and `implementation flexoFoundationTest()` as production dependencies (the `testImplementation` ones are commented out above them). The log also carries 14 lines naming `TestResourceCenter`.

**Not verified.** Whether the three production sources `FIBCompilationUnitBrowser`, `FIBVirtualModelSelector` and `FIBFlexoConceptSelector` really need a class of these libraries (they mention `TestResource…` or `org.openflexo.foundation.test`; read only by `grep`), or only their `main` methods do.

**Impact.** The test center, and JUnit, are in every distribution; its fixtures are visible in the resource center browsers of the user, and compete with real ones for URIs (a test fixture already named like a user model masks it silently: see the workspace `CLAUDE.md`).

### UI-D-3 — On a Java runtime without its class library files, the application dies at startup behind its splash screen  ·  `DONE`

**Symptom.** The splash screen stays forever. The log ends with `[ERROR]: Cannot locate JRE jar in …/jre` then `Exception in thread "main" java.lang.IllegalArgumentException: info cannot be null` at `JarManager.addClassFileSource(JarManager.java:155)` ← `JarManager.addCurrentJreClassFileSource` ← `FMLTechnologyAdapterController.activate(FMLTechnologyAdapterController.java:214)` ← `DefaultTechnologyAdapterControllerService.initialize` ← `ApplicationContext.<init>` ← `Flexo.main`.

**Verified (2026-10-18).** formod-app launched by `./gradlew :formod:formod-app:run` from openflexo-dev, on the JRE that jenv selects for the workspace (`~/.jenv/versions/17` → the Eclipse JustJ JRE in `~/.p2/pool`, a JRE without `jmods`): Gradle runs the application on the JVM of its daemon. `LibraryInfo.getMainJreJarInfo()` answers `null` there, and `addClassFileSource(null)` throws. `activate()` only catches `IOException`, so the exception kills the main thread while the splash screen is up. The same launch with `JAVA_HOME` on a full JDK 17 (Homebrew 17.0.16) starts normally. Independent of the PAMELA proxy backend.

**Work-around.** Run the application on a JDK (`JAVA_HOME=<a JDK 17> ./gradlew …:run`), or point jenv's `17` to a JDK.

**Fixed (2026-10-21).** The cause was deeper than the missing JRE of jenv: a "modern" Java was recognized by its `jmods` directory, which JREs do not have and which JDK builds since Java 24 may not have either (the Temurin 25 of the workspace: same symptom on a full JDK). The `jmods` directory was also read as a directory of class files, but it holds `.jmod` archives, so the classes of the JDK were never offered on Java 9+. `ModulesLibraryInfo` now reads the runtime image through the `jrt:/` file system, found by the presence of `lib/modules`, and `addCurrentJreClassFileSource()` no longer throws when no class library is found (it warns). Verified: formod and Flexo Maintainer start on JDK 25. The code completion of JDK types was not checked by hand.

**Original proposal.** In `activate()`, skip the JRE class-file source (with a warning) when `getMainJreJarInfo()` is null, so that only the completion of Java types in the FML editor is degraded; more generally, an exception during the initialization of a service should not leave the splash screen up with a dead main thread.

### UI-D-5 — Loading a module initialized the inspectors and actions of its technologies twice  ·  `DONE`

**Symptom.** Loading the Free Modelling Editor took 15.8 s, and 47 inspectors were loaded twice (`CreateDiagram` four times).

**Cause.** A module that activates technologies in its `initModule()` reaches `TechnologyAdapterController.activate(module)` twice: through `TechnologyAdapterController.activate()`, which goes over the loaded modules (the module being loaded is already one of them), then through the `ModuleLoaded` notification. Each call initialized the inspectors, the actions and the advanced actions again.

**Fix.** Inspectors and actions are initialized once per module controller; activable plugins are still checked at each call. Loading the Free Modelling Editor now takes 13.4 s (measured under Eclipse, 2026-11-05), each inspector being loaded once.
