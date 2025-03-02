package com.example.todolist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CompletedFragment : Fragment() {
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var emptyMessage: TextView
    private val completedTasks = mutableListOf<Task>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_completed, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerCompletedTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(completedTasks, onRemove = { position -> removeTask(position) }, {}, {}, true)

        recyclerView.adapter = taskAdapter
        emptyMessage = view.findViewById(R.id.emptyMessage)
        updateEmptyMessage()
        return view
    }

    private fun updateEmptyMessage() {
        if(completedTasks.isEmpty()){
            emptyMessage.visibility = View.VISIBLE
        }else{
            emptyMessage.visibility = View.GONE
        }
    }

    fun addTask(task: Task) {
        completedTasks.add(task)
        activity?.runOnUiThread {  // 🔹 Ensure UI updates
            taskAdapter.notifyItemInserted(completedTasks.size - 1)
            updateEmptyMessage()
        }

    }
    fun removeTask(position: Int) {
        if(position >=0 && position < completedTasks.size){
            completedTasks.removeAt(position)
            activity?.runOnUiThread {
                taskAdapter.notifyItemRemoved(position)
                taskAdapter.notifyItemRangeChanged(position,completedTasks.size)
                updateEmptyMessage()
            }
        }
    }

}