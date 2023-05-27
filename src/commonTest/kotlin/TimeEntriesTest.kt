import kotlinx.datetime.*
import kotlin.test.*

class TimeEntriesTest {
    private lateinit var timeEntries1: TimeEntries
    private lateinit var timeEntries2: TimeEntries
    private lateinit var timeEntries3: TimeEntries
    private lateinit var timeEntries4: TimeEntries
    private lateinit var timeEntries5: TimeEntries
    private lateinit var timeEntries6: TimeEntries
    private lateinit var timeEntries7: TimeEntries

    private val timeEntry0 = TimeEntry.from("0")
    private val timeEntry0to60 = TimeEntry.from("0,60")
    private val timeEntry120to300 = TimeEntry.from("120,300")
    private val timeEntry450 = TimeEntry.from("450")
    private val timeEntry0to120 = TimeEntry.from("0,120")
    private val timeEntryInDay1 = TimeEntry.from("86400,86460")
    private val timeEntryInDay2 = TimeEntry.from("172800,172860")
    private val timeEntryInDay2NoEnd = TimeEntry.from("172800")
    private val timeEntryAcrossTwoDays = TimeEntry.from("86400,172800")

    // Timezones make me want to scream
    private val day0 = timeEntry0.start.toLocalDate()
    private val day1 = timeEntryInDay1.start.toLocalDate()
    private val day2 = timeEntryInDay2.start.toLocalDate()
    private val day3 = day2.plus(1, DateTimeUnit.DAY)
    private val day4 = day3.plus(1, DateTimeUnit.DAY)

    private val futureTime = Clock.System.now().plus(1, DateTimeUnit.MINUTE)
    private val time0 = Clock.System.now().minus(1, DateTimeUnit.HOUR)
    private val time1 = time0.plus(30, DateTimeUnit.MINUTE)

    @BeforeTest
    fun beforeEach() {
        timeEntries1 = TimeEntries()
        timeEntries2 = TimeEntries(mutableListOf(
            timeEntry0
        ))
        timeEntries3 = TimeEntries(mutableListOf(
            timeEntry0to60,
            timeEntry120to300,
        ))
        timeEntries4 = TimeEntries(mutableListOf(
            timeEntry0to60,
            timeEntry120to300,
            timeEntry450,
        ))
        timeEntries5 = TimeEntries(
            mutableListOf(
                timeEntry0to120,
                timeEntryInDay1,
                timeEntryInDay2NoEnd,
            )
        )
        timeEntries6 = TimeEntries(
            mutableListOf(
                timeEntry0to120,
                timeEntryInDay1,
                timeEntryInDay2,
            )
        )
        timeEntries7 = TimeEntries(
            mutableListOf(
                timeEntry0to60,
                timeEntry120to300,
                timeEntryAcrossTwoDays,
            )
        )
    }

    @Test
    fun testIsClockedIn() {
        assertFalse(timeEntries1.isClockedIn)
        assertTrue(timeEntries2.isClockedIn)
        assertFalse(timeEntries3.isClockedIn)
        assertTrue(timeEntries4.isClockedIn)
        assertTrue(timeEntries5.isClockedIn)
        assertFalse(timeEntries6.isClockedIn)
        assertFalse(timeEntries7.isClockedIn)
    }

    @Test
    fun testIsClockedOut() {
        assertTrue(timeEntries1.isClockedOut)
        assertFalse(timeEntries2.isClockedOut)
        assertTrue(timeEntries3.isClockedOut)
        assertFalse(timeEntries4.isClockedOut)
        assertFalse(timeEntries5.isClockedOut)
        assertTrue(timeEntries6.isClockedOut)
        assertTrue(timeEntries7.isClockedOut)
    }

    @Test
    fun testLoad() {
        timeEntries1.load("0,1;2,3")
        assertEquals(
            listOf(
                TimeEntry.from("0,1"),
                TimeEntry.from("2,3")
            ),
            timeEntries1.entries
        )

        timeEntries2.load("")
        assertEquals(
            listOf(),
            timeEntries2.entries
        )

        timeEntries3.load("10,50;60")
        assertEquals(
            listOf(
                TimeEntry.from("10,50"),
                TimeEntry.from("60")
            ),
            timeEntries3.entries
        )

        assertFails {
            timeEntries4.load("50,10;20,30")
        }

        assertFails {
            timeEntries5.load("50,60;10,20")
        }

        assertFails {
            timeEntries6.load("10;20,30;40")
        }
    }

    @Test
    fun testToString() {
        assertEquals("", timeEntries1.toString())
        assertEquals("0", timeEntries2.toString())
        assertEquals("0,60;120,300", timeEntries3.toString())
        assertEquals("0,60;120,300;450", timeEntries4.toString())
        assertEquals("0,120;86400,86460;172800", timeEntries5.toString())
        assertEquals("0,120;86400,86460;172800,172860", timeEntries6.toString())
        assertEquals("0,60;120,300;86400,172800", timeEntries7.toString())
    }

    @Test
    fun testFilterByDay() {
        assertTrue(timeEntries1.filterByDay(day0).isEmpty())
        assertTrue(timeEntries2.filterByDay(day3).isEmpty())
        assertTrue(timeEntries3.filterByDay(day3).isEmpty())
        assertTrue(timeEntries4.filterByDay(day3).isEmpty())
        assertTrue(timeEntries5.filterByDay(day3).isEmpty())
        assertTrue(timeEntries6.filterByDay(day3).isEmpty())
        assertTrue(timeEntries7.filterByDay(day3).isEmpty())

        assertEquals(timeEntries2.entries, timeEntries2.filterByDay(day0))
        assertEquals(timeEntries3.entries, timeEntries3.filterByDay(day0))
        assertEquals(timeEntries4.entries, timeEntries4.filterByDay(day0))
        assertEquals(
            listOf(timeEntry0to120),
            timeEntries5.filterByDay(day0)
        )
        assertEquals(
            listOf(timeEntryInDay1),
            timeEntries5.filterByDay(day1)
        )
        assertEquals(
            listOf(timeEntryInDay2NoEnd),
            timeEntries5.filterByDay(day2)
        )
        assertEquals(
            listOf(timeEntryInDay2),
            timeEntries6.filterByDay(day2)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timeEntries7.filterByDay(day1)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timeEntries7.filterByDay(day2)
        )
    }

    @Test
    fun testFilterByDateRange() {
        assertTrue(timeEntries1.filterByDateRange(day0, day3).isEmpty())
        assertTrue(timeEntries2.filterByDateRange(day1, day3).isEmpty())
        assertTrue(timeEntries3.filterByDateRange(day1, day3).isEmpty())
        assertTrue(timeEntries4.filterByDateRange(day1, day3).isEmpty())
        assertTrue(timeEntries5.filterByDateRange(day3, day4).isEmpty())
        assertTrue(timeEntries6.filterByDateRange(day3, day4).isEmpty())

        assertEquals(
            timeEntries2.entries,
            timeEntries2.filterByDateRange(day0, day1)
        )
        assertEquals(
            timeEntries3.entries,
            timeEntries3.filterByDateRange(day0, day2)
        )
        assertEquals(
            timeEntries4.entries,
            timeEntries4.filterByDateRange(day0, day3)
        )
        assertEquals(
            timeEntries5.entries,
            timeEntries5.filterByDateRange(day0, day4)
        )
        assertEquals(
            timeEntries6.entries,
            timeEntries6.filterByDateRange(day0, day4)
        )
        assertEquals(
            timeEntries7.entries,
            timeEntries7.filterByDateRange(day0, day4)
        )

        assertEquals(
            listOf(
                timeEntry0to120,
                timeEntryInDay1
            ),
            timeEntries5.filterByDateRange(day0, day1)
        )
        assertEquals(
            listOf(
                timeEntryInDay1,
                timeEntryInDay2
            ),
            timeEntries6.filterByDateRange(day1, day2)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timeEntries7.filterByDateRange(day1, day2)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timeEntries7.filterByDateRange(day2, day3)
        )
    }

    @Test
    fun testClean() {
        assertEquals(CleanResult.NO_OP, timeEntries1.clean())
        assertEquals(CleanResult.NO_OP, timeEntries2.clean(day0))

        assertEquals(CleanResult.SUCCESS, timeEntries3.clean(day1))
        assertTrue(timeEntries3.entries.isEmpty())

        assertEquals(CleanResult.SUCCESS, timeEntries5.clean(day2))
        assertEquals(
            listOf(timeEntryInDay2NoEnd),
            timeEntries5.entries
        )

        assertEquals(CleanResult.SUCCESS, timeEntries6.clean(day1))
        assertEquals(
            listOf(
                timeEntryInDay1,
                timeEntryInDay2
            ),
            timeEntries6.entries
        )

        assertEquals(CleanResult.SUCCESS, timeEntries7.clean(day1))
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timeEntries7.entries
        )

        assertEquals(CleanResult.NO_OP, timeEntries7.clean(day2))
    }

    @Test
    fun testClockIn() {
        assertEquals(ClockResult.TIME_IN_FUTURE, timeEntries1.clockIn(futureTime))

        assertEquals(ClockResult.TIME_TOO_EARLY, timeEntries3.clockIn(timeEntry0.start))

        assertEquals(ClockResult.SUCCESS, timeEntries1.clockIn(time0))
        assertEquals(time0, timeEntries1.entries.first().start)

        assertEquals(ClockResult.NO_OP, timeEntries2.clockIn(time0))

        assertEquals(ClockResult.SUCCESS, timeEntries3.clockIn(time0))
        assertEquals(time0, timeEntries3.entries.last().start)

        assertEquals(ClockResult.NO_OP, timeEntries4.clockIn(time0))

        assertEquals(ClockResult.NO_OP, timeEntries5.clockIn(time0))

        assertEquals(ClockResult.SUCCESS, timeEntries6.clockIn(time0))
        assertEquals(time0, timeEntries6.entries.last().start)

        assertEquals(ClockResult.SUCCESS, timeEntries7.clockIn(time0))
        assertEquals(time0, timeEntries7.entries.last().start)
    }

    @Test
    fun testClockOut() {
        assertEquals(ClockResult.TIME_IN_FUTURE, timeEntries2.clockOut(futureTime))

        assertEquals(ClockResult.TIME_TOO_EARLY, timeEntries4.clockOut(timeEntry120to300.start))

        assertEquals(ClockResult.NO_OP, timeEntries1.clockOut(time0))

        assertEquals(ClockResult.SUCCESS, timeEntries2.clockOut(time0))
        assertEquals(time0, timeEntries2.entries.first().end)

        assertEquals(ClockResult.NO_OP, timeEntries3.clockOut(time0))

        assertEquals(ClockResult.SUCCESS, timeEntries4.clockOut(time0))
        assertEquals(time0, timeEntries4.entries.last().end)

        assertEquals(ClockResult.SUCCESS, timeEntries5.clockOut(time0))
        assertEquals(time0, timeEntries5.entries.last().end)

        assertEquals(ClockResult.NO_OP, timeEntries6.clockOut(time0))

        assertEquals(ClockResult.NO_OP, timeEntries7.clockOut(time0))
    }

    @Test
    fun testUndo() {
        assertEquals(UndoResult.NO_OP, timeEntries1.undo())

        assertEquals(UndoResult.SUCCESS, timeEntries2.undo())
        assertTrue(timeEntries2.entries.isEmpty())
        assertEquals(UndoResult.NO_OP, timeEntries2.undo())

        assertEquals(UndoResult.SUCCESS, timeEntries4.undo())
        assertEquals(timeEntries3.entries, timeEntries4.entries)

        assertEquals(UndoResult.SUCCESS, timeEntries6.undo())
        assertEquals(timeEntries5.entries, timeEntries6.entries)

        assertEquals(UndoResult.SUCCESS, timeEntries5.undo())
        assertEquals(
            listOf(
                timeEntry0to120,
                timeEntryInDay1
            ),
            timeEntries5.entries
        )
    }

    private fun minutesSince(epochSeconds: Long): Long {
        return minutesSince(Instant.fromEpochSeconds(epochSeconds))
    }

    private fun minutesSince(instant: Instant): Long {
        val now = Clock.System.now()
        return (now - instant).inWholeMinutes
    }

    @Test
    fun testCalculateMinutesWorked() {
        assertEquals(0L, timeEntries1.calculateMinutesWorked(day3))
        assertEquals(0L, timeEntries2.calculateMinutesWorked(day3))
        assertEquals(0L, timeEntries3.calculateMinutesWorked(day3))
        assertEquals(0L, timeEntries4.calculateMinutesWorked(day3))
        assertEquals(0L, timeEntries5.calculateMinutesWorked(day3))
        assertEquals(0L, timeEntries6.calculateMinutesWorked(day3))
        assertEquals(0L, timeEntries7.calculateMinutesWorked(day3))

        assertEquals(minutesSince(0), timeEntries2.calculateMinutesWorked(day0))
        assertEquals(4L, timeEntries3.calculateMinutesWorked(day0))
        assertEquals(
            4L + minutesSince(450L),
            timeEntries4.calculateMinutesWorked(day0)
        )

        assertEquals(2L, timeEntries5.calculateMinutesWorked(day0))
        assertEquals(1L, timeEntries5.calculateMinutesWorked(day1))
        assertEquals(minutesSince(timeEntryInDay2NoEnd.start), timeEntries5.calculateMinutesWorked(day2))

        assertEquals(2L, timeEntries6.calculateMinutesWorked(day0))
        assertEquals(1L, timeEntries6.calculateMinutesWorked(day1))
        assertEquals(1L, timeEntries6.calculateMinutesWorked(day2))

        assertEquals(4L, timeEntries7.calculateMinutesWorked(day0))
        assertEquals(
            (timeEntryAcrossTwoDays.end!! - timeEntryAcrossTwoDays.start).inWholeMinutes,
            timeEntries7.calculateMinutesWorked(day1)
        )
        assertEquals(
            (timeEntryAcrossTwoDays.end!! - timeEntryAcrossTwoDays.start).inWholeMinutes,
            timeEntries7.calculateMinutesWorked(day2)
        )
    }

    @Test
    fun testCalculateMinutesOnBreak() {
        TODO("Not implemented yet")
    }

    @Test
    fun testCalculateExpectedEndTime() {
        val eightHrsInMin = 8L * 60L
        val eightHrsInSec = eightHrsInMin * 60L
        val now = Clock.System.now()
        assertEquals(
            now.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timeEntries1.calculateExpectedEndTime(eightHrsInMin).epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec).epochSeconds,
            timeEntries2.calculateExpectedEndTime(eightHrsInMin).epochSeconds
        )
        // TODO: Figure out if this logic is wrong or if the function is wrong
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec + 60).epochSeconds,
            timeEntries3.calculateExpectedEndTime(eightHrsInMin).epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec + 4 * 60).epochSeconds,
            timeEntries4.calculateExpectedEndTime(eightHrsInMin).epochSeconds
        )
        TODO("Not finished yet")
    }
}