package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_words")
data class SavedWord(
    @PrimaryKey val word: String,
    val definition: String,
    val translation: String,
    val phonetic: String,
    val partOfSpeech: String,
    val sourceSentence: String, // Context sentence from the story
    val isLearned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val lastActiveTimestamp: Long = 0L,
    val totalStoriesRead: Int = 0,
    val totalWordsSaved: Int = 0,
    val readStoriesJson: String = "[]" // JSON array of story IDs like ["story_1", "story_2"]
)

@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY timestamp DESC")
    fun getAllSavedWords(): Flow<List<SavedWord>>

    @Query("SELECT * FROM saved_words WHERE word = :word LIMIT 1")
    suspend fun getSavedWord(word: String): SavedWord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedWord(savedWord: SavedWord)

    @Update
    suspend fun updateSavedWord(savedWord: SavedWord)

    @Delete
    suspend fun deleteSavedWord(savedWord: SavedWord)

    @Query("DELETE FROM saved_words WHERE word = :word")
    suspend fun deleteWordByString(word: String)

    @Query("SELECT COUNT(*) FROM saved_words WHERE isLearned = 1")
    fun getLearnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM saved_words")
    fun getTotalCountFlow(): Flow<Int>
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)
}

@Database(entities = [SavedWord::class, UserStats::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedWordDao(): SavedWordDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "joker_story_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
