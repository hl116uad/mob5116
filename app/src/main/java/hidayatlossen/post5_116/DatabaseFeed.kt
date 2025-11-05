package hidayatlossen.post5_116

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Feed::class], version = 1, exportSchema = false)
abstract class DatabaseFeed : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    companion object {
        @Volatile
        private var INSTANCE: DatabaseFeed? = null
        fun getDatabase(context: Context): DatabaseFeed {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseFeed::class.java,
                    "db_feed"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}