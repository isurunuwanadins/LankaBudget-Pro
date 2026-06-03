package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LankaBudget Pro", appName)
  }

  @Test
  fun `launch main activity`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java)
    controller.setup() // creates, starts, resumes the activity
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }
}
