package fi.zakru.mobcom.data

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert

@Database(entities = [User::class, Message::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
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

@Entity
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long,
    var sender: String,
    var message: String,
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getMessages(): List<Message>

    @Insert
    fun addMessages(messages: List<Message>)

    @Insert
    fun insert(message: Message): Long
}
