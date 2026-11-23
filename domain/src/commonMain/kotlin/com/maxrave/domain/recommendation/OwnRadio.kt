package com.maxrave.domain.recommendation

import com.maxrave.domain.data.model.browse.album.Track
import kotlin.concurrent.Volatile

/**
 * Fork: where the songs after the one playing come from.
 *
 * Upstream asks YouTube for a song's radio. The fork builds its own (the recommender lives in the
 * app module, which this layer cannot see), so the app registers it here at startup and the song
 * repository asks it first. With nothing registered, or when it answers null or nothing, YouTube's
 * radio is used as before: a queue never ends in silence because a third party was down.
 */
interface OwnRadioSource {
    /**
     * Songs to play after [seedVideoId], best first, the seed itself not among them. [more] asks
     * for a further batch of a radio already started from that seed. Null when it cannot answer.
     */
    suspend fun radio(
        seedVideoId: String,
        more: Boolean,
    ): List<Track>?
}

object OwnRadio {
    @Volatile
    var source: OwnRadioSource? = null

    private const val PREFIX = "own-radio:"

    /** The queue's continuation token for a radio of ours: the next batch is asked from [source]. */
    fun continuation(seedVideoId: String): String = PREFIX + seedVideoId

    /** The seed a continuation token of ours names, or null for one of YouTube's. */
    fun seedOf(continuation: String?): String? = continuation?.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)

    /** `RDAMVM<videoId>`, the id of a single song's radio and nothing else. */
    fun isSongRadio(
        playlistId: String?,
        videoId: String?,
    ): Boolean = !videoId.isNullOrBlank() && playlistId == "RDAMVM$videoId"
}

/** One play from the history: which song, how long ago, how much of it was heard. */
data class PlayHistoryRow(
    val videoId: String,
    val ageDays: Double,
    val listenedSecond: Long,
    val durationSecond: Long,
)
