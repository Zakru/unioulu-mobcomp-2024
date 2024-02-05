package fi.zakru.mobcom.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Entity
data class User(
    @PrimaryKey val username: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "image_uri") var imageUri: String?,
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE username = :username")
    fun findByUsername(username: String): User?

    @Upsert
    fun upsert(user: User)
}
