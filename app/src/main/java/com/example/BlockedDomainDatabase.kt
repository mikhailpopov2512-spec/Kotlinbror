package com.example

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "blocked_domains")
data class BlockedDomain(
    @PrimaryKey val domain: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface BlockedDomainDao {
    @Query("SELECT * FROM blocked_domains ORDER BY addedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedDomain>>

    @Query("SELECT * FROM blocked_domains ORDER BY addedAt DESC")
    suspend fun getAllBlockedList(): List<BlockedDomain>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(domain: BlockedDomain)

    @Delete
    suspend fun delete(domain: BlockedDomain)
    
    @Query("DELETE FROM blocked_domains")
    suspend fun deleteAll()

    @Query("DELETE FROM blocked_domains WHERE domain = :domain")
    suspend fun deleteByDomain(domain: String)
}

@Database(entities = [BlockedDomain::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedDomainDao(): BlockedDomainDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ros_blocked_domains.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
