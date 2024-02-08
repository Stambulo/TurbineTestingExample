package com.stam.turbine

sealed class VmEvents {
    object OnLaunch: VmEvents()
}

sealed class VmState {
    object Waiting: VmState()
    object Running: VmState()
    data class Finished(val data: String): VmState()
}
