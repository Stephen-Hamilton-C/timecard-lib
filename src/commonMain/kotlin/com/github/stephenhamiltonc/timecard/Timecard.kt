package com.github.stephenhamiltonc.timecard

import com.github.stephenhamiltonc.timecard.result.CleanResult
import com.github.stephenhamiltonc.timecard.result.ClockResult
import com.github.stephenhamiltonc.timecard.result.UndoResult
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

/**
 * Contains and manages instances of TimeEntry
 */
@Serializable
class Timecard(
    private val _entries: MutableList<TimeEntry> = mutableListOf()
) {
    /**
     * All the TimeEntry instances that have been logged.
     */
    val entries: List<TimeEntry>
        get() = _entries

    val isClockedIn: Boolean
        get() = entries.isNotEmpty() && entries.lastOrNull()?.end == null

    val isClockedOut: Boolean
        get() = !isClockedIn

    init {
        // Verify that each TimeEntry is chronological
        var previousInstant = Instant.fromEpochSeconds(0)
        for(entry in _entries) {
            val isLastEntry = entry == _entries.last()
            if(entry.end == null && !isLastEntry)
                throw IllegalStateException("Only the last TimeEntry may have an end time of null!")

            if(entry.start < previousInstant)
                throw IllegalStateException("Timecard must be stored in chronological order!")

            // Don't need to check if start and end of the same TimeEntry are logical
            // since that is done by TimeEntry during construction

            if(entry.end != null)
                previousInstant = entry.end
        }
    }

    companion object {
        /**
         * Creates a Timecard from the given data
         * Format is "start,end\nstart"
         * This format can be retrieved with Timecard.toString()
         * @param data The data to load the Timecard from
         */
        @JvmStatic
        fun fromString(data: String): Timecard {
            val newEntries = mutableListOf<TimeEntry>()
            val entriesData = data.split("\n")
            for (entryData in entriesData) {
                if (entryData.isEmpty()) continue

                val entry = TimeEntry.fromString(entryData)
                newEntries.add(entry)
            }

            return Timecard(newEntries)
        }
    }

    /**
     * Serializes this Timecard into a String
     * Format: "start,end\nstart"
     * @return The Timecard as a serialized String
     */
    override fun toString(): String = _entries.joinToString("\n")

    /**
     * Gets a list of entries that are within the given date
     * @param date The day to filter by. Defaults to today.
     */
    fun filterByDay(date: LocalDate = LocalDate.today()): List<TimeEntry> {
        return filterByDateRange(date..date)
    }

    /**
     * Gets a list of entries from the date given to today
     * @param fromDate The date to start at
     * @return A list of entries from the given date to today
     */
    fun filterByDateRange(fromDate: LocalDate): List<TimeEntry> {
        return filterByDateRange(fromDate..LocalDate.today())
    }

    /**
     * Gets any entries that are within the date range
     * @param dateRange The range of dates to check
     * @return A list of entries that are within the range
     */
    fun filterByDateRange(dateRange: ClosedRange<LocalDate>): List<TimeEntry> {
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

    /**
     * Removes any entries older than the given date
     * @param pastDate The oldest day of entries to keep
     * @return
     * - DATE_IN_FUTURE if the given date is in the future
     * - NO_OP if there is nothing to clean
     * - SUCCESS if clean finished
     */
    fun clean(pastDate: LocalDate = LocalDate.today()): CleanResult {
        if(pastDate > LocalDate.today()) return CleanResult.DATE_IN_FUTURE

        val cleanedEntries = filterByDateRange(pastDate)
        if(_entries.size == cleanedEntries.size) return CleanResult.NO_OP

        _entries.removeAll {
            !cleanedEntries.contains(it)
        }
        return CleanResult.SUCCESS
    }

    /**
     * Removes all entries
     */
    fun clear() {
        _entries.clear()
    }

    private fun timeIsFuture(time: Instant): Boolean {
        val now = Clock.System.now()
        return now < time
    }

    /**
     * Attempts to log a clock in
     * @param time The time to clock in. Defaults to NOW
     * @return
     * - NO_OP if already clocked in
     * - TIME_IN_FUTURE if the provided time is in the future
     * - TIME_TOO_EARLY if the provided time is before the last clock out time
     * - SUCCESS if clocking in finished
     */
    fun clockIn(time: Instant = Clock.System.now()): ClockResult {
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

    /**
     * Attempts to log a clock out
     * @param time The time to clock out. Defaults to NOW
     * @return
     * - NO_OP if already clocked out
     * - TIME_IN_FUTURE if the provided time is in the future
     * - TIME_TOO_EARLY if the provided time is before the last clock in time
     * - SUCCESS if clocking out finished
     */
    fun clockOut(time: Instant = Clock.System.now()): ClockResult {
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

    /**
     * Attempts to remove the last time log
     * @return
     * - NO_OP if there are no time logs to remove
     * - SUCCESS if undo finished
     */
    fun undo(): UndoResult {
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
     * @param includeNow Determines whether NOW should be returned if previousTime is in TODAY.
     */
    private fun getLastTime(time: Instant?, previousTime: Instant, includeNow: Boolean = true): Instant? {
        if(time == null) {
            val now = Clock.System.now()
            return if(now.toLocalDate() == previousTime.toLocalDate()) {
                // These are the same day, use NOW for calculation
                if(includeNow) now else null
            } else {
                // Not on the same day, likely looking at history
                null
            }
        }

        return time
    }

    /**
     * Calculates how many minutes the user has worked so far.
     * If clocked in, this includes minutes since last clocked in,
     * if the given date is TODAY
     * @param date The day to run this calculation on. Defaults to TODAY
     * @param includeNow Whether the calculated time should assume the user is currently working.
     *                   This only applies if the date is TODAY.
     * @return The number of minutes that have been logged as work
     */
    fun calculateMinutesWorked(date: LocalDate = LocalDate.today(), includeNow: Boolean = true): Long {
        var totalMinutes = 0L
        for(entry in filterByDay(date)) {
            val endTime = getLastTime(entry.end, entry.start, includeNow) ?: continue
            val duration = endTime - entry.start
            totalMinutes += duration.inWholeMinutes
        }

        return totalMinutes
    }

    /**
     * Calculates how many minutes the user has not worked so far.
     * If clocked out, this includes minutes since last clocked out,
     * if the given date is TODAY
     * @param date The day to run this calculation on. Defaults to TODAY
     * @param includeNow Whether the calculated time should assume the user is currently on break.
     *                   This only applies if the date is TODAY.
     * @return The number of minutes that have been logged as on break
     */
    fun calculateMinutesOnBreak(date: LocalDate = LocalDate.today(), includeNow: Boolean = true): Long {
        var totalMinutes = 0L
        val entriesForDate = filterByDay(date)
        for((i, currentEntry) in entriesForDate.withIndex()) {
            val nextEntry = entriesForDate.getOrNull(i + 1)

            if(currentEntry.end != null) {
                val nextStartTime = getLastTime(nextEntry?.start, currentEntry.end, includeNow) ?: continue
                val duration = nextStartTime - currentEntry.end
                totalMinutes += duration.inWholeMinutes
            }
        }
        
        return totalMinutes
    }

    /**
     * Calculates when the worked time will reach the number of minutes given
     * @param minutesToWork How many minutes to expect a full work day to be
     * @param date The day to run this calculation on. Defaults to TODAY
     * @return An Instant determining when worked minutes reaches minutesToWork.
     * Returns null if the given date is not today and there are no entries for that day.
     */
    fun calculateExpectedEndTime(minutesToWork: Long, date: LocalDate = LocalDate.today()): Instant? {
        val minutesOnBreak = calculateMinutesOnBreak(date)

        val entriesForDate = filterByDay(date)
        if(entriesForDate.isEmpty() && date != LocalDate.today()) return null

        val startTime = entriesForDate.firstOrNull()?.start ?: Clock.System.now()
        // See #4 for why we're adding 1 more minute here
        return startTime.plus(minutesToWork + minutesOnBreak + 1, DateTimeUnit.MINUTE)
    }
}
