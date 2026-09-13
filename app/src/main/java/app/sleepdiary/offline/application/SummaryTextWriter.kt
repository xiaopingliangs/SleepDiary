package app.sleepdiary.offline.application

import java.io.OutputStream

/** Writes only the already-reviewed snapshot. Success includes successful stream close. */
object SummaryTextWriter {
    fun write(snapshot: String, open: () -> OutputStream?) {
        checkNotNull(open()) { "Destination stream unavailable" }.use {
            it.write(snapshot.toByteArray(Charsets.UTF_8))
        }
    }
}
