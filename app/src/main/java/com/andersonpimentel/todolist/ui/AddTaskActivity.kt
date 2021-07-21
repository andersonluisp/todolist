package com.andersonpimentel.todolist.ui

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.R
import com.andersonpimentel.todolist.databinding.ActivityTaskAddBinding
import com.andersonpimentel.todolist.extensions.format
import com.andersonpimentel.todolist.extensions.text
import com.andersonpimentel.todolist.model.AppRepository
import com.andersonpimentel.todolist.model.Task
import com.andersonpimentel.todolist.viewmodels.AddTaskViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddTaskActivity: AppCompatActivity() {

    private lateinit var binding: ActivityTaskAddBinding
    private lateinit var addTaskViewModel: AddTaskViewModel
    private var repository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addTaskViewModel = ViewModelProvider(
            this,
            AddTaskViewModel.AddTaskViewModelFactory(repository)
        ).get(AddTaskViewModel::class.java)

        setSupportActionBar(binding.addToolbar)
        binding.addToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        verifyEditTask()
        insertListeners()
    }

    private fun verifyEditTask(){
        if (intent.hasExtra(TASK)) {
            val task = intent.getParcelableExtra<Task>(TASK)!!
            binding.tilTitle.text = task.title
            binding.tilDescription.text = task.description
            binding.tilDate.text = task.date
            binding.tilHour.text = task.hour
            binding.btnNewTask.text = getString(R.string.label_update_task)
        }
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
                hour = binding.tilHour.text,
            )
            CoroutineScope(IO).launch {
                if (intent.hasExtra(TASK)) {
                    val newTaskId = intent.getParcelableExtra<Task>(TASK)!!.id
                    addTaskViewModel.insertTaskFirebase(task, newTaskId)
                } else {
                    if (intent.hasExtra(LAST_TASK_ID)) {
                        val newTaskId = intent.getStringExtra(LAST_TASK_ID)!!.toInt() + 1
                        addTaskViewModel.insertTaskFirebase(task, newTaskId)
                    }
                }
                withContext(Main) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }

        }
    }

    companion object {
        const val LAST_TASK_ID = "last_task_id"
        const val TASK = "task"
    }

}