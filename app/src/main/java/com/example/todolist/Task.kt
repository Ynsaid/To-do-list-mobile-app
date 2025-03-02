package com.example.todolist

data class Task(
    var title: String,
    var description: String,
    val deadline: String,
    var isCompleted: Boolean = false
)
