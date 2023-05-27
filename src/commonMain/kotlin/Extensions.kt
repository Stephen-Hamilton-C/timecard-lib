import kotlinx.datetime.*

internal fun LocalDate.Companion.today(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

internal fun Instant.toLocalDate(): LocalDate {
    return this.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
