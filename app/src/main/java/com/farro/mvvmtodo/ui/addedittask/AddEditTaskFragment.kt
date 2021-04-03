package com.farro.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.farro.mvvmtodo.R
import com.farro.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.farro.mvvmtodo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

const val ADD_EDIT_REQUEST = "add_edit_request"
const val ADD_EDIT_RESULT = "add_edit_result"

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

            editTextTaskName.addTextChangedListener {
                addEditTaskViewModel.taskName = it.toString()
            }

            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                addEditTaskViewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                addEditTaskViewModel.onSaveClicked()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            addEditTaskViewModel.addEditTasksEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            ADD_EDIT_REQUEST,
                            bundleOf(ADD_EDIT_RESULT to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }
}