package com.github.stephenhamiltonc.timecard

import kotlinx.datetime.Instant
import kotlin.jvm.JvmStatic

data class TimeEntry(val start: Instant, val end: Instant? = null) {
    init {
        if(end != null && start > end)
            throw IllegalStateException("A TimeEntry cannot have a start time that is after an end time!")
    }

    companion object {
        @JvmStatic
        fun from(data: String): TimeEntry {
            val dataSplit = data.split(",")
            val start = Instant.fromEpochSeconds(dataSplit[0].toLong())
            val end = if(dataSplit.size > 1) {
                Instant.fromEpochSeconds(dataSplit[1].toLong())
            } else null

            return TimeEntry(start, end)
        }
    }

    override fun toString(): String {
        return if(end == null) {
            start.epochSeconds.toString()
        } else {
            "${start.epochSeconds},${end.epochSeconds}"
        }
    }
}