package com.github.stephenhamiltonc.timecard

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

@Serializable
data class TimeEntry(val start: Instant, val end: Instant? = null) {
    init {
        if(end != null && start > end)
            throw IllegalStateException("A TimeEntry cannot have a start time that is after an end time!")
    }

    companion object {
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

    override fun toString(): String {
        return if(end == null) {
            start.toEpochMilliseconds().toString()
        } else {
            "${start.toEpochMilliseconds()},${end.toEpochMilliseconds()}"
        }
    }
}