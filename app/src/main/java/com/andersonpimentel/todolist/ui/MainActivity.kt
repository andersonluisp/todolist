package com.andersonpimentel.todolist.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.andersonpimentel.todolist.R
import com.andersonpimentel.todolist.databinding.ActivityMainBinding
import com.andersonpimentel.todolist.datasource.TaskDataSource
import com.andersonpimentel.todolist.ui.signin.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter by lazy {  TaskListAdapter()  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        verifyAuthentication();

        setSupportActionBar(binding.mainToolbar)

        binding.rvTasks.adapter = adapter
        //updateList()
        insertListeners()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var item2 = binding.mainToolbar.menu.getItem(0)
        when (item2.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                verifyAuthentication()
            }
        }
        return super.onOptionsItemSelected(item2)
    }

    private fun verifyAuthentication() {
        if (FirebaseAuth.getInstance().uid.isNullOrBlank()){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun insertListeners() {
        binding.fab.setOnClickListener {
            startActivityForResult(Intent(this, AddTaskActivity::class.java), CREATE_NEW_TASK)
        }

        adapter.listenerEdit = {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra(AddTaskActivity.TASK_ID, it.id)
            startActivityForResult(intent, CREATE_NEW_TASK)
        }

        adapter.listenerDelete = {
            TaskDataSource.deleteTask(it)
            updateList()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_NEW_TASK && resultCode == RESULT_OK) updateList()
    }

    private fun updateList(){
        //val list = TaskDataSource.getList()
        CoroutineScope(Dispatchers.IO).launch {
            val listFirebase = TaskDataSource.getListFirebase(FirebaseAuth.getInstance().uid)
            withContext(Main){
                binding.includeEmpty.emptyState.visibility = if(listFirebase.isEmpty()) View.VISIBLE
                else View.GONE
                adapter.submitList(listFirebase)
            }
        }
    }

    companion object{
        const val CREATE_NEW_TASK = 1000
    }
}