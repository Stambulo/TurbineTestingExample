package com.stam.turbine

import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

@RunWith(JUnit4::class)
class TurbineViewModelTest {
    // 1.
    @Mock
    lateinit var heavyComputation: HeavyComputationTemplate

    // 2.
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Test
    fun `Given the sut is initialized, then it waits for event`() {

        // 3.
        val sut = ExampleViewModel(heavyComputation)

        // 4.
        assertTrue(sut.vmState.value == VmState.Waiting)
    }
}
