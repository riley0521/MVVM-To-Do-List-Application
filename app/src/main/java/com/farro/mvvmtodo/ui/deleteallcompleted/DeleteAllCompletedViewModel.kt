package com.farro.mvvmtodo.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.farro.mvvmtodo.data.TaskDao
import com.farro.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun onConfirmClicked() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }

}