package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "victory_logs")
data class VictoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "REFLECT", "TRIGGER", or "CBT"
    val notes: String = "",
    val triggerContext: String = "",
    val automaticThought: String = "",
    val identifiedDistortion: String = "",
    val reframedTruth: String = "",
    val scriptureReference: String = "",
    val userId: String = "" // Binds Log securely to an authenticated session
)

@Entity(tableName = "freedom_goals")
data class FreedomGoal(
    @PrimaryKey val id: Int = 1, // Single-entry configuration
    val startDate: Long = System.currentTimeMillis(),
    val struggleType: String = "Substance Use", // "Substance Use", "Anxiety", "Depression", "Fear", "Other"
    val customDeclaration: String = "I am a new creation in Christ; the old has gone, the new is here!",
    val userId: String = "" // Binds Goal counter securely to an authenticated session
)

@Entity(tableName = "daily_check_ins")
data class DailyCheckIn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String, // e.g. "Joyful", "Peaceful", "Anxious", "Struggling", "Tempted", "Discouraged"
    val energyLevel: Int, // 1 to 5
    val triggers: String = "",
    val journalNotes: String = "",
    val aiReflection: String = "",
    val userId: String = "" // Binds Log securely to an authenticated session
)

// --- DAOs ---

@Dao
interface VictoryLogDao {
    @Query("SELECT * FROM victory_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<VictoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VictoryLog)

    @Query("DELETE FROM victory_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
    
    @Delete
    suspend fun deleteLog(log: VictoryLog)
}

@Dao
interface FreedomGoalDao {
    @Query("SELECT * FROM freedom_goals WHERE id = 1")
    fun getFreedomGoalFlow(): Flow<FreedomGoal?>

    @Query("SELECT * FROM freedom_goals WHERE id = 1")
    suspend fun getFreedomGoal(): FreedomGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreedomGoal(goal: FreedomGoal)
}

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_check_ins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckIn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckIn)

    @Query("DELETE FROM daily_check_ins WHERE id = :id")
    suspend fun deleteCheckInById(id: Int)
}

// --- App Database ---

@Database(entities = [VictoryLog::class, FreedomGoal::class, DailyCheckIn::class], version = 3, exportSchema = false)
abstract class OverComerDatabase : RoomDatabase() {
    abstract val victoryLogDao: VictoryLogDao
    abstract val freedomGoalDao: FreedomGoalDao
    abstract val dailyCheckInDao: DailyCheckInDao

    companion object {
        @Volatile
        private var INSTANCE: OverComerDatabase? = null

        fun getDatabase(context: Context): OverComerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OverComerDatabase::class.java,
                    "overcomer_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository abstraction ---

class OverComerRepository(private val db: OverComerDatabase) {
    val victoryLogs: Flow<List<VictoryLog>> = db.victoryLogDao.getAllLogs()
    val freedomGoal: Flow<FreedomGoal?> = db.freedomGoalDao.getFreedomGoalFlow()
    val dailyCheckIns: Flow<List<DailyCheckIn>> = db.dailyCheckInDao.getAllCheckIns()

    suspend fun insertLog(log: VictoryLog) {
        db.victoryLogDao.insertLog(log)
    }

    suspend fun deleteLogById(id: Int) {
        db.victoryLogDao.deleteLogById(id)
    }

    suspend fun getFreedomGoal(): FreedomGoal? {
        return db.freedomGoalDao.getFreedomGoal()
    }

    suspend fun updateFreedomGoal(goal: FreedomGoal) {
        db.freedomGoalDao.insertFreedomGoal(goal)
    }

    suspend fun insertCheckIn(checkIn: DailyCheckIn) {
        db.dailyCheckInDao.insertCheckIn(checkIn)
    }

    suspend fun deleteCheckInById(id: Int) {
        db.dailyCheckInDao.deleteCheckInById(id)
    }
}
