package com.example.to_dolist

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var taskListView: ListView
    private lateinit var taskInput: EditText
    private lateinit var addButton: Button
    private var taskList = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        taskInput = findViewById(R.id.taskInput)
        addButton = findViewById(R.id.addButton)
        taskListView = findViewById(R.id.taskListView)

        // Initialize database and DAO
        db = AppDatabase.getDatabase(this)
        taskDao = db.taskDao()

        // Set up adapter
        adapter = TaskAdapter(this, taskList) { task ->
            updateTask(task)
        }
        taskListView.adapter = adapter

        // Load tasks from database
        loadTasks()

        // Add task when button is clicked
        addButton.setOnClickListener {
            val taskName = taskInput.text.toString()
            if (taskName.isNotEmpty()) {
                addTask(taskName)
                taskInput.text.clear() // Clear the input field
            }
        }

        // Click listener to complete
        taskListView.setOnItemClickListener { _, _, position, _ ->
            val task = taskList[position]
            task.isCompleted = !task.isCompleted
            updateTask(task)
        }

        // Long click to delete a task
        taskListView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedTask = taskList[position]

            val options = if (selectedTask.isCompleted) {
                arrayOf("Mark as Incomplete", "Delete Task")
            } else {
                arrayOf("Mark as Complete", "Delete Task")
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Choose an Action")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // Toggle task completion status
                        selectedTask.isCompleted = !selectedTask.isCompleted
                        updateTask(selectedTask)
                    }
                    1 -> { // Delete Task
                        deleteTask(selectedTask)
                    }
                }
            }

            builder.show()
            true
        }
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            taskList = taskDao.getAllTasks().toMutableList()
            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    private fun addTask(taskName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val newTask = Task(taskName = taskName, isCompleted = false)
            taskDao.insertTask(newTask)
            taskList.add(newTask)
            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    private fun updateTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.updateTask(task)
            taskList.remove(task)
            if (task.isCompleted) {
                taskList.add(task)  // Move completed tasks to the bottom
            } else {
                taskList.add(0, task)  // Move incomplete tasks to the top
            }
            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    private fun deleteTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.deleteTask(task)
            taskList.remove(task)
            withContext(Dispatchers.Main) {
                updateUI()
            }
        }
    }

    private fun updateUI() {
        taskList.sortBy { it.isCompleted }
        adapter.updateTasks(taskList)
    }
}
