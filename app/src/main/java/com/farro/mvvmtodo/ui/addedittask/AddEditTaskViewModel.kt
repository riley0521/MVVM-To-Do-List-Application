package com.farro.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.farro.mvvmtodo.data.Task
import com.farro.mvvmtodo.data.TaskDao

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TASK = "task"
        private const val TASK_NAME = "taskName"
        private const val IS_IMPORTANT = "taskImportance"
    }

    val task = state.get<Task>(TASK)

    var taskName = state.get<String>(TASK_NAME) ?: task?.name ?: ""
        set(value) {
            field = value
            state.set(TASK_NAME, value)
        }

    var taskImportance = state.get<Boolean>(IS_IMPORTANT) ?: task?.important ?: false
        set(value) {
            field = value
            state.set(IS_IMPORTANT, value)
        }
}