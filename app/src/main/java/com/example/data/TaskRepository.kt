package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getByCategory(category: String): Flow<List<Task>> =
        taskDao.getTasksByCategory(category)

    suspend fun insert(task: Task) = taskDao.insertTask(task)
    suspend fun update(task: Task) = taskDao.updateTask(task)
    suspend fun deleteById(id: Int) = taskDao.deleteTaskById(id)
}
