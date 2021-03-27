package com.farro.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.farro.mvvmtodo.R
import com.farro.mvvmtodo.data.Task
import com.farro.mvvmtodo.databinding.FragmentTasksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val tasksAdapter = TasksAdapter(this)

        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        binding.apply {
            recyclerViewTasks.apply {
                setHasFixedSize(true)
                adapter = tasksAdapter
            }
        }
    }

    override fun onItemClick(task: Task) {
        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(task)
        findNavController().navigate(action)
    }
}