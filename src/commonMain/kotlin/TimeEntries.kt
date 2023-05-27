import kotlinx.datetime.*

class TimeEntries(
    private val _entries: MutableList<TimeEntry> = mutableListOf()
) : ITimeEntries {
    val entries: List<TimeEntry>
        get() = _entries

    override val isClockedIn: Boolean
        get() = entries.isNotEmpty() && entries.lastOrNull()?.end == null
    override val isClockedOut: Boolean
        get() = !isClockedIn

    init {
        validateEntries()
    }

    private fun validateEntries() {
        var previousInstant = Instant.fromEpochSeconds(0)
        for(entry in _entries) {
            val isLastEntry = entry == _entries.last()
            if(entry.end == null && !isLastEntry)
                throw IllegalStateException("Only the last TimeEntry may have an end time of null!")

            if(entry.start < previousInstant)
                throw IllegalStateException("TimeEntries must be stored in chronological order!")

            // Don't need to check if start and end of the same TimeEntry are logical
            // since that is done by TimeEntry during construction

            if(entry.end != null)
                previousInstant = entry.end
        }
    }

    override fun load(data: String) {
        _entries.clear()

        val entriesData = data.split(";")
        for(entryData in entriesData) {
            if(entryData.isEmpty()) continue

            val entry = TimeEntry.from(entryData)
            _entries.add(entry)
        }

        validateEntries()
    }
    override fun toString(): String = _entries.joinToString(";")

    override fun filterByDay(date: LocalDate): List<TimeEntry> {
        return filterByDateRange(date, date)
    }

    override fun filterByDateRange(fromDate: LocalDate, toDate: LocalDate): List<TimeEntry> {
        return _entries.filter {
            val startDate = it.start.toLocalDate()
            val endDate = it.end?.toLocalDate()
            val startDateInRange = startDate in fromDate..toDate
            it.start.toLocalDate()

            return@filter if(endDate == null) {
                startDateInRange
            } else {
                startDateInRange || (endDate in fromDate..toDate)
            }
        }
    }

    override fun clean(pastDate: LocalDate): CleanResult {
        val cleanedEntries = filterByDateRange(pastDate)
        if(_entries.size == cleanedEntries.size) return CleanResult.NO_OP

        _entries.removeAll {
            !cleanedEntries.contains(it)
        }
        return CleanResult.SUCCESS
    }

    private fun timeIsFuture(time: Instant): Boolean {
        val now = Clock.System.now()
        return now < time
    }

    override fun clockIn(time: Instant): ClockResult {
        if(isClockedIn) return ClockResult.NO_OP
        if(timeIsFuture(time)) return ClockResult.TIME_IN_FUTURE
        
        val lastEntry = entries.lastOrNull()
        // lastEntry must have an end time because we have already validated
        // that we are clocked out
        if(lastEntry != null && lastEntry.end!! >= time)
            return ClockResult.TIME_TOO_EARLY

        val newEntry = TimeEntry(time)
        _entries.add(newEntry)

        return ClockResult.SUCCESS
    }

    override fun clockOut(time: Instant): ClockResult {
        if(isClockedOut) return ClockResult.NO_OP
        if(timeIsFuture(time)) return ClockResult.TIME_IN_FUTURE

        val lastEntry = entries.last()
        if(lastEntry.start >= time)
            return ClockResult.TIME_TOO_EARLY

        val newEntry = TimeEntry(lastEntry.start, time)
        _entries.removeLast()
        _entries.add(newEntry)

        return ClockResult.SUCCESS
    }

    override fun undo(): UndoResult {
        if(_entries.isEmpty()) return UndoResult.NO_OP

        if(isClockedIn) {
            _entries.removeLast()
        } else {
            val lastEntry = _entries.removeLast()
            val newEntry = TimeEntry(lastEntry.start)
            _entries.add(newEntry)
        }

        return UndoResult.SUCCESS
    }

    override fun calculateMinutesWorked(date: LocalDate): Long {
        var totalMinutes = 0L
        for(entry in filterByDay(date)) {
            val endTime = entry.end ?: Clock.System.now()
            val duration = endTime - entry.start
            totalMinutes += duration.inWholeMinutes
        }

        return totalMinutes
    }

    override fun calculateMinutesOnBreak(date: LocalDate): Long {
        var totalMinutes = 0L
        val entriesForDate = filterByDay(date)
        for((i, currentEntry) in entriesForDate.withIndex()) {
            val nextEntry = entriesForDate.getOrNull(i + 1)

            if(currentEntry.end != null) {
                val nextStartTime = nextEntry?.start ?: Clock.System.now()
                val duration = nextStartTime - currentEntry.end
                totalMinutes += duration.inWholeMinutes
            }
        }
        
        return totalMinutes
    }

    override fun calculateExpectedEndTime(minutesToWork: Long): Instant {
        val minutesOnBreak = calculateMinutesOnBreak()

        val startTime = _entries.firstOrNull()?.start ?: Clock.System.now()
        return startTime.plus(minutesToWork + minutesOnBreak, DateTimeUnit.MINUTE)
    }
}
