package com.andrewlevada.togglewrapper.service

import android.content.Context
import androidx.room.*

@Database(entities = [ToggleProject::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun toggleProjectsDao(): ToggleProjectsDao
}

@Dao
interface ToggleProjectsDao {
    @Query("SELECT * FROM toggle_projects WHERE id = :id")
    fun getById(id: Int): ToggleProject?

    @Query("DELETE FROM toggle_projects")
    fun reset()

    @Insert
    fun insert(users: List<ToggleProject>)
}

internal var globalDatabase: AppDatabase? = null

fun initDatabase(applicationContext: Context) {
    if (globalDatabase != null) return

    globalDatabase = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "db"
    ).allowMainThreadQueries().build()
}

fun db(): AppDatabase {
    if (globalDatabase == null)
        throw IllegalStateException("Database is not initialized")
    return globalDatabase!!
}