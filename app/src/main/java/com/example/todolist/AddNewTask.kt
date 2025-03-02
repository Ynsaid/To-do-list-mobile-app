package com.example.todolist

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddNewTask : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_task)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.btnCancel)
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                tvSelectedDate.text = selectedDate
            }, year, month, day)

            datePicker.show()
        }

        btnConfirm.setOnClickListener {
            val title = etTitle.text.toString()
            val description = etDescription.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && selectedDate.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("title", title)
                resultIntent.putExtra("description", description)
                resultIntent.putExtra("deadline", selectedDate)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
