package instrumentedTests.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gabr.gabc.kelo.R
import com.gabr.gabc.kelo.constants.Constants
import com.gabr.gabc.kelo.firebase.ChoreQueries
import com.gabr.gabc.kelo.firebase.GroupQueries
import com.gabr.gabc.kelo.firebase.UserQueries
import com.gabr.gabc.kelo.main.MainActivity
import com.gabr.gabc.kelo.dataModels.Group
import com.gabr.gabc.kelo.dataModels.User
import com.google.firebase.FirebaseApp
import instrumentedTests.ui.utils.DisableAnimationsRule
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

/** Defines the Settings UI Tests */
//@RunWith(AndroidJUnit4::class)
class SettingsTest {
    /*private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @Rule(order = 0)
    @JvmField
    val disabledAnimations = DisableAnimationsRule()

    @Rule(order = 1)
    @JvmField
    val activityScenario = ActivityScenarioRule<MainActivity>(intent)

    companion object {
        private val group = Group("UI_GROUP", "generic group", "EUR")
        private val user = User("UI_USER", "Gabriel", 0, true)

        /** Initializes and creates Firebase needed data for the tests */
        @JvmStatic
        @BeforeClass
        fun setUpFirebase() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            FirebaseApp.initializeApp(context)
            runBlocking { GroupQueries().createGroup(group) }

            // Sets shared preferences for activities to use
            val gr = context.getSharedPreferences(Constants.GROUP_ID, Context.MODE_PRIVATE)
            with (gr.edit()) {
                putString(Constants.GROUP_ID, group.id)
                commit()
            }

            val us =  context.getSharedPreferences(Constants.USER_ID, Context.MODE_PRIVATE)
            with (us.edit()) {
                putString(Constants.USER_ID, user.id)
                commit()
            }
        }

        /** Cleans up Firebase */
        @JvmStatic
        @AfterClass
        fun cleanFirebase() {
            runBlocking {
                UserQueries().deleteAllUsers(group.id)
                ChoreQueries().deleteAllChores(group.id)
                GroupQueries().deleteGroup(group.id)
            }
        }
    }

    /** Function called before each test to go to the desired activity */
    @Before
    fun pressSettingsTab() {
        runBlocking { UserQueries().createUser(user, group.id) }

        onView(withId(R.id.settings_menu)).perform(click())
    }

    /** Cleans up the user for every test */
    @After
    fun cleanUser() {
        runBlocking { UserQueries().deleteAllUsers(group.id) }
    }

    /**
     * Test that verifies that leaving a group automatically removes the user and sets the current
     * user to the Welcome Page
     * */
    @Test
    fun dialogOnLeaveGroupAppearsAndRedirectsToWelcomePage() {
        onView(withId(R.id.settingsExitGroupButton)).perform(scrollTo()).perform(click())
        onView(withText(R.string.settings_dialog_msg_leave_group)).inRoot(isDialog()).check(matches(isDisplayed()))
        clickOnDialogAndCheckReturnToWelcomeActivity()
    }

    /**
     * Test that verifies that removing a group automatically removes the user and sets the current
     * user to the Welcome Page
     * */
    @Test
    fun dialogOnDeleteGroupAppearsAndRedirectsToWelcomePage() {
        onView(withId(R.id.settingsRemoveGroupButton)).perform(scrollTo()).perform(click())
        onView(withText(R.string.settings_dialog_msg_delete_group)).inRoot(isDialog()).check(matches(isDisplayed()))
        clickOnDialogAndCheckReturnToWelcomeActivity()
    }

    /** Test that verifies that a dialog appears upon clicking edit user setting */
    @Test
    fun dialogOnEditUserAppears() {
        onView(withId(R.id.settingsUpdateUserButton)).perform(scrollTo()).perform(click())
        onView(withText(R.string.settings_change_user_name_message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    /** Test that verifies that a dialog appears upon clicking edit group setting */
    @Test
    fun dialogOnEditGroupNameAppears() {
        onView(withId(R.id.settingsUpdateGroupButton)).perform(scrollTo()).perform(click())
        onView(withText(R.string.settings_change_group_name_message)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    private fun clickOnDialogAndCheckReturnToWelcomeActivity() {
        onView(withId(android.R.id.button1)).perform(click())
        Thread.sleep(1000)
        onView(withText(R.string.welcome_to_kelo)).check(matches(isDisplayed()))
    }*/
}