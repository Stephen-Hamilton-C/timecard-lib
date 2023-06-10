package com.github.stephenhamiltonc.timecard

import com.github.stephenhamiltonc.timecard.result.*
import kotlinx.datetime.*
import kotlin.test.*

class TimecardTest {
    private lateinit var timecard1: Timecard
    private lateinit var timecard2: Timecard
    private lateinit var timecard3: Timecard
    private lateinit var timecard4: Timecard
    private lateinit var timecard5: Timecard
    private lateinit var timecard6: Timecard
    private lateinit var timecard7: Timecard

    private val timeEntry0 = TimeEntry.fromString("0")
    private val timeEntry0to60 = TimeEntry.fromString("0,60000")
    private val timeEntry120to300 = TimeEntry.fromString("120000,300000")
    private val timeEntry450 = TimeEntry.fromString("450000")
    private val timeEntry0to120 = TimeEntry.fromString("0,120000")
    private val timeEntryInDay1 = TimeEntry.fromString("86400000,86460000")
    private val timeEntryInDay2 = TimeEntry.fromString("172800000,172860000")
    private val timeEntryInDay2NoEnd = TimeEntry.fromString("172800000")
    private val timeEntryAcrossTwoDays = TimeEntry.fromString("86400000,172800000")

    // Timezones make me want to scream
    private val day0 = timeEntry0.start.toLocalDate()
    private val day1 = timeEntryInDay1.start.toLocalDate()
    private val day2 = timeEntryInDay2.start.toLocalDate()
    private val day3 = day2.plus(1, DateTimeUnit.DAY)
    private val day4 = day3.plus(1, DateTimeUnit.DAY)

    private val futureTime = Clock.System.now().plus(1, DateTimeUnit.MINUTE)
    private val oneHourAgo = Clock.System.now().minus(1, DateTimeUnit.HOUR)

    @BeforeTest
    fun beforeEach() {
        timecard1 = Timecard()
        timecard2 = Timecard(mutableListOf(
            timeEntry0
        ))
        timecard3 = Timecard(mutableListOf(
            timeEntry0to60,
            timeEntry120to300,
        ))
        timecard4 = Timecard(mutableListOf(
            timeEntry0to60,
            timeEntry120to300,
            timeEntry450,
        ))
        timecard5 = Timecard(
            mutableListOf(
                timeEntry0to120,
                timeEntryInDay1,
                timeEntryInDay2NoEnd,
            )
        )
        timecard6 = Timecard(
            mutableListOf(
                timeEntry0to120,
                timeEntryInDay1,
                timeEntryInDay2,
            )
        )
        timecard7 = Timecard(
            mutableListOf(
                timeEntry0to60,
                timeEntry120to300,
                timeEntryAcrossTwoDays,
            )
        )
    }

    @Test
    fun testConstructorValidation() {
        val instant0 = Instant.fromEpochSeconds(0)
        val instant60 = Instant.fromEpochSeconds(60000)
        val instant120 = Instant.fromEpochSeconds(120000)

        assertFails {
            Timecard(mutableListOf(TimeEntry(instant0, instant120), TimeEntry(instant60)))
        }

        assertFails {
            Timecard(mutableListOf(TimeEntry(instant0), TimeEntry(instant60, instant120)))
        }

        assertFails {
            Timecard(mutableListOf(TimeEntry(instant60), TimeEntry(instant0, instant120)))
        }
    }

    @Test
    fun testIsClockedIn() {
        assertFalse(timecard1.isClockedIn)
        assertTrue(timecard2.isClockedIn)
        assertFalse(timecard3.isClockedIn)
        assertTrue(timecard4.isClockedIn)
        assertTrue(timecard5.isClockedIn)
        assertFalse(timecard6.isClockedIn)
        assertFalse(timecard7.isClockedIn)
    }

    @Test
    fun testIsClockedOut() {
        assertTrue(timecard1.isClockedOut)
        assertFalse(timecard2.isClockedOut)
        assertTrue(timecard3.isClockedOut)
        assertFalse(timecard4.isClockedOut)
        assertFalse(timecard5.isClockedOut)
        assertTrue(timecard6.isClockedOut)
        assertTrue(timecard7.isClockedOut)
    }

    @Test
    fun testFromString() {
        var timecard = Timecard.fromString("0,1\n2,3")
        assertEquals(
            listOf(
                TimeEntry.fromString("0,1"),
                TimeEntry.fromString("2,3")
            ),
            timecard.entries
        )

        timecard = Timecard.fromString("")
        assertEquals(
            listOf(),
            timecard.entries
        )

        timecard = Timecard.fromString("10,50\n60")
        assertEquals(
            listOf(
                TimeEntry.fromString("10,50"),
                TimeEntry.fromString("60")
            ),
            timecard.entries
        )

        assertFails {
            Timecard.fromString("50,10\n20,30")
        }

        assertFails {
            Timecard.fromString("50,60\n10,20")
        }

        assertFails {
            Timecard.fromString("10\n20,30\n40")
        }
    }

    @Test
    fun testToString() {
        assertEquals("", timecard1.toString())
        assertEquals("0", timecard2.toString())
        assertEquals("0,60000\n120000,300000", timecard3.toString())
        assertEquals("0,60000\n120000,300000\n450000", timecard4.toString())
        assertEquals("0,120000\n86400000,86460000\n172800000", timecard5.toString())
        assertEquals("0,120000\n86400000,86460000\n172800000,172860000", timecard6.toString())
        assertEquals("0,60000\n120000,300000\n86400000,172800000", timecard7.toString())
    }

    @Test
    fun testFilterByDay() {
        assertTrue(timecard1.filterByDay(day0).isEmpty())
        assertTrue(timecard2.filterByDay(day3).isEmpty())
        assertTrue(timecard3.filterByDay(day3).isEmpty())
        assertTrue(timecard4.filterByDay(day3).isEmpty())
        assertTrue(timecard5.filterByDay(day3).isEmpty())
        assertTrue(timecard6.filterByDay(day3).isEmpty())
        assertTrue(timecard7.filterByDay(day3).isEmpty())

        assertEquals(timecard2.entries, timecard2.filterByDay(day0))
        assertEquals(timecard3.entries, timecard3.filterByDay(day0))
        assertEquals(timecard4.entries, timecard4.filterByDay(day0))
        assertEquals(
            listOf(timeEntry0to120),
            timecard5.filterByDay(day0)
        )
        assertEquals(
            listOf(timeEntryInDay1),
            timecard5.filterByDay(day1)
        )
        assertEquals(
            listOf(timeEntryInDay2NoEnd),
            timecard5.filterByDay(day2)
        )
        assertEquals(
            listOf(timeEntryInDay2),
            timecard6.filterByDay(day2)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timecard7.filterByDay(day1)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timecard7.filterByDay(day2)
        )
    }

    @Test
    fun testFilterByDateRange() {
        assertTrue(timecard1.filterByDateRange(day0..day3).isEmpty())
        assertTrue(timecard2.filterByDateRange(day1..day3).isEmpty())
        assertTrue(timecard3.filterByDateRange(day1..day3).isEmpty())
        assertTrue(timecard4.filterByDateRange(day1..day3).isEmpty())
        assertTrue(timecard5.filterByDateRange(day3..day4).isEmpty())
        assertTrue(timecard6.filterByDateRange(day3..day4).isEmpty())

        assertEquals(
            timecard2.entries,
            timecard2.filterByDateRange(day0..day1)
        )
        assertEquals(
            timecard3.entries,
            timecard3.filterByDateRange(day0..day2)
        )
        assertEquals(
            timecard4.entries,
            timecard4.filterByDateRange(day0..day3)
        )
        assertEquals(
            timecard5.entries,
            timecard5.filterByDateRange(day0..day4)
        )
        assertEquals(
            timecard6.entries,
            timecard6.filterByDateRange(day0..day4)
        )
        assertEquals(
            timecard7.entries,
            timecard7.filterByDateRange(day0..day4)
        )

        assertEquals(
            listOf(
                timeEntry0to120,
                timeEntryInDay1
            ),
            timecard5.filterByDateRange(day0..day1)
        )
        assertEquals(
            listOf(
                timeEntryInDay1,
                timeEntryInDay2
            ),
            timecard6.filterByDateRange(day1..day2)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timecard7.filterByDateRange(day1..day2)
        )
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timecard7.filterByDateRange(day2..day3)
        )
    }

    @Test
    fun testClean() {
        assertEquals(CleanResult.NO_OP, timecard1.clean())
        assertEquals(CleanResult.NO_OP, timecard2.clean(day0))

        assertEquals(CleanResult.SUCCESS, timecard3.clean(day1))
        assertTrue(timecard3.entries.isEmpty())

        assertEquals(CleanResult.SUCCESS, timecard5.clean(day2))
        assertEquals(
            listOf(timeEntryInDay2NoEnd),
            timecard5.entries
        )

        assertEquals(CleanResult.SUCCESS, timecard6.clean(day1))
        assertEquals(
            listOf(
                timeEntryInDay1,
                timeEntryInDay2
            ),
            timecard6.entries
        )

        assertEquals(CleanResult.SUCCESS, timecard7.clean(day1))
        assertEquals(
            listOf(timeEntryAcrossTwoDays),
            timecard7.entries
        )

        assertEquals(CleanResult.NO_OP, timecard7.clean(day2))

        val futureDay = LocalDate.today().plus(1, DateTimeUnit.DAY)
        assertEquals(CleanResult.DATE_IN_FUTURE, timecard1.clean(futureDay))
    }

    @Test
    fun testClockIn() {
        assertEquals(ClockResult.TIME_IN_FUTURE, timecard1.clockIn(futureTime))

        assertEquals(ClockResult.TIME_TOO_EARLY, timecard3.clockIn(timeEntry0.start))

        assertEquals(ClockResult.SUCCESS, timecard1.clockIn(oneHourAgo))
        assertEquals(oneHourAgo, timecard1.entries.first().start)

        assertEquals(ClockResult.NO_OP, timecard2.clockIn(oneHourAgo))

        assertEquals(ClockResult.SUCCESS, timecard3.clockIn(oneHourAgo))
        assertEquals(oneHourAgo, timecard3.entries.last().start)

        assertEquals(ClockResult.NO_OP, timecard4.clockIn(oneHourAgo))

        assertEquals(ClockResult.NO_OP, timecard5.clockIn(oneHourAgo))

        assertEquals(ClockResult.SUCCESS, timecard6.clockIn(oneHourAgo))
        assertEquals(oneHourAgo, timecard6.entries.last().start)

        assertEquals(ClockResult.SUCCESS, timecard7.clockIn(oneHourAgo))
        assertEquals(oneHourAgo, timecard7.entries.last().start)
    }

    @Test
    fun testClockOut() {
        assertEquals(ClockResult.TIME_IN_FUTURE, timecard2.clockOut(futureTime))

        assertEquals(ClockResult.TIME_TOO_EARLY, timecard4.clockOut(timeEntry120to300.start))

        assertEquals(ClockResult.NO_OP, timecard1.clockOut(oneHourAgo))

        assertEquals(ClockResult.SUCCESS, timecard2.clockOut(oneHourAgo))
        assertEquals(oneHourAgo, timecard2.entries.first().end)

        assertEquals(ClockResult.NO_OP, timecard3.clockOut(oneHourAgo))

        assertEquals(ClockResult.SUCCESS, timecard4.clockOut(oneHourAgo))
        assertEquals(oneHourAgo, timecard4.entries.last().end)

        assertEquals(ClockResult.SUCCESS, timecard5.clockOut(oneHourAgo))
        assertEquals(oneHourAgo, timecard5.entries.last().end)

        assertEquals(ClockResult.NO_OP, timecard6.clockOut(oneHourAgo))

        assertEquals(ClockResult.NO_OP, timecard7.clockOut(oneHourAgo))
    }

    @Test
    fun testUndo() {
        assertEquals(UndoResult.NO_OP, timecard1.undo())

        assertEquals(UndoResult.SUCCESS, timecard2.undo())
        assertTrue(timecard2.entries.isEmpty())
        assertEquals(UndoResult.NO_OP, timecard2.undo())

        assertEquals(UndoResult.SUCCESS, timecard4.undo())
        assertEquals(timecard3.entries, timecard4.entries)

        assertEquals(UndoResult.SUCCESS, timecard6.undo())
        assertEquals(timecard5.entries, timecard6.entries)

        assertEquals(UndoResult.SUCCESS, timecard5.undo())
        assertEquals(
            listOf(
                timeEntry0to120,
                timeEntryInDay1
            ),
            timecard5.entries
        )
    }

    @Test
    fun testCalculateMinutesWorked() {
        assertEquals(0L, timecard1.calculateMinutesWorked(day3))
        assertEquals(0L, timecard2.calculateMinutesWorked(day3))
        assertEquals(0L, timecard3.calculateMinutesWorked(day3))
        assertEquals(0L, timecard4.calculateMinutesWorked(day3))
        assertEquals(0L, timecard5.calculateMinutesWorked(day3))
        assertEquals(0L, timecard6.calculateMinutesWorked(day3))
        assertEquals(0L, timecard7.calculateMinutesWorked(day3))

        assertEquals(0, timecard2.calculateMinutesWorked(day0))
        assertEquals(4L, timecard3.calculateMinutesWorked(day0))
        assertEquals(4L, timecard4.calculateMinutesWorked(day0))

        assertEquals(2L, timecard5.calculateMinutesWorked(day0))
        assertEquals(1L, timecard5.calculateMinutesWorked(day1))
        assertEquals(0L, timecard5.calculateMinutesWorked(day2))

        assertEquals(2L, timecard6.calculateMinutesWorked(day0))
        assertEquals(1L, timecard6.calculateMinutesWorked(day1))
        assertEquals(1L, timecard6.calculateMinutesWorked(day2))

        assertEquals(4L, timecard7.calculateMinutesWorked(day0))
        assertEquals(
            (timeEntryAcrossTwoDays.end!! - timeEntryAcrossTwoDays.start).inWholeMinutes,
            timecard7.calculateMinutesWorked(day1)
        )
        assertEquals(
            (timeEntryAcrossTwoDays.end!! - timeEntryAcrossTwoDays.start).inWholeMinutes,
            timecard7.calculateMinutesWorked(day2)
        )
    }

    @Test
    fun testCalculateMinutesOnBreak() {
        assertEquals(0, timecard1.calculateMinutesOnBreak(day3))
        assertEquals(0, timecard2.calculateMinutesOnBreak(day3))
        assertEquals(0, timecard3.calculateMinutesOnBreak(day3))
        assertEquals(0, timecard4.calculateMinutesOnBreak(day3))
        assertEquals(0, timecard5.calculateMinutesOnBreak(day3))
        assertEquals(0, timecard6.calculateMinutesOnBreak(day3))
        assertEquals(0, timecard7.calculateMinutesOnBreak(day3))

        assertEquals(0, timecard1.calculateMinutesOnBreak(day0))
        assertEquals(0, timecard2.calculateMinutesOnBreak(day0))
        assertEquals(1, timecard3.calculateMinutesOnBreak(day0))
        assertEquals(3, timecard4.calculateMinutesOnBreak(day0))
        assertEquals(0, timecard5.calculateMinutesOnBreak(day0))
        assertEquals(0, timecard5.calculateMinutesOnBreak(day1))
        assertEquals(0, timecard6.calculateMinutesOnBreak(day0))
        assertEquals(0, timecard6.calculateMinutesOnBreak(day1))
        assertEquals(0, timecard6.calculateMinutesOnBreak(day2))
        assertEquals(1, timecard7.calculateMinutesOnBreak(day0))
        assertEquals(0, timecard7.calculateMinutesOnBreak(day1))
        assertEquals(0, timecard7.calculateMinutesOnBreak(day2))
    }

    @Test
    fun testCalculateExpectedEndTime() {
        val eightHrsInMin = 8L * 60L
        val eightHrsInSec = eightHrsInMin * 60L
        val now = Clock.System.now()
        assertNull(timecard1.calculateExpectedEndTime(eightHrsInMin, day0))
        assertEquals(
            now.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timecard1.calculateExpectedEndTime(eightHrsInMin)?.epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec).epochSeconds,
            timecard2.calculateExpectedEndTime(eightHrsInMin, day0)?.epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec + 60).epochSeconds,
            timecard3.calculateExpectedEndTime(eightHrsInMin, day0)?.epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec + 3 * 60).epochSeconds,
            timecard4.calculateExpectedEndTime(eightHrsInMin, day0)?.epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec).epochSeconds,
            timecard5.calculateExpectedEndTime(eightHrsInMin, day0)?.epochSeconds
        )
        assertEquals(
            timeEntryInDay1.start.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timecard5.calculateExpectedEndTime(eightHrsInMin, day1)?.epochSeconds
        )
        assertEquals(
            timeEntryInDay2NoEnd.start.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timecard5.calculateExpectedEndTime(eightHrsInMin, day2)?.epochSeconds
        )
        assertEquals(
            timeEntryInDay1.start.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timecard6.calculateExpectedEndTime(eightHrsInMin, day1)?.epochSeconds
        )
        assertEquals(
            Instant.fromEpochSeconds(eightHrsInSec + 60).epochSeconds,
            timecard7.calculateExpectedEndTime(eightHrsInMin, day0)?.epochSeconds
        )
        assertEquals(
            timeEntryAcrossTwoDays.start.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timecard7.calculateExpectedEndTime(eightHrsInMin, day1)?.epochSeconds
        )
        assertEquals(
            timeEntryAcrossTwoDays.start.plus(eightHrsInMin, DateTimeUnit.MINUTE).epochSeconds,
            timecard7.calculateExpectedEndTime(eightHrsInMin, day2)?.epochSeconds
        )
    }
}