package com.example.todolist

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class PendingFragment : Fragment() {
    private val channelId = "task_notifications"  // Notification Channel ID
    private val notificationId = 1234

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pending, container, false)
        recyclerView = view.findViewById(R.id.reyclcerOfTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        emptyMessage = view.findViewById(R.id.emptyMessage)
        sharedPreferences = requireContext().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
        loadTasks()

        // 🔹 Initialize TaskAdapter with logic
        taskAdapter = TaskAdapter(taskList,
            onRemove = { position -> removeTask(position) },
            onComplete = { task -> moveTaskToCompleted(task) },
            onOverdue = { task -> moveToOverdue(task) }
        )

        recyclerView.adapter = taskAdapter
        updateEmptyMessage()
        checkOverdueTasks()
        createNotificationChannel()
        scheduleNotifications() // Schedule notifications for tasks

        return view
    }

    private fun moveToOverdue(task: Task) {
        val position = taskList.indexOf(task)
        if (position != -1) {
            removeTask(position)

            val overdueFragment = parentFragmentManager.fragments.find { it is OverdueFragment } as? OverdueFragment
            if (overdueFragment != null) {
                overdueFragment.addTask(task)  // ✅ Move to OverdueFragment
            } else {
                Log.e("PendingFragment", "OverdueFragment not found")
            }
        }
    }

    private fun updateEmptyMessage() {
        emptyMessage.visibility = if (taskList.isEmpty()) View.VISIBLE else View.GONE
    }

    fun addTask(task: Task) {
        if (isTaskOverdue(task)) {
            moveToOverdue(task) // 🚨 Move directly if overdue
        } else {
            taskList.add(task)
            taskAdapter.notifyItemInserted(taskList.size - 1)
            saveTasks()
            updateEmptyMessage()
            scheduleNotifications() // Re-schedule notifications when a new task is added
        }
    }

    private fun removeTask(position: Int) {
        if (position in taskList.indices) {
            taskList.removeAt(position)

            recyclerView.post {
                taskAdapter.notifyItemRemoved(position)
                taskAdapter.notifyItemRangeChanged(position, taskList.size) // Update item positions safely
            }

            saveTasks()
            updateEmptyMessage()
            scheduleNotifications() // Update notifications when a task is removed
        }
    }


    private fun moveTaskToCompleted(task: Task) {
        val position = taskList.indexOf(task)
        if (position != -1) {
            removeTask(position)

            val completedFragment = parentFragmentManager.fragments.find { it is CompletedFragment } as? CompletedFragment
            if (completedFragment != null) {
                completedFragment.addTask(task)  // ✅ Move to CompletedFragment
            } else {
                Log.e("PendingFragment", "CompletedFragment not found")
            }
        }
    }

    private fun saveTasks() {
        sharedPreferences.edit().putString("task_list", gson.toJson(taskList)).apply()
    }

    private fun loadTasks() {
        val json = sharedPreferences.getString("task_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            taskList.clear()
            taskList.addAll(gson.fromJson(json, type))
        }
    }

    private fun isTaskOverdue(task: Task): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val deadlineDate = sdf.parse(task.deadline)
            deadlineDate != null && deadlineDate.before(Date())  // ✅ Check if past date
        } catch (e: Exception) {
            false
        }
    }

    private fun checkOverdueTasks() {
        val overdueTasks = taskList.filter { isTaskOverdue(it) }
        overdueTasks.forEach { moveToOverdue(it) }
    }

    private fun scheduleNotifications() {
        for (task in taskList) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            try {
                val deadlineDate = sdf.parse(task.deadline)
                if (deadlineDate != null) {
                    val notifyTime = deadlineDate.time - (10 * 60 * 1000) // 10 min before deadline
                    if (notifyTime > System.currentTimeMillis()) {
                        scheduleNotification(task.title, notifyTime)
                    }
                }
            } catch (e: Exception) {
                Log.e("PendingFragment", "Error parsing date: ${task.deadline}")
            }
        }
    }

    private fun scheduleNotification(taskTitle: String, notifyTime: Long) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        intent.putExtra("task_title", taskTitle)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notifications"
            val descriptionText = "Reminds users about upcoming tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null  // ✅ Prevent memory leaks
    }
}
