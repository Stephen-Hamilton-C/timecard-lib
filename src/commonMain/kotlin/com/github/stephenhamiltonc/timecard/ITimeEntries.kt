package com.github.stephenhamiltonc.timecard

import com.github.stephenhamiltonc.timecard.result.CleanResult
import com.github.stephenhamiltonc.timecard.result.ClockResult
import com.github.stephenhamiltonc.timecard.result.UndoResult
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

internal interface ITimeEntries {
    val isClockedIn: Boolean
    val isClockedOut: Boolean

    /**
     * Serializes this TimeEntries into a String
     * Format: "start,end;start"
     * @return The TimeEntries as a serialized String
     */
    override fun toString(): String

    /**
     * Gets a list of entries that are within the given date
     * @param date The day to filter by. Defaults to today.
     */
    fun filterByDay(date: LocalDate = LocalDate.today()): List<TimeEntry>
    /**
     * Gets a list of entries from the date given to today
     * @param fromDate The date to start at
     * @return A list of entries from the given date to today
     */
    fun filterByDateRange(fromDate: LocalDate): List<TimeEntry>
    /**
     * Gets any entries that are within the date range
     * @param dateRange The range of dates to check
     * @return A list of entries that are within the range
     */
    fun filterByDateRange(dateRange: ClosedRange<LocalDate>): List<TimeEntry>
    /**
     * Removes any entries older than the given date
     * @param pastDate The oldest day of entries to keep
     * @return
     * - DATE_IN_FUTURE if the given date is in the future
     * - NO_OP if there is nothing to clean
     * - SUCCESS if clean finished
     */
    fun clean(pastDate: LocalDate = LocalDate.today()): CleanResult
    /**
     * Removes all entries
     */
    fun clear()
    /**
     * Attempts to log a clock in
     * @param time The time to clock in. Defaults to NOW
     * @return
     * - NO_OP if already clocked in
     * - TIME_IN_FUTURE if the provided time is in the future
     * - TIME_TOO_EARLY if the provided time is before the last clock out time
     * - SUCCESS if clocking in finished
     */
    fun clockIn(time: Instant = Clock.System.now()): ClockResult
    /**
     * Attempts to log a clock out
     * @param time The time to clock out. Defaults to NOW
     * @return
     * - NO_OP if already clocked out
     * - TIME_IN_FUTURE if the provided time is in the future
     * - TIME_TOO_EARLY if the provided time is before the last clock in time
     * - SUCCESS if clocking out finished
     */
    fun clockOut(time: Instant = Clock.System.now()): ClockResult
    /**
     * Attempts to remove the last time log
     * @return
     * - NO_OP if there are no time logs to remove
     * - SUCCESS if undo finished
     */
    fun undo(): UndoResult
    /**
     * Calculates how many minutes the user has worked so far.
     * If clocked in, this includes minutes since last clocked in,
     * if the given date is TODAY
     * @param date The day to run this calculation on. Defaults to TODAY
     * @return The number of minutes that have been logged as work
     */
    fun calculateMinutesWorked(date: LocalDate = LocalDate.today()): Long
    /**
     * Calculates how many minutes the user has not worked so far.
     * If clocked out, this includes minutes since last clocked out,
     * if the given date is TODAY
     * @param date The day to run this calculation on. Defaults to TODAY
     * @return The number of minutes that have been logged as on break
     */
    fun calculateMinutesOnBreak(date: LocalDate = LocalDate.today()): Long
    /**
     * Calculates when the worked time will reach the number of minutes given
     * @param minutesToWork How many minutes to expect a full work day to be
     * @param date The day to run this calculation on. Defaults to TODAY
     * @return An Instant determining when worked minutes reaches minutesToWork.
     * Returns null if the given date is not today and there are no entries for that day.
     */
    fun calculateExpectedEndTime(minutesToWork: Long, date: LocalDate = LocalDate.today()): Instant?
}