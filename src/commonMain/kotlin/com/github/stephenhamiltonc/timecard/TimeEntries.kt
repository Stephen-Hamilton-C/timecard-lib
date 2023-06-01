package com.github.stephenhamiltonc.timecard

import com.github.stephenhamiltonc.timecard.result.CleanResult
import com.github.stephenhamiltonc.timecard.result.ClockResult
import com.github.stephenhamiltonc.timecard.result.UndoResult
import kotlinx.datetime.*
import kotlin.jvm.JvmStatic

/**
 * Contains and manages instances of TimeEntry
 */
class TimeEntries(
    private val _entries: MutableList<TimeEntry> = mutableListOf()
) : ITimeEntries {
    /**
     * All the TimeEntry instances that have been logged.
     */
    val entries: List<TimeEntry>
        get() = _entries

    override val isClockedIn: Boolean
        get() = entries.isNotEmpty() && entries.lastOrNull()?.end == null

    override val isClockedOut: Boolean
        get() = !isClockedIn

    init {
        validateEntries()
    }

    /**
     * Verifies that each TimeEntry is chronological
     */
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

    companion object {
        /**
         * Creates a TimeEntries from the given data
         * Format is "start,end;start"
         * This format can be retrieved with TimeEntries.toString()
         * @param data The data to load the TimeEntries from
         */
        @JvmStatic
        fun fromString(data: String): TimeEntries {
            val newEntries = mutableListOf<TimeEntry>()
            val entriesData = data.split(";")
            for (entryData in entriesData) {
                if (entryData.isEmpty()) continue

                val entry = TimeEntry.fromString(entryData)
                newEntries.add(entry)
            }

            return TimeEntries(newEntries)
        }
    }

    override fun toString(): String = _entries.joinToString(";")

    override fun filterByDay(date: LocalDate): List<TimeEntry> {
        return filterByDateRange(date..date)
    }

    override fun filterByDateRange(fromDate: LocalDate): List<TimeEntry> {
        return filterByDateRange(fromDate..LocalDate.today())
    }

    override fun filterByDateRange(dateRange: ClosedRange<LocalDate>): List<TimeEntry> {
        return _entries.filter {
            val startDate = it.start.toLocalDate()
            val endDate = it.end?.toLocalDate()
            val startDateInRange = startDate in dateRange
            it.start.toLocalDate()

            return@filter if(endDate == null) {
                startDateInRange
            } else {
                startDateInRange || (endDate in dateRange)
            }
        }
    }

    override fun clean(pastDate: LocalDate): CleanResult {
        if(pastDate > LocalDate.today()) return CleanResult.DATE_IN_FUTURE

        val cleanedEntries = filterByDateRange(pastDate)
        if(_entries.size == cleanedEntries.size) return CleanResult.NO_OP

        _entries.removeAll {
            !cleanedEntries.contains(it)
        }
        return CleanResult.SUCCESS
    }

    override fun clear() {
        _entries.clear()
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

    /**
     * Gets the last time used for work/break calculations
     * @param time The time that may exist
     * @param previousTime The time before the time that may exist
     */
    private fun getLastTime(time: Instant?, previousTime: Instant): Instant? {
        if(time == null) {
            val now = Clock.System.now()
            return if(now.toLocalDate() == previousTime.toLocalDate()) {
                // These are the same day, use NOW for calculation
                now
            } else {
                // Not on the same day, likely looking at history
                null
            }
        }

        return time
    }

    override fun calculateMinutesWorked(date: LocalDate): Long {
        var totalMinutes = 0L
        for(entry in filterByDay(date)) {
            val endTime = getLastTime(entry.end, entry.start) ?: continue
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
                val nextStartTime = getLastTime(nextEntry?.start, currentEntry.end) ?: continue
                val duration = nextStartTime - currentEntry.end
                totalMinutes += duration.inWholeMinutes
            }
        }
        
        return totalMinutes
    }

    override fun calculateExpectedEndTime(minutesToWork: Long, date: LocalDate): Instant? {
        val minutesOnBreak = calculateMinutesOnBreak(date)

        val entriesForDate = filterByDay(date)
        if(entriesForDate.isEmpty() && date != LocalDate.today()) return null

        val startTime = entriesForDate.firstOrNull()?.start ?: Clock.System.now()
        return startTime.plus(minutesToWork + minutesOnBreak, DateTimeUnit.MINUTE)
    }
}
