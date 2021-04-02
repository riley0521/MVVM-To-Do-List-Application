package com.farro.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.farro.mvvmtodo.data.PreferencesManager
import com.farro.mvvmtodo.data.SortOrder
import com.farro.mvvmtodo.data.Task
import com.farro.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    // With DataStore Preference Manager
    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

    // Methods that you can call in the corresponding fragment that uses this ViewModel.
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedSelected(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteMessage(task))
    }

    fun onUndoDeleteClicked(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClicked() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    // We will use sealed class to hold data for us that we can send in channel
    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteMessage(val task: Task) : TasksEvent()
    }

    // Without DataStore Preference Manager
//    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
//    val hideCompleted = MutableStateFlow(false)
//
//    private val tasksFlow = combine(
//        searchQuery,
//        sortOrder,
//        hideCompleted
//    ) { query, sortOrder, hideCompleted ->
//        Triple(query, sortOrder, hideCompleted)
//    }.flatMapLatest { (query, sortOrder, hideCompleted) ->
//        taskDao.getTasks(query, sortOrder, hideCompleted)
//    }
}

