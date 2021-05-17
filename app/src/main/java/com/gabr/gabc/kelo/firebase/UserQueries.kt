package com.gabr.gabc.kelo.firebase

import com.gabr.gabc.kelo.constants.UserFields
import com.gabr.gabc.kelo.constants.fbGroupsCollection
import com.gabr.gabc.kelo.constants.fbUsersCollection
import com.gabr.gabc.kelo.models.Group
import com.gabr.gabc.kelo.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.random.Random

class UserQueries {

    private var instance: FirebaseFirestore = Firebase.firestore

    /**
     * Function that makes a [User] join an existing [Group], validating if the name that the user
     * has filled is already taken in the requested [Group]
     *
     * @param groupId: group where the user would like to join
     * @param user: user to join
     * @return Int determining the success:
     *      -1: generic error
     *      -2: username is already taken
     *      -3: group does not exist
     *      else: user id
     * */
    suspend fun joinGroup(groupId: String, user: User): String {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId).get().await()
            if (!ref.exists()) "-3"
            else {
                if (isUsernameAvailable(groupId, user.name)) {
                    val res = createUser(user, groupId)
                    res?.id ?: "-1"
                } else {
                    "-2"
                }
            }
        } catch (e: Exception) {
            "-1"
        }
    }

    /**
     * Function that creates a [User]
     *
     * @param user: user to be created in a certain group
     * @param groupId: group id in which the user is
     * @return returning User
     * */
    suspend fun createUser(user: User, groupId: String): User? {
        return try {
            val ref = if (user.id != "") {
                instance.collection(fbGroupsCollection).document(groupId)
                        .collection(fbUsersCollection)
                        .document(user.id)
            } else {
                instance.collection(fbGroupsCollection).document(groupId)
                        .collection(fbUsersCollection)
                        .document()
            }
            ref.set(user.toMap()).await()
            user.id = ref.id
            user
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Function that retrieves a desired [User] with a given group id
     *
     * @param userId: id to get the user
     * @param groupId: group id in which the chore is
     * @return [User] containing the information
     * */
    suspend fun getUser(userId: String, groupId: String): User? {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId)
                    .collection(fbUsersCollection).document(userId)
                    .get().await()
            ref.toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Function that retrieves all users in a group
     *
     * @param groupId: group id in which the users are
     * @return ArrayList<User?>? containing the users of the group
     * */
    suspend fun getAllUsers(groupId: String): ArrayList<User?>? {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId)
                .collection(fbUsersCollection)
                .get().await()
            val users: ArrayList<User?> = arrayListOf()
            val data = ref.documents
            for (user in data) {
                users.add(user.toObject<User>())
            }
            users
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Function that updates a desired [User] with a given group id
     *
     * @param user: updated user
     * @param groupId: group id in which the chore is
     * @return Boolean that returns true if query was successful
     * */
    suspend fun updateUser(user: User, groupId: String): Boolean {
        return try {
            instance.collection(fbGroupsCollection).document(groupId)
                    .collection(fbUsersCollection).document(user.id)
                    .update(user.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Function that deletes a desired [User] with a given group id
     *
     * @param userId: ID of the to be deleted user
     * @param groupId: group id in which the chore is
     * @return Boolean that returns true if query was successful
     * */
    suspend fun deleteUser(userId: String, groupId: String): Boolean {
        return try {
            instance.collection(fbGroupsCollection).document(groupId)
                    .collection(fbUsersCollection).document(userId)
                    .delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Helper function that deletes all users in a group
     *
     * @param groupId: group id in which the users are
     * @return Boolean that returns true if query was successful
     * */
    suspend fun deleteAllUsers(groupId: String): Boolean {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId)
                    .collection(fbUsersCollection).get().await()
            ref.documents.forEach {
                val user = it.toObject<User>()
                user?.let { u -> deleteUser(u.id, groupId) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the user with the least amount of points of the group
     *
     * @param groupId: group ID from Firebase
     * */
    suspend fun getMostLazyUser(groupId: String): User? {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId)
                .collection(fbUsersCollection)
                .orderBy(UserFields.points, Query.Direction.ASCENDING).limit(1)
                .get().await()
            ref.documents.first().toObject<User>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets a random user from an array
     *
     * @param users: array of users from [getAllUsers]
     * @return randomly selected [User]
     * */
    fun getRandomUser(users: ArrayList<User?>?): User? {
        return if (users != null) users[Random.nextInt(0, users.size)] else null
    }

    /**
     * Function that verifies if the user name of a [User] is already taken in a certain [Group]
     *
     * @param groupId: group ID from Firebase
     * @param userName: name of the user
     * @return Boolean of success
     * */
    suspend fun isUsernameAvailable(groupId: String, userName: String): Boolean {
        return try {
            val obs = instance.collection(fbGroupsCollection)
                .document(groupId)
                .collection(fbUsersCollection)
                .whereEqualTo(UserFields.name, userName)
                .get()
                .await()
            return obs.documents.size == 0
        } catch (e: Exception) {
            false
        }
    }
}