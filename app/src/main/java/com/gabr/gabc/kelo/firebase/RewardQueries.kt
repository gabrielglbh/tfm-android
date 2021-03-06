package com.gabr.gabc.kelo.firebase

import com.gabr.gabc.kelo.constants.Constants
import com.gabr.gabc.kelo.dataModels.Reward
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception

/** Class that holds all the necessary Reward Queries to retrieve, read, update or delete chores from Firebase */
class RewardQueries {

    private var instance: FirebaseFirestore = Firebase.firestore
    private val fbGroupsCollection = Constants.fbGroupsCollection
    private val fbRewardsSubCollection = Constants.fbRewardsSubCollection

    /**
     * Function that creates a [Reward] in an existing Group
     *
     * @param reward: reward to be created
     * @param groupId: group ID to make the reward in
     * @return Boolean of success
     * */
    suspend fun createReward(reward: Reward, groupId: String): Reward? {
        return try {
            reward.id?.let {
                val ref = if (it != "") {
                    instance.collection(fbGroupsCollection).document(groupId)
                        .collection(fbRewardsSubCollection).document(it)
                    } else {
                        instance.collection(fbGroupsCollection).document(groupId)
                            .collection(fbRewardsSubCollection).document()
                    }
                ref.set((reward.toMap())).await()
                reward
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Function that retrieves all rewards in a group
     *
     * @param groupId: group id in which the users are
     * @return ArrayList<Reward>? containing the rewards of the group
     * */
    suspend fun getAllRewards(groupId: String): ArrayList<Reward>? {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId)
                .collection(fbRewardsSubCollection)
                .get().await()
            val rewards = arrayListOf<Reward>()
            val data = ref.documents
            for (user in data) {
                user.toObject<Reward>()?.let { rewards.add(it) }
            }
            rewards
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Function that updates a desired [Reward] with a given group id
     *
     * @param reward: updated reward
     * @param groupId: group id in which the chore is
     * @return Boolean that returns true if query was successful
     * */
    suspend fun updateReward(reward: Reward, groupId: String): Boolean {
        return try {
            reward.id?.let {
                instance.collection(fbGroupsCollection).document(groupId)
                    .collection(fbRewardsSubCollection).document(it)
                    .update(reward.toMap())
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Function that deletes a desired [Reward] with a given group id
     *
     * @param rewardId: ID of the to be deleted reward
     * @param groupId: group id in which the chore is
     * @return Boolean that returns true if query was successful
     * */
    suspend fun deleteReward(rewardId: String, groupId: String): Boolean {
        return try {
            instance.collection(fbGroupsCollection).document(groupId)
                .collection(fbRewardsSubCollection).document(rewardId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Helper function that deletes all rewards in a group
     *
     * @param groupId: group id in which the rewards are
     * @return Boolean that returns true if query was successful
     * */
    suspend fun deleteAllRewards(groupId: String): Boolean {
        return try {
            val ref = instance.collection(fbGroupsCollection).document(groupId)
                .collection(fbRewardsSubCollection).get().await()
            ref.documents.forEach {
                val reward = it.toObject<Reward>()
                reward?.let { r -> r.id?.let { it1 -> deleteReward(it1, groupId) } }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}