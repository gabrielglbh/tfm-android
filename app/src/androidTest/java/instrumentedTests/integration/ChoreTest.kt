package instrumentedTests.integration

import androidx.test.platform.app.InstrumentationRegistry
import com.gabr.gabc.kelo.firebase.ChoreQueries
import com.gabr.gabc.kelo.firebase.GroupQueries
import com.gabr.gabc.kelo.firebase.UserQueries
import com.gabr.gabc.kelo.dataModels.Chore
import com.gabr.gabc.kelo.dataModels.Group
import com.gabr.gabc.kelo.dataModels.User
import com.google.firebase.FirebaseApp
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import java.util.*

/** Defines the Chore Instrumentation Tests */
@RunWith(BlockJUnit4ClassRunner::class)
class ChoreTest {
    private val q = ChoreQueries()
    private val group = Group("GROUP", "generic group", "EUR")
    private val user = User("USER", "generic user", 20)
    private val chore = Chore("CHORE_C", "CHORE_C", "", "USER", Calendar.getInstance().time, 30)

    /** Initializes and creates Firebase needed data for the tests */
    @Before
    fun setUp() {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().context)
        runBlocking {
            GroupQueries().createGroup(group)
            UserQueries().createUser(user, group.id)
            ChoreQueries().createChore(chore, group.id)
        }
    }

    /** Cleans up Firebase */
    @After
    fun clean() {
        runBlocking {
            ChoreQueries().deleteAllChores(group.id)
            UserQueries().deleteAllUsers(group.id)
            GroupQueries().deleteGroup(group.id)
        }
    }

    /** Tests the createChore function */
    @Test
    fun createChoreSuccessfully() = runBlocking {
        val c = q.createChore(chore, group.id)
        assertTrue(c != null && c.id == chore.id)
    }

    /** Tests the createChore function when the group does not exist */
    @Test
    fun createChoreWhenGroupDoesNotExistSuccessfully() = runBlocking {
        val c = q.createChore(chore, "")
        assertTrue(c == null)
    }

    /** Tests the getChore function */
    @Test
    fun readChoreSuccessfully() = runBlocking {
        val result = q.getChore(chore.id!!, group.id)
        assertTrue(
            result != null &&
            result.id == chore.id
        )
    }

    /** Tests the getChore function when the group does not exist*/
    @Test
    fun readChoreNotSuccessfully() = runBlocking {
        val result = q.getChore("NON_EXISTING_CHORE", group.id)
        assertTrue(result == null)
    }

    /** Tests the getAllChores function */
    @Test
    fun readAllChoresSuccessfully() = runBlocking {
        val uploadChore = Chore("CHORE_U", "Lavar los platos", "", "sadca09sd99aaa")
        q.createChore(uploadChore, group.id)
        val chores = q.getAllChores(group.id)
        assertTrue(chores != null)
        assertTrue(chores?.size == 2)
        assertTrue(chores!![0].expiration!!.time < chores[1].expiration!!.time)
    }

    /** Tests the updateChore function */
    @Test
    fun updateChoreSuccessfully() = runBlocking {
        val modified = Chore(chore.id, "Lavar Platos", "", "Gabriel", Calendar.getInstance().time)
        val result = q.updateChore(modified, group.id)
        assertTrue(result)
    }

    /** Tests the updateChore function when the group does not exist */
    @Test
    fun updateChoreWhenGroupDoesNotExistSuccessfully() = runBlocking {
        val modified = Chore(chore.id, "Lavar Platos", "", "Gabriel", Calendar.getInstance().time)
        val result = q.updateChore(modified, "")
        assertFalse(result)
    }

    /** Tests the deleteChore function */
    @Test
    fun deleteChoreSuccessfully() = runBlocking {
        val result = q.deleteChore(chore.id!!, group.id)
        assertTrue(result)
    }

    /** Tests the deleteAllChores function */
    @Test
    fun deleteAllChoresSuccessfully() = runBlocking {
        val result = q.deleteAllChores(group.id)
        assertTrue(result)
    }

    /** Tests the deleteAllChores function when the group does not exist */
    @Test
    fun deleteAllChoresWhenGroupDoesNotExistSuccessfully() = runBlocking {
        val result = q.deleteAllChores("")
        assertFalse(result)
    }

    /** Tests the completeChore function */
    @Test
    fun completeChoreSuccessfully() = runBlocking {
        val success = q.completeChore(chore, group.id)
        val user = UserQueries().getUser(user.id, group.id)
        assertTrue(success && user != null && user.points == 50)
    }

    /** Tests the completeChore function when the user does not exist */
    @Test
    fun completeChoreWhenUserDoesNotExistSuccessfully() = runBlocking {
        val c = q.createChore(Chore("NEW_CHORE", "Tidy Up", "", ""), group.id)
        val success = c?.let { q.completeChore(it, group.id) }
        assertTrue(success != null && !success)
    }
}