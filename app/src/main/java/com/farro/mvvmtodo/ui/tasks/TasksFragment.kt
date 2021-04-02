package com.farro.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.farro.mvvmtodo.R
import com.farro.mvvmtodo.data.SortOrder
import com.farro.mvvmtodo.data.Task
import com.farro.mvvmtodo.databinding.FragmentTasksBinding
import com.farro.mvvmtodo.util.exhaustive
import com.farro.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val tasksViewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val tasksAdapter = TasksAdapter(this)

        tasksViewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        binding.apply {
            recyclerViewTasks.apply {
                setHasFixedSize(true)
                adapter = tasksAdapter
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // We do not need to move the item around
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    tasksViewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                tasksViewModel.onAddNewTaskClicked()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            tasksViewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TasksEvent.ShowUndoDeleteMessage -> {
                        Snackbar.make(
                            requireView(),
                            "Task deleted",
                            Snackbar.LENGTH_LONG
                        ).setAction(
                            "UNDO"
                        ) {
                            tasksViewModel.onUndoDeleteClicked(event.task)
                        }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(title = "New Task")
                        findNavController().navigate(R.id.action_tasksFragment_to_addEditTaskFragment)
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                event.task,
                                "Edit Task #${event.task.id}"
                            )
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(task: Task) {
        tasksViewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        tasksViewModel.onTaskCheckChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            tasksViewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                tasksViewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                tasksViewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_by_date_created -> {
                tasksViewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                tasksViewModel.onHideCompletedSelected(item.isChecked)
                true
            }

            R.id.action_delete_all_completed_tasks -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}