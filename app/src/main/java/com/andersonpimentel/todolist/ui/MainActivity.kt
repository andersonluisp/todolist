package com.andersonpimentel.todolist.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.andersonpimentel.todolist.R
import com.andersonpimentel.todolist.databinding.ActivityMainBinding
import com.andersonpimentel.todolist.model.AppRepository
import com.andersonpimentel.todolist.ui.signin.LoginActivity
import com.andersonpimentel.todolist.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter by lazy {  TaskListAdapter()  }
    private lateinit var mainViewModel: MainViewModel
    private var repository = AppRepository()
    private var lastTaskId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModel.MainViewModelFactory(repository)
        ).get(MainViewModel::class.java)

        setSupportActionBar(binding.mainToolbar)

        verifyAuthentication()
        updateList()
        insertListeners()
        binding.rvTasks.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val item2 = binding.mainToolbar.menu.getItem(0)
        when (item2.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                verifyAuthentication()
            }
        }
        return super.onOptionsItemSelected(item2)
    }

    private fun verifyAuthentication() {
        if (mainViewModel.verifyAuthentication()){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else{
            mainViewModel.getTaskListFirebase()
        }
    }

    private fun insertListeners() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra(AddTaskActivity.LAST_TASK_ID, lastTaskId.toString())
            startActivityForResult(intent, CREATE_NEW_TASK)
        }
        adapter.listenerEdit = { task ->
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra(AddTaskActivity.TASK, task)
            startActivityForResult(intent, CREATE_NEW_TASK)
        }
        adapter.listenerDelete = {
            mainViewModel.removeTaskFirebase(it.id)
        }
    }

    private fun updateList(){
        binding.includeEmpty.emptyState.visibility = View.VISIBLE
        mainViewModel.listTask.observe(this){ listTasks ->
            if(mainViewModel.verifyListIsEmpty(listTasks)){
                binding.includeEmpty.emptyState.visibility = View.VISIBLE
            }else{
                lastTaskId = listTasks[listTasks.lastIndex].id
                binding.includeEmpty.emptyState.visibility = View.GONE
            }
            adapter.submitList(listTasks)
            adapter.notifyDataSetChanged()
        }
    }

    companion object{
        const val CREATE_NEW_TASK = 1000
    }
}