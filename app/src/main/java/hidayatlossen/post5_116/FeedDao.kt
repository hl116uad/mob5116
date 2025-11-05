package hidayatlossen.post5_116

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(feed: Feed)

    @Update
    fun update(feed: Feed)

    @Delete
    fun delete(feed: Feed)

    @Query("SELECT * FROM feed ORDER BY id DESC")
    fun getAllFeed(): LiveData<List<Feed>>

    @Query("SELECT * FROM feed WHERE id = :feedId")
    fun getFeedById(feedId: Int): Feed?
}