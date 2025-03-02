package com.example.todolist

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.gson.Gson
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OverdueFragment : Fragment() {
    private val gson = Gson()
    private lateinit var taskAdapter: TaskAdapter
    private val overdueTasks = mutableListOf<Task>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyMessage: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_overdue, container, false)

        // 🔹 Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("tasks_prefs", android.content.Context.MODE_PRIVATE)

        // 🔹 Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerOverdueTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🔹 Load saved tasks
        loadTasks()

        // 🔹 Set up TaskAdapter
        taskAdapter = TaskAdapter(overdueTasks, {}, onComplete = { task -> moveTaskToCompleted(task) }, {})
        recyclerView.adapter = taskAdapter

        // 🔹 Initialize empty message
        emptyMessage = view.findViewById(R.id.emptyMessage)
        updateEmptyMessage()

        return view
    }

    fun addTask(task: Task) {
        overdueTasks.add(task)
        activity?.runOnUiThread {
            taskAdapter.notifyItemInserted(overdueTasks.size - 1)
            updateEmptyMessage()
        }
        saveTasks() // ✅ Save tasks when adding
    }

    private fun updateEmptyMessage() {
        emptyMessage.visibility = if (overdueTasks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun saveTasks() {
        sharedPreferences.edit().putString("task_list", gson.toJson(overdueTasks)).apply()
    }

    private fun loadTasks() {
        val json = sharedPreferences.getString("task_list", null)
        if (json != null) {
            val savedTasks = gson.fromJson(json, Array<Task>::class.java).toList()
            overdueTasks.addAll(savedTasks)
        }
    }

    private fun removeTask(position: Int) {
        if (position in overdueTasks.indices) {
            overdueTasks.removeAt(position)
            recyclerView.post {
                taskAdapter.notifyItemRemoved(position)
                taskAdapter.notifyItemRangeChanged(position, overdueTasks.size)
                updateEmptyMessage()
            }
            saveTasks()

        }
    }

    private fun moveTaskToCompleted(task: Task) {
        val position = overdueTasks.indexOf(task)
        if (position != -1) {
            removeTask(position)

            val completedFragment = parentFragmentManager.findFragmentByTag("completedFragment") as? CompletedFragment
            if (completedFragment != null) {
                completedFragment.addTask(task)  // ✅ Move to CompletedFragment
            } else {
                Log.e("OverdueFragment", "CompletedFragment not found")
            }
        }
    }
}
