package com.example.to_dolist
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.widget.BaseAdapter

class TaskAdapter(
    private val context: Context,
    private var taskList: MutableList<Task>,
    private val onTaskCheckedChange: (Task) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = taskList.size

    override fun getItem(position: Int): Task = taskList[position]

    override fun getItemId(position: Int): Long = taskList[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_list_item, parent, false)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val taskName = view.findViewById<TextView>(R.id.taskName)

        val task = taskList[position]
        taskName.text = task.taskName

        // Detach listener
        checkBox.setOnCheckedChangeListener(null)

        checkBox.isChecked = task.isCompleted

        // Apply strikethrough and grey text if the task is completed
        if (task.isCompleted) {
            taskName.paintFlags = taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            taskName.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        } else {
            taskName.paintFlags = taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            taskName.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        // Re-attach listener
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            onTaskCheckedChange(task)
        }

        return view
    }

    fun updateTasks(newTaskList: MutableList<Task>) {
        taskList = newTaskList
        notifyDataSetChanged()
    }
}
