package com.farro.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.farro.mvvmtodo.R
import com.farro.mvvmtodo.databinding.FragmentAddEditTaskBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val addEditTaskViewModel: AddEditTaskViewModel by viewModels()
    private val args by navArgs<AddEditTaskFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        val task = args.task

        binding.apply {
            editTextTaskName.setText(addEditTaskViewModel.taskName)
            checkBoxImportant.isChecked = addEditTaskViewModel.taskImportance
            checkBoxImportant.jumpDrawablesToCurrentState()

            textViewDateCreated.isVisible = addEditTaskViewModel.task != null
            textViewDateCreated.text =
                "Date Created: ${addEditTaskViewModel.task?.createdDateFormatted}"
        }
    }
}