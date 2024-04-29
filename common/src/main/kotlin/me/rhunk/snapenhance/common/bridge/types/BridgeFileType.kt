package me.rhunk.snapenhance.common.bridge.types

import android.content.Context
import java.io.File


enum class BridgeFileType(val value: Int, val fileName: String, val displayName: String, private val isDatabase: Boolean = false) {
    CONFIG(0, "config.json", "Config"),
    MAPPINGS(1, "mappings.json", "Mappings"),
    MESSAGE_LOGGER_DATABASE(2, "message_logger.db", "Message Logger",true),
    PINNED_CONVERSATIONS(3, "pinned_conversations.txt", "Pinned Conversations"),
    SUSPEND_LOCATION_STATE(4, "suspend_location_state.txt", "Suspend Location State"),
    PINNED_BEST_FRIEND(5, "pinned_best_friend.txt", "Pinned Best Friend");

    fun resolve(context: Context): File = if (isDatabase) {
        context.getDatabasePath(fileName)
    } else {
        File(context.filesDir, fileName)
    }

    companion object {
        fun fromValue(value: Int): BridgeFileType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}