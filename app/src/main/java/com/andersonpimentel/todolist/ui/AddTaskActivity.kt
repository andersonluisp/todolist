package com.andersonpimentel.todolist.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.andersonpimentel.todolist.databinding.ActivityTaskAddBinding
import com.andersonpimentel.todolist.datasource.TaskDataSource
import com.andersonpimentel.todolist.extensions.format
import com.andersonpimentel.todolist.extensions.text
import com.andersonpimentel.todolist.model.Task
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class AddTaskActivity: AppCompatActivity() {

    private lateinit var binding: ActivityTaskAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        insertListeners()

    }

    private fun insertListeners() {
        binding.tilDate.editText?.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                val timeZone = TimeZone.getDefault()
                val offset = timeZone.getOffset(Date().time) * -1
                binding.tilDate.text = Date(it + offset).format()
            }
            datePicker.show(supportFragmentManager, "DATE_PICKER_TAG")
        }

        binding.tilHour.editText?.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build()
            timePicker.addOnPositiveButtonClickListener {
                binding.tilHour.text = "${String.format("%02d",timePicker.hour)}:" +
                        String.format("%02d",timePicker.minute)

            }
            timePicker.show(supportFragmentManager, null)
        }

        binding.btnCancel.setOnClickListener{
            finish()
        }

        binding.btnNewTask.setOnClickListener {
            val task = Task(
                title = binding.tilTitle.text,
                description = binding.tilDescription.text,
                date = binding.tilDate.text,
                hour = binding.tilHour.text
            )
            TaskDataSource.insertTask(task)
            Log.e("TAG", TaskDataSource.getList().toString())
        }
    }

}