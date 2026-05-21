package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1, // Single profile row
    val level: Int = 1,
    val exp: Int = 0,
    val streak: Int = 0,
    val lastActiveTimestamp: Long = 0L,
    val problemsSolved: Int = 0,
    val correctProblems: Int = 0,
    val completedLessonsCsv: String = "" // Comma-separated list of lesson IDs
)

@Entity(tableName = "quiz_history")
data class QuizHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equation: String,
    val category: String, // e.g. "Linear Equations", "Quadratic"
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_message")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAO Interface ---

@Dao
interface MathTutorDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProgress(progress: UserProgress)

    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC")
    fun getQuizHistory(): Flow<List<QuizHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizHistory(history: QuizHistory)

    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_message ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentMessages(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_message")
    suspend fun clearChatHistory()
}

// --- App Database ---

@Database(
    entities = [UserProgress::class, QuizHistory::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): MathTutorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "math_tutor_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository Pattern ---

class MathTutorRepository(private val dao: MathTutorDao) {
    val userProgress: Flow<UserProgress?> = dao.getUserProgress()
    val quizHistory: Flow<List<QuizHistory>> = dao.getQuizHistory()
    val chatMessages: Flow<List<ChatMessage>> = dao.getChatMessages()

    suspend fun saveUserProgress(progress: UserProgress) {
        dao.saveUserProgress(progress)
    }

    suspend fun insertQuizHistory(history: QuizHistory) {
        dao.insertQuizHistory(history)
    }

    suspend fun insertChatMessage(message: ChatMessage) {
        dao.insertChatMessage(message)
    }

    suspend fun getRecentMessagesForContext(): List<ChatMessage> {
        return dao.getRecentMessages()
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }
}
