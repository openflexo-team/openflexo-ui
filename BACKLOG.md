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
