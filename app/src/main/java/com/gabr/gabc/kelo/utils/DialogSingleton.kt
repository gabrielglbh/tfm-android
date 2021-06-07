package com.gabr.gabc.kelo.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.gabr.gabc.kelo.R
import com.gabr.gabc.kelo.dataModels.User
import com.gabr.gabc.kelo.dataModels.Group
import com.gabr.gabc.kelo.firebase.GroupQueries
import com.gabr.gabc.kelo.firebase.UserQueries
import com.gabr.gabc.kelo.welcome.WelcomePageFunctions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Singleton instance with helper functions to create Dialogs across all code */
object DialogSingleton {
    /**
     * Creates and shows a customizable dialog with a various parameters:
     *
     * @param activity: current activity to show the dialog on
     * @param title: title of the dialog
     * @param message: message to show in the dialog body
     * @param positiveButton: text to display in the positive button
     * @param onPositive: function that executes when the positive button is pressed
     * */
    fun createCustomDialog(activity: Activity, title: String, message: String,
                           positiveButton: String, onPositive: () -> Unit) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                onPositive()
                dialog.dismiss()
            }
            .setNegativeButton(
                activity.baseContext.getString(R.string.settings_dialog_btn_negative)
            ) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    /**
     * Creates and shows a customizable dialog with a various parameters. It also initializes all the
     * controllers needed for the custom view to function properly.
     *
     * @param activity: current activity to show the dialog on
     * @param context: current context
     * @param user: user to be updated
     * @param group: group to be updated
     * */
    fun createDialogWithEditTextField(activity: Activity, context: Context,
                                      user: User? = null, group: Group? = null,
                                      onDismiss: ((title: String) -> Unit)? = null) {
        val dialogView = activity.layoutInflater.inflate(R.layout.update_content_dialog, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()

        val label: TextView = dialogView.findViewById(R.id.settingsChangeContentLabel)
        val layout: TextInputLayout = dialogView.findViewById(R.id.settingsChangeContentLayout)
        val edit: TextInputEditText = dialogView.findViewById(R.id.settingsChangeContentEditText)

        edit.doOnTextChanged { _, _, _, _ -> UtilsSingleton.clearErrorFromTextLayout(layout) }

        if (user == null && group != null) {
            label.text = context.getString(R.string.settings_change_group_name_label)
            edit.setText(group.name)
            edit.setOnEditorActionListener { v, _, _ ->
                val name = edit.text.toString()
                if (!WelcomePageFunctions.isGroupNameValid(name)) layout.error = context.getString(R.string.err_invalid_name)
                else {
                    CoroutineScope(Dispatchers.Main).launch {
                        UtilsSingleton.clearErrorFromTextLayout(layout)
                        group.name = name
                        val updated = GroupQueries().updateGroup(group)
                        if (updated) {
                            dialog.dismiss()
                            if (onDismiss != null) { onDismiss(name) }
                        }
                        else layout.error = context.getString(R.string.err_group_update)
                    }
                }
                UtilsSingleton.hideKeyboard(activity, v)
                true
            }
        } else if (user != null && group == null) {
            label.text = context.getString(R.string.settings_change_user_name_label)
            edit.setText(user.name)
            edit.setOnEditorActionListener { v, _, _ ->
                val name = edit.text.toString()
                if (!WelcomePageFunctions.isUserNameValid(name)) layout.error = context.getString(R.string.err_invalid_name)
                else {
                    CoroutineScope(Dispatchers.Main).launch {
                        UtilsSingleton.clearErrorFromTextLayout(layout)
                        val success = UserQueries().isUsernameAvailable(SharedPreferences.groupId, name)
                        if (success) {
                            user.name = name
                            val updated = UserQueries().updateUser(user, SharedPreferences.groupId)
                            if (updated) dialog.dismiss()
                            else layout.error = context.getString(R.string.err_user_update)
                        } else layout.error = context.getString(R.string.err_username_taken)
                    }
                }
                UtilsSingleton.hideKeyboard(activity, v)
                true
            }
        }
    }
}