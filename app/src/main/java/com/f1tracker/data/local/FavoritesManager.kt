package com.f1tracker.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user's favorite drivers (2) and team (1) using SharedPreferences.
 */
object FavoritesManager {
    private const val PREFS_NAME = "f1_favorites"
    private const val KEY_DRIVER_1 = "fav_driver_1"
    private const val KEY_DRIVER_2 = "fav_driver_2"
    private const val KEY_TEAM = "fav_team"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteDriverIds(context: Context): List<String> {
        val p = prefs(context)
        return listOfNotNull(
            p.getString(KEY_DRIVER_1, null),
            p.getString(KEY_DRIVER_2, null)
        )
    }

    fun getFavoriteTeamId(context: Context): String? =
        prefs(context).getString(KEY_TEAM, null)

    fun hasFavorites(context: Context): Boolean {
        val p = prefs(context)
        return p.getString(KEY_DRIVER_1, null) != null
    }

    fun saveFavorites(context: Context, driver1Id: String, driver2Id: String, teamId: String) {
        prefs(context).edit()
            .putString(KEY_DRIVER_1, driver1Id)
            .putString(KEY_DRIVER_2, driver2Id)
            .putString(KEY_TEAM, teamId)
            .apply()
    }

    fun clearFavorites(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
