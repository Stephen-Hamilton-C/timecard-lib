package com.github.stephenhamiltonc.timecard

import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TimeEntryTest {
    private lateinit var instant1: Instant
    private lateinit var instant2: Instant
    private lateinit var instant3: Instant
    private lateinit var instant4: Instant

    private lateinit var timeEntry1: TimeEntry
    private lateinit var timeEntry2: TimeEntry
    private lateinit var timeEntry3: TimeEntry
    private lateinit var timeEntry4: TimeEntry
    private lateinit var timeEntry5: TimeEntry

    @BeforeTest
    fun beforeEach() {
        instant1 = Instant.fromEpochSeconds(0L)
        instant2 = Instant.fromEpochSeconds(123456L)
        instant3 = Instant.fromEpochSeconds(123457L)
        instant4 = Instant.fromEpochSeconds(987654321L)

        timeEntry1 = TimeEntry(instant1, instant2)
        timeEntry2 = TimeEntry(instant3, instant4)
        timeEntry3 = TimeEntry(instant1, instant4)
        timeEntry4 = TimeEntry(instant1, instant4)
        timeEntry5 = TimeEntry(instant1)
    }

    @Test
    fun testValidTime() {
        assertFailsWith<IllegalStateException> {
            TimeEntry(instant2, instant1)
        }
        assertFailsWith<IllegalStateException> {
            TimeEntry(instant3, instant2)
        }
        TimeEntry(instant1, instant2)
        TimeEntry(instant2, instant3)
        TimeEntry(instant3, instant4)
        TimeEntry(instant1, instant3)
        TimeEntry(instant1, instant4)
        TimeEntry(instant2, instant4)

        TimeEntry(instant1, instant1)
        TimeEntry(instant2, instant2)
        TimeEntry(instant3, instant3)
        TimeEntry(instant4, instant4)
        TimeEntry(instant1)
    }

    @Test
    fun testFrom() {
        assertEquals(timeEntry1, TimeEntry.fromString("0,123456"))
        assertEquals(timeEntry2, TimeEntry.fromString("123457,987654321"))
        assertEquals(timeEntry3, TimeEntry.fromString("0,987654321"))
        assertEquals(timeEntry4, TimeEntry.fromString("0,987654321"))
        assertEquals(timeEntry5, TimeEntry.fromString("0"))
        assertFailsWith<IllegalStateException> {
            TimeEntry.fromString("50,10")
        }
    }

    @Test
    fun testToString() {
        assertEquals("0,123456", timeEntry1.toString())
        assertEquals("123457,987654321", timeEntry2.toString())
        assertEquals("0,987654321", timeEntry3.toString())
        assertEquals("0,987654321", timeEntry4.toString())
        assertEquals("0", timeEntry5.toString())
    }

    @Test
    fun testEquals() {
        assertEquals(timeEntry3, timeEntry4)
        assertEquals(timeEntry1, timeEntry1)

        assertNotEquals(timeEntry1, timeEntry2)
        assertNotEquals(timeEntry4, timeEntry5)
        assertNotEquals<TimeEntry?>(null, timeEntry1)
        assertNotEquals<Any>(timeEntry1, 0)
    }
}