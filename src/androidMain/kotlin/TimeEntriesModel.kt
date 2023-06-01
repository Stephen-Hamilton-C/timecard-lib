import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.stephenhamiltonc.timecard.ITimeEntries
import com.github.stephenhamiltonc.timecard.TimeEntries
import com.github.stephenhamiltonc.timecard.TimeEntry
import com.github.stephenhamiltonc.timecard.result.CleanResult
import com.github.stephenhamiltonc.timecard.result.ClockResult
import com.github.stephenhamiltonc.timecard.result.UndoResult
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class TimeEntriesModel : ViewModel(), ITimeEntries {
    var entries by mutableStateOf(listOf<TimeEntry>())
        private set

    override val isClockedIn: Boolean
        get() = entries.lastOrNull()?.end == null
    override val isClockedOut: Boolean
        get() = !isClockedIn

    private var _timeEntries = TimeEntries()

    /**
     * Clears the current entries and loads a new TimeEntries from the given data
     * @param data Serialized TimeEntries data
     */
    fun load(data: String) {
        _timeEntries = TimeEntries.fromString(data)
        entries = _timeEntries.entries
    }

    override fun toString(): String {
        return _timeEntries.toString()
    }

    override fun filterByDay(date: LocalDate): List<TimeEntry> {
        return _timeEntries.filterByDay(date)
    }

    override fun filterByDateRange(fromDate: LocalDate): List<TimeEntry> {
        return _timeEntries.filterByDateRange(fromDate)
    }

    override fun filterByDateRange(dateRange: ClosedRange<LocalDate>): List<TimeEntry> {
        return _timeEntries.filterByDateRange(dateRange)
    }

    override fun clean(pastDate: LocalDate): CleanResult {
        return _timeEntries.clean(pastDate)
    }

    override fun clear() {
        _timeEntries.clear()
        entries = _timeEntries.entries
    }

    override fun clockIn(time: Instant): ClockResult {
        val result = _timeEntries.clockIn(time)
        entries = _timeEntries.entries
        return result
    }

    override fun clockOut(time: Instant): ClockResult {
        val result = _timeEntries.clockOut(time)
        entries = _timeEntries.entries
        return result
    }

    override fun undo(): UndoResult {
        val result = _timeEntries.undo()
        entries = _timeEntries.entries
        return result
    }

    override fun calculateMinutesWorked(date: LocalDate): Long {
        return _timeEntries.calculateMinutesWorked(date)
    }

    override fun calculateMinutesOnBreak(date: LocalDate): Long {
        return _timeEntries.calculateMinutesOnBreak(date)
    }

    override fun calculateExpectedEndTime(minutesToWork: Long, date: LocalDate): Instant? {
        return _timeEntries.calculateExpectedEndTime(minutesToWork, date)
    }
}