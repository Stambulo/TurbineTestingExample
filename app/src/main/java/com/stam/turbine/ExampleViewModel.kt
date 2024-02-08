package com.stam.turbine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// interface for heavy computation job, which returns the string
interface HeavyComputationTemplate {
    suspend fun doComputation(): String
}

// Hilt annotations are not needed in the example
// because we will manually inject mocks - in real app with hilt, they are essential
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val computationRepo: HeavyComputationTemplate,
) : ViewModel() {

    private val _vmState: MutableStateFlow<VmState> = MutableStateFlow(VmState.Waiting)
    val vmState = _vmState.asStateFlow()

    fun onEvent(event: VmEvents): Job = when (event) {
        VmEvents.OnLaunch -> onLaunch()
    }

    private fun onLaunch() = viewModelScope.launch(Dispatchers.Main) {
        _vmState.value = VmState.Running
        val result = computationRepo.doComputation()
        _vmState.value = VmState.Finished(result)
        _vmState.value = VmState.Waiting
    }
}
