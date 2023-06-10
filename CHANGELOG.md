# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2023-06-09

### Added
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) capability to `Timecard` and `TimeEntry`

### Changed
- **[BREAKING]** `TimeEntries` was renamed to `Timecard`
- **[BREAKING]** `Timecard.toString()` now separates `TimeEntry`s by newline `\n` rather than semicolon `;`
  - `Timecard.fromString()` was updated to comply with this change as well
- **[BREAKING]** `TimeEntry.toString()` now stores Instants as epoch milliseconds rather than epoch seconds
    - `TimeEntry.fromString()` was updated to comply with this change as well

## [1.0.0] - 2023-06-01
- Initial release
