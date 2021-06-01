package instrumentedTests.integration

import androidx.test.platform.app.InstrumentationRegistry
import com.gabr.gabc.kelo.firebase.GroupQueries
import com.gabr.gabc.kelo.firebase.UserQueries
import com.gabr.gabc.kelo.models.Group
import com.gabr.gabc.kelo.models.User
import com.gabr.gabc.kelo.utils.PermissionsSingleton
import com.google.firebase.FirebaseApp
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

/** Defines the User Instrumentation Tests */
@RunWith(BlockJUnit4ClassRunner::class)
class UserTest {
    private val q = UserQueries()
    private val group = Group("GROUP", "generic group", "EUR")
    private val user = User("USER_A", "createUser", 0)

    /** Initializes and creates Firebase needed data for the tests */
    @Before
    fun setUp() {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().context)
        runBlocking {
            GroupQueries().createGroup(group)
            UserQueries().createUser(user, group.id)
        }
    }

    /** Cleans up Firebase */
    @After
    fun clean() {
        runBlocking {
            UserQueries().deleteAllUsers(group.id)
            GroupQueries().deleteGroup(group.id)
        }
    }

    /** Tests the createUser function */
    @Test
    fun createUserSuccessfully() = runBlocking {
        val user = q.createUser(user, group.id)
        assertTrue(user != null && user.id == user.id)
    }

    /** Tests the getUser function */
    @Test
    fun readUserSuccessfully() = runBlocking {
        val result = q.getUser(user.id, group.id)
        assertTrue(
            result != null &&
            result.id == user.id
        )
    }

    /** Tests the getMostLazyUser function */
    @Test
    fun readMostLazyUserSuccessfully() = runBlocking {
        val u = User("CREATE_USER_2", "createUser", 30)
        val notToBeRetrievedUser = q.createUser(u, group.id)
        assertTrue(notToBeRetrievedUser != null)
        val result = q.getMostLazyUser(group.id)
        assertTrue(
            result != null &&
            result.id == user.id
        )
    }

    /** Tests the getRandomUser function */
    @Test
    fun readRandomUserSuccessfully() = runBlocking {
        q.createUser(User("RND_USER", "Random", 30), group.id)
        val rndUser = q.getRandomUser(group.id)
        assertTrue(rndUser != null)
    }

    /** Tests the getAllUsers function */
    @Test
    fun readAllUsersSuccessfully() = runBlocking {
        val result = q.getAllUsers(group.id)
        assertTrue(result != null && result.size == 1)
    }

    /** Tests the updateUser function */
    @Test
    fun updateUserSuccessfully() = runBlocking {
        val modified = User(user.id, "Gabriel", 56)
        val result = q.updateUser(modified, group.id)
        assertTrue(result)
    }

    /** Tests the deleteUser function */
    @Test
    fun deleteUserSuccessfully() = runBlocking {
        val result = q.deleteUser(user.id, group.id)
        assertTrue(result)
    }

    /** Tests the deleteAllUsers function */
    @Test
    fun deleteAllUsersSuccessfully() = runBlocking {
        val result = q.deleteAllUsers(group.id)
        assertTrue(result)
    }

    /** Tests the joinGroup function */
    @Test
    fun joinGroupSuccessfully() = runBlocking {
        val join = User("JOIN_USER", "joining user", 0)
        val result = q.joinGroup(group.id, join)
        assertTrue(result == join.id)
    }

    /** Tests the isUsernameAvailable function */
    @Test
    fun isUsernameAvailableSuccessfully() = runBlocking {
        val result = q.isUsernameAvailable(group.id, "nameIsFullyAvailable")
        assertTrue(result)
    }

    /** Tests the switching of the admin when the current admin leaves the group */
    @Test
    fun changeNewAdminOnAdminLeaveGroup() = runBlocking {
        q.createUser(User("RND_USER", "Random", 30), group.id)
        val success = q.updateNewAdmin(group.id)
        assertTrue(success)
    }

    /** Tests the listener function for users: attachListenerToAppForUserRemoved */
    @Test
    fun setListenerUserWasDeletedRedirectSuccessfully() = runBlocking {
        var success = false
        val uploadUser = User("USER_U", "Gabriel", 0)
        q.createUser(uploadUser, group.id)
        val result = q.attachListenerToAppForUserRemoved(group.id, uploadUser.id) { success = true }
        q.deleteUser(uploadUser.id, group.id)
        assertTrue(result != null)
        assertTrue(success)
    }

    /** Tests the getAllUsers function to set the admin of the group */
    @Test
    fun verifyIfUsersEmptyThenNewUserIsAdmin() = runBlocking {
        q.deleteUser(user.id, group.id)
        val users = q.getAllUsers(group.id)
        val newUser = User("USER_ADMIN", "Gabriel", 0, PermissionsSingleton.willUserBeAdmin(users))
        q.joinGroup(group.id, newUser)
        val shouldBeAdmin = q.getUser(newUser.id, group.id)
        assertTrue(shouldBeAdmin != null && shouldBeAdmin.isAdmin)
    }
}