package hidayatlossen.post5_116

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed")
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "gambar_feed")
    val gambarFeed: String, // bisa simpan path lokal atau URL

    @ColumnInfo(name = "caption")
    val caption: String,

    @ColumnInfo(name = "gambar_profile")
    val gambarProfile: String // juga path atau URL
)