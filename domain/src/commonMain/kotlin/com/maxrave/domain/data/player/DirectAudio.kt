package com.maxrave.domain.data.player

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Fork: a track that is a plain audio file on the web (an RSS podcast episode) rather than a
 * YouTube video. Its id carries the file's URL, so the music player can play it anywhere a
 * YouTube id goes: the stream resolver hands the URL back, the media cache keys on the id, and a
 * stored queue or history row still plays after a restart with nothing else to look up.
 *
 * Everything that only makes sense for YouTube (related songs, formats, SponsorBlock, watch-time
 * pings, lyrics, scrobbling, listening analytics) is skipped for these ids.
 */
@OptIn(ExperimentalEncodingApi::class)
object DirectAudio {
    const val PREFIX = "podcast:"

    private val codec = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

    fun idFor(url: String): String = PREFIX + codec.encode(url.encodeToByteArray())

    fun isDirect(id: String?): Boolean = id != null && id.startsWith(PREFIX)

    fun urlOf(id: String?): String? {
        if (!isDirect(id)) return null
        return runCatching { codec.decode(id!!.removePrefix(PREFIX)).decodeToString() }.getOrNull()
    }
}
