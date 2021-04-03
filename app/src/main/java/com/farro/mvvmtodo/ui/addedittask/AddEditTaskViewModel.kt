package com.farro.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farro.mvvmtodo.data.Task
import com.farro.mvvmtodo.data.TaskDao
import com.farro.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.farro.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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

    private val addEditTasksEventChannel = Channel<AddEditTaskEvent>()
    val addEditTasksEvent = addEditTasksEventChannel.receiveAsFlow()

    fun onSaveClicked() {
        if (taskName.isBlank()) {
            // send event to fragment that the taskName is empty.
            showInvalidInputMessage("Name cannot be empty.")
            // return here to exit the method.
            return
        }

        if (task != null) {
            // update the selected task
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            // add new task
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(message: String) = viewModelScope.launch {
        addEditTasksEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(message))
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insert(newTask)
        // Navigate back to To-Do List
        addEditTasksEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        // Navigate back to To-Do List
        addEditTasksEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}