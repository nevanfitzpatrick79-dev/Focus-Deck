package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority { HIGH, MEDIUM, LOW }

enum class TaskCategory(val label: String, val emoji: String) {
    ALL("All", "📋"),
    WORK("Work", "💼"),
    HOME("Home", "🏠"),
    PERSONAL("Personal", "⭐"),
    ERRANDS("Errands", "🛒"),
    HEALTH("Health", "💚")
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = TaskPriority.MEDIUM.name,
    val category: String = TaskCategory.PERSONAL.name,
    val dueDateMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

fun Task.priorityEnum(): TaskPriority =
    try { TaskPriority.valueOf(priority) } catch (e: Exception) { TaskPriority.MEDIUM }

fun Task.categoryEnum(): TaskCategory =
    try { TaskCategory.valueOf(category) } catch (e: Exception) { TaskCategory.PERSONAL }

fun Task.isOverdue(): Boolean =
    dueDateMs != null && !isCompleted && dueDateMs < System.currentTimeMillis()

fun Task.isDueToday(): Boolean {
    if (dueDateMs == null || isCompleted) return false
    val now = java.util.Calendar.getInstance()
    val due = java.util.Calendar.getInstance().apply { timeInMillis = dueDateMs }
    return now.get(java.util.Calendar.YEAR) == due.get(java.util.Calendar.YEAR) &&
           now.get(java.util.Calendar.DAY_OF_YEAR) == due.get(java.util.Calendar.DAY_OF_YEAR)
}
