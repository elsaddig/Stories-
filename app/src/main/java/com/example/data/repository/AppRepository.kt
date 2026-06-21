package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.db.SavedWord
import com.example.data.db.SavedWordDao
import com.example.data.db.UserStats
import com.example.data.db.UserStatsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import java.util.Calendar

class AppRepository(
    private val savedWordDao: SavedWordDao,
    private val userStatsDao: UserStatsDao
) {
    val allSavedWords: Flow<List<SavedWord>> = savedWordDao.getAllSavedWords()
    val userStats: Flow<UserStats?> = userStatsDao.getUserStatsFlow()
    val learnedCount: Flow<Int> = savedWordDao.getLearnedCount()
    val totalCountFlow: Flow<Int> = savedWordDao.getTotalCountFlow()

    // Smart Dictionary search
    suspend fun lookupWord(word: String, contextSentence: String): SavedWord {
        return GeminiClient.defineWord(word, contextSentence)
    }

    suspend fun saveWord(savedWord: SavedWord) {
        savedWordDao.insertSavedWord(savedWord)
        
        // Update user stats count
        val stats = userStatsDao.getUserStats() ?: UserStats()
        userStatsDao.insertUserStats(
            stats.copy(totalWordsSaved = stats.totalWordsSaved + 1)
        )
    }

    suspend fun deleteWord(word: String) {
        savedWordDao.deleteWordByString(word)
        val stats = userStatsDao.getUserStats() ?: UserStats()
        userStatsDao.insertUserStats(
            stats.copy(totalWordsSaved = maxOf(0, stats.totalWordsSaved - 1))
        )
    }

    suspend fun toggleLearned(word: String, isLearned: Boolean) {
        val saved = savedWordDao.getSavedWord(word)
        if (saved != null) {
            savedWordDao.insertSavedWord(saved.copy(isLearned = isLearned))
        }
    }

    suspend fun markStoryAsRead(storyId: String) {
        val stats = userStatsDao.getUserStats() ?: UserStats()
        val readJsonStr = stats.readStoriesJson
        val jsonArray = try {
            JSONArray(readJsonStr)
        } catch (e: Exception) {
            JSONArray()
        }

        var alreadyRead = false
        for (i in 0 until jsonArray.length()) {
            if (jsonArray.getString(i) == storyId) {
                alreadyRead = true
                break
            }
        }

        if (!alreadyRead) {
            jsonArray.put(storyId)
            val updatedJson = jsonArray.toString()
            userStatsDao.insertUserStats(
                stats.copy(
                    totalStoriesRead = stats.totalStoriesRead + 1,
                    readStoriesJson = updatedJson
                )
            )
        }
        
        // Trigger streak tick because reading completed
        updateStreak()
    }

    suspend fun updateStreak() {
        val stats = userStatsDao.getUserStats() ?: UserStats()
        val now = System.currentTimeMillis()
        
        if (stats.lastActiveTimestamp == 0L) {
            // First time ever
            userStatsDao.insertUserStats(
                stats.copy(
                    currentStreak = 1,
                    lastActiveTimestamp = now
                )
            )
            return
        }

        val lastCal = Calendar.getInstance().apply { timeInMillis = stats.lastActiveTimestamp }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        val isSameDay = lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                lastCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            // Already active today, hold current stats but update timestamp to precise current time
            userStatsDao.insertUserStats(stats.copy(lastActiveTimestamp = now))
            return
        }

        // Set yesterday calendar
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val isYesterday = lastCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                lastCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)

        val newStreak = if (isYesterday) {
            stats.currentStreak + 1
        } else {
            1 // Streak broken, restart at 1
        }

        userStatsDao.insertUserStats(
            stats.copy(
                currentStreak = newStreak,
                lastActiveTimestamp = now
            )
        )
    }
}
