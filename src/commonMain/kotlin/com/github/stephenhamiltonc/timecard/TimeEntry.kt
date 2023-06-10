package com.github.stephenhamiltonc.timecard

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

/**
 * A single entry with a start and an open end.
 * A null end time means the user is still clocked in.
 */
@Serializable
data class TimeEntry(val start: Instant, val end: Instant? = null) {
    init {
        if(end != null && start > end)
            throw IllegalStateException("A TimeEntry cannot have a start time that is after an end time!")
    }

    companion object {
        /**
         * Creates a TimeEntry from the given data
         * Format is "start,end" or "start"
         * This format can be retrieved with TimeEntry.toString()
         * @param data The data to load the TimeEntry from
         * @throws IllegalStateException If the start and end times in the data are not in chronological order
         */
        @JvmStatic
        fun fromString(data: String): TimeEntry {
            val dataSplit = data.split(",")
            val start = Instant.fromEpochMilliseconds(dataSplit[0].toLong())
            val end = if(dataSplit.size > 1) {
                Instant.fromEpochMilliseconds(dataSplit[1].toLong())
            } else null

            return TimeEntry(start, end)
        }
    }

    /**
     * Serializes this TimeEntry into a String
     * Format: "start,end" or "start"
     * @return The TimeEntry as a serialized String
     */
    override fun toString(): String {
        return if(end == null) {
            start.toEpochMilliseconds().toString()
        } else {
            "${start.toEpochMilliseconds()},${end.toEpochMilliseconds()}"
        }
    }
}