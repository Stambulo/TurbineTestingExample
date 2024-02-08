package com.stam.turbine

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
class TurbineWaitsViewModelTest {

    // 1.
    @Mock
    lateinit var heavyComputation: HeavyComputationTemplate

    // 2.
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        // setting up test dispatcher as main dispatcher for coroutines
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        // removing the test dispatcher
        Dispatchers.resetMain()
    }

    // Testing one StateFlow
    @Test
    fun `Given the ViewModel waits - When the event OnLaunch comes, then execute heavy computation with result`() =
        // 1.
        runTest {
            // ARRANGE
            val expectedString = "Result"
            // 2.
            heavyComputation.stub {
                onBlocking { doComputation() } doAnswer { expectedString }
            }
            val sut = ExampleViewModel(heavyComputation)

            // 3.
            sut.vmState.test {

                // ACTION
                sut.onEvent(VmEvents.OnLaunch)

                // CHECK
                // 4.
                Assert.assertEquals(VmState.Waiting, awaitItem())
                Assert.assertEquals(VmState.Running, awaitItem())
                Assert.assertEquals(VmState.Finished(expectedString), awaitItem())
                Assert.assertEquals(VmState.Waiting, awaitItem())

                // the test will finish on its own, because of lambda usage
            }
        }

    // Testing multiple StateFlow
    @Test
    fun `Given the ViewModel waits - When the event OnLaunch comes, then both computations runs successfully`() =
        runTest {
            turbineScope {
                // ARRANGE
                val expectedString = "Result"
                heavyComputation.stub {
                    onBlocking { doComputation() } doAnswer { expectedString }
                }

                val sut = StateFlowViewModel(heavyComputation)

                val firstStateReceiver = sut.vmState.testIn(backgroundScope)
                val secondStateReceiver = sut.secondVmState.testIn(backgroundScope)

                // ACTION
                sut.onEvent(VmEvents.OnLaunch)

                // CHECK
                Assert.assertEquals(VmState.Waiting, firstStateReceiver.awaitItem())
                Assert.assertEquals(VmState.Waiting, secondStateReceiver.awaitItem())

                Assert.assertEquals(VmState.Running, firstStateReceiver.awaitItem())
                Assert.assertEquals(VmState.Running, secondStateReceiver.awaitItem())

                Assert.assertEquals(VmState.Finished(expectedString), firstStateReceiver.awaitItem())
                Assert.assertEquals(VmState.Finished(expectedString), secondStateReceiver.awaitItem())

                Assert.assertEquals(VmState.Waiting, firstStateReceiver.awaitItem())
                Assert.assertEquals(VmState.Waiting, secondStateReceiver.awaitItem())

                firstStateReceiver.cancel()
                secondStateReceiver.cancel()
            }
        }
}
