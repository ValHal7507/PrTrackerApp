Read CONTEXT.md in full. Then read every file in the project source tree to fully understand the current architecture, data flow, naming conventions, and how all the pieces connect together — the data classes, StorageManager, PRViewModel, all screens, all components, navigation, and WorkManager workers. Only after you have a complete understanding of the entire codebase, update CONTEXT.md with everything that was added or changed this session. Your only job is to ADD and UPDATE — never delete, never remove, never overwrite existing content that is still accurate. Follow these rules:

1. If a new file was created, ADD it to the project structure tree in section 3 without touching the existing entries.
2. If a data class was modified, UPDATE only the changed field(s) in its code block in section 4, leaving everything else untouched.
3. If a new ViewModel function or StateFlow was added, ADD it to the relevant table in section 9 without touching existing rows.
4. If a new composable or component was created, ADD it to section 10 with its signature and a short description, without touching existing entries.
5. If a screen was modified, UPDATE only the part of its description in section 8 that actually changed. If a new screen was created, ADD its full description as a new subsection.
6. If a new navigation route was added, ADD it to the routes table in section 6 without touching existing rows.
7. ADD a new numbered entry at the bottom of the feature changelog list (section 14 or whichever section has the numbered list) summarizing what was built this session in one or two sentences.
8. If StorageManager or StorageData was modified, UPDATE only the changed parts in section 5.
9. If a new dependency was added, ADD it to the tech stack table in section 2 and to the toml block without touching existing entries.
10. Write the full updated CONTEXT.md to disk, preserving every single existing line that was not explicitly changed by this session.
