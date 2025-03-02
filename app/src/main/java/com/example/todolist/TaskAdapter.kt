package com.example.todolist

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val onRemove: (Int) -> Unit,
    private val onComplete : (Task) -> Unit,
    private val onOverdue: (Task) -> Unit,
    private val isCompletedFragment: Boolean = false
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.taskTitle)
        val tvDescription: TextView = view.findViewById(R.id.taskDescription)
        val tvDeadline: TextView = view.findViewById(R.id.taskDeadline)
        val completedBtn: AppCompatImageButton = view.findViewById(R.id.toCompleted)
        val updateBtn: AppCompatImageButton = view.findViewById(R.id.btnUpdate)
        val removeBtn: AppCompatImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.title
        holder.tvDescription.text = task.description
        holder.tvDeadline.text = task.deadline

        // Disable Completed button if task is completed
        if (task.isCompleted) {
            holder.completedBtn.isEnabled = false
            holder.completedBtn.alpha = 0.5f // Reduce opacity to show it's disabled
        } else {
            holder.completedBtn.isEnabled = true
            holder.completedBtn.alpha = 1.0f
        }

        // Check if the task is overdue
        if (isTaskOverdue(task.deadline)) {
            onOverdue(task)
            Handler(Looper.getMainLooper()).post {
                removeItem(position) // ✅ Runs after layout update
            }
        }


        holder.removeBtn.setOnClickListener {
            removeItem(position)
        }

        holder.completedBtn.setOnClickListener {
            task.isCompleted = true  // Mark task as completed
            notifyItemChanged(position) // Refresh UI to reflect the disabled button
            onComplete(task)
        }
        holder.updateBtn.setOnClickListener {
            showUpdateDialog(holder.itemView.context, position, task)
        }
    }


    override fun getItemCount(): Int = taskList.size
    private fun showUpdateDialog(context: Context, position: Int, task: Task) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Update Task")

        // Create input fields for title and description
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val titleInput = EditText(context).apply {
            hint = "Task Title"
            setText(task.title) // Prefill current title
        }

        val descriptionInput = EditText(context).apply {
            hint = "Task Description"
            setText(task.description) // Prefill current description
        }

        layout.addView(titleInput)
        layout.addView(descriptionInput)
        dialog.setView(layout)

        // Save button action
        dialog.setPositiveButton("Save") { _, _ ->
            task.title = titleInput.text.toString()
            task.description = descriptionInput.text.toString()
            notifyItemChanged(position) // Update UI
        }

        // Cancel button action
        dialog.setNegativeButton("Cancel", null)

        dialog.show()
    }
    // 🔹 Public function to remove an item
    fun removeItem(position: Int) {
        if (position < 0 || position >= taskList.size) {
            Log.e("TaskAdapter", "removeItem: invalid index $position, size ${taskList.size}")
            return
        }
        taskList.removeAt(position)
        notifyItemRemoved(position)
    }


    fun isTaskOverdue(dueDate: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Use the correct format
            val taskDate = dateFormat.parse(dueDate)  // Parse the date
            val currentDate = Date()  // Get the current date
            taskDate?.before(currentDate) ?: false  // Check if it's overdue
        } catch (e: Exception) {
            e.printStackTrace()
            false  // Default to false if parsing fails
        }
    }


}
