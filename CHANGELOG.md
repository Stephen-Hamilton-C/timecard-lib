# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.3] - 2024-07-04

### Changed
- `Timecard.calculateMinutesWorked()` now takes an optional second argument to include time since last entry to NOW.

### Fixed
- `Timecard.calculateMinutesWorked()` returning 0 minutes despite there being time worked



## [2.0.2] - 2024-07-03

### Changed
- **[BREAKING]** Minimum supported Java from 11 to 17
- `Timecard.calculateMinutesOnBreak()` now takes an optional second argument to include time since last entry to NOW.
- Updated dependencies



## [2.0.1] - 2023-07-19

### Fixed
- `Timecard.calculateExpectedEndTime()` now reports the time the user is finished rather than the last minute they need to work (#4)



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
