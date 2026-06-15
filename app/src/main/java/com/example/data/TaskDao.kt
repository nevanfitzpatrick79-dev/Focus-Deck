package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        ORDER BY
            isCompleted ASC,
            CASE WHEN dueDateMs IS NOT NULL AND dueDateMs < :nowMs AND isCompleted = 0 THEN 0 ELSE 1 END ASC,
            CASE WHEN dueDateMs IS NOT NULL AND isCompleted = 0 THEN dueDateMs ELSE 9999999999999 END ASC,
            CASE priority
                WHEN 'HIGH' THEN 0
                WHEN 'MEDIUM' THEN 1
                WHEN 'LOW' THEN 2
                ELSE 1
            END ASC,
            timestamp DESC
    """)
    fun getAllTasks(nowMs: Long = System.currentTimeMillis()): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE category = :category
        ORDER BY
            isCompleted ASC,
            CASE priority
                WHEN 'HIGH' THEN 0
                WHEN 'MEDIUM' THEN 1
                WHEN 'LOW' THEN 2
                ELSE 1
            END ASC,
            timestamp DESC
    """)
    fun getTasksByCategory(category: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}
