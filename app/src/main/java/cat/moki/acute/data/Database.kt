package cat.moki.acute.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cat.moki.acute.models.Album
import cat.moki.acute.models.AlbumDao
import cat.moki.acute.models.Converters
import cat.moki.acute.models.Song
import cat.moki.acute.models.SongDao

val DATABASE_NAME = "cache1.db"

@Database(entities = [Album::class, Song::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun album(): AlbumDao
    abstract fun song(): SongDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).allowMainThreadQueries().build()
        }
    }
}