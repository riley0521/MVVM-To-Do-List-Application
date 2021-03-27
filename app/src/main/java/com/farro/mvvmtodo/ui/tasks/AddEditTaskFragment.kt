package com.farro.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.farro.mvvmtodo.R
import com.farro.mvvmtodo.databinding.FragmentAddEditTaskBinding
import kotlinx.android.synthetic.main.fragment_add_edit_task.*

class AddEditTaskFragment: Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: TasksViewModel by viewModels()
    private val args by navArgs<AddEditTaskFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        val task = args.task

        binding.apply {
            editTextTaskName.setText(task.name)
            check_box_important.isChecked = task.important
            textViewDateCreated.text = task.createdDateFormatted
        }
    }
}