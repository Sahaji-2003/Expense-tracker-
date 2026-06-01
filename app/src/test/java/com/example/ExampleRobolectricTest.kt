package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.MainContainerScreen
import com.example.ui.screens.AddManageHubScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule 
  val composeTestRule = createComposeRule()

  @Test
  fun testAppName() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Expense Planner", appName)
  }

  private fun createTestViewModel(app: Application): ExpenseViewModel {
    val database = com.example.data.local.AppDatabase.getDatabase(app)
    val repository = com.example.data.repository.ExpenseRepository(
      database.categoryDao(),
      database.expenseDao(),
      database.userBudgetDao(),
      database.recurringExpenseDao(),
      database.userDao()
    )
    return ExpenseViewModel(app, repository)
  }

  @Test
  fun testMainContainerScreen_rendersSuccessfully() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = createTestViewModel(app)

    composeTestRule.setContent {
      MyApplicationTheme {
        MainContainerScreen(viewModel = viewModel)
      }
    }

    // Since loggedInUser is null initially, it should display the LoginScreen
    composeTestRule.onNodeWithTag("login_screen").assertExists()
  }

  @Test
  fun testAddManageHubScreen_rendersSuccessfully() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = createTestViewModel(app)

    composeTestRule.setContent {
      MyApplicationTheme {
        AddManageHubScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("add_manage_hub_screen").assertExists()
  }

  @Test
  fun testAnalyticsScreen_rendersSuccessfully() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = createTestViewModel(app)

    composeTestRule.setContent {
      MyApplicationTheme {
        AnalyticsScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("analytics_screen").assertExists()
  }
}


