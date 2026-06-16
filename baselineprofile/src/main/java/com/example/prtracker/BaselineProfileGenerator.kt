package com.example.prtracker

import androidx.benchmark.macro.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
public class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.example.prtracker",
            maxIterations = 1
        ) {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            val scrollable = device.findObject(By.scrollable(true))
            scrollable?.fling(Direction.DOWN)
            device.waitForIdle()
            scrollable?.fling(Direction.UP)
            device.waitForIdle()

            val addFab = device.findObject(By.desc("Add Exercise"))
            addFab?.click()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()

            val settingsNav = device.findObject(By.desc("Settings"))
            settingsNav?.click()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()

            val goalsNav = device.findObject(By.desc("Goals"))
            goalsNav?.click()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
        }
    }
}
