package com.gabr.gabc.kelo.choreDetail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.gabr.gabc.kelo.R
import com.gabr.gabc.kelo.firebase.ChoreQueries
import com.gabr.gabc.kelo.firebase.UserQueries
import com.gabr.gabc.kelo.dataModels.Chore
import com.gabr.gabc.kelo.utils.*
import com.gabr.gabc.kelo.utils.common.CustomDatePicker
import com.gabr.gabc.kelo.viewModels.AssigneeViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/** Activity that manages the Edition and Detail View for a Chore */
class ChoreDetailActivity : AppCompatActivity() {

    companion object {
        const val VIEW_DETAILS = "VIEW_DETAILS"
        const val CHORE = "CHORE"
    }

    private var chore = Chore()
    private lateinit var viewModel: AssigneeViewModel

    private var viewDetails: Boolean = false
    private var selectedCalendar = Calendar.getInstance()

    private lateinit var toolbar: MaterialToolbar

    private lateinit var parent: ConstraintLayout
    private lateinit var icon: ImageView
    private lateinit var nameLayout: TextInputLayout
    private lateinit var nameEditText: TextInputEditText
    private lateinit var date: MaterialButton
    private lateinit var assignee: MaterialButton
    private lateinit var importanceGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chore_detail)

        viewModel = run { ViewModelProvider(this).get(AssigneeViewModel::class.java) }
        viewDetails = intent.getBooleanExtra(VIEW_DETAILS, false)
        if (viewDetails) chore = intent.getParcelableExtra(CHORE)!!

        parent = findViewById(R.id.chore_details_layout)
        parent.setOnClickListener { UtilsSingleton.hideKeyboard(this, parent) }

        icon = findViewById(R.id.choreDetailIcon)
        if (viewDetails) icon.setImageDrawable(UtilsSingleton.createAvatar(chore.name))

        UtilsSingleton.changeStatusBarColor(this, this, R.color.toolbarBackground)
        toolbar = findViewById(R.id.toolbar_widget)
        toolbar.title = if (viewDetails) getString(R.string.chore_detail) else getString(R.string.add_chore)
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setUpChoreName()
        setUpDatePicker()
        setUpImportance()
        setUpAssignee()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu?.findItem(R.id.toolbar_share)?.isVisible = false
        menu?.findItem(R.id.toolbar_completed_chores)?.isVisible = false
        menu?.findItem(R.id.toolbar_information)?.isVisible = false
        menu?.findItem(R.id.toolbar_assigned)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.toolbar_done -> {
                validateChore()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> true
        }
    }

    private fun validateChore() {
        chore.name = nameEditText.text.toString()

        chore.name?.let {
            if (!ChoreDetailFunctions.isChoreNameValid(it)) nameLayout.error = getString(R.string.err_invalid_name)
            else {
                if (ChoreDetailFunctions.validateChore(chore)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val gid = SharedPreferences.groupId
                        val user = UserQueries().getUser(SharedPreferences.userId, gid)
                        if (user != null) {
                            chore.creator = user.id
                            if (viewDetails) {
                                val success = ChoreQueries().updateChore(chore, gid)
                                if (success) finish()
                                else UtilsSingleton.showSnackBar(parent, getString(R.string.err_chore_update))
                            } else {
                                val res = ChoreQueries().createChore(chore, gid)
                                if (res != null) finish()
                                else UtilsSingleton.showSnackBar(parent, getString(R.string.err_chore_creation))
                            }
                        } else {
                            UtilsSingleton.showSnackBar(parent, getString(R.string.err_chore_creation))
                        }
                    }
                } else {
                    UtilsSingleton.showSnackBar(parent, getString(R.string.err_chore_not_completed))
                }
            }
        }
    }

    private fun setUpChoreName() {
        nameLayout = findViewById(R.id.choreDetailNameLayout)
        nameEditText = findViewById(R.id.choreDetailNameEditText)

        if (viewDetails) nameEditText.setText(chore.name)

        nameEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) UtilsSingleton.hideKeyboard(this, v)
        }
        nameEditText.doOnTextChanged { _, _, _, _ -> UtilsSingleton.clearErrorFromTextLayout(nameLayout) }
        nameEditText.setOnEditorActionListener { view, _, _ ->
            clearFocusOfEditTextAndSetDrawable()
            UtilsSingleton.hideKeyboard(this, view)
            true
        }
    }

    private fun setUpDatePicker() {
        date = findViewById(R.id.choreDetailExpireDateButton)

        if (viewDetails) chore.expiration?.let { selectedCalendar.time = it }
        date.text = DatesSingleton.parseCalendarToString(selectedCalendar)

        date.setOnClickListener {
            clearFocusOfEditTextAndSetDrawable()
            val datePicker = CustomDatePicker(selectedCalendar) { day, month, year ->
                val calendar = ChoreDetailFunctions.parseAndUpdateChoreWithSelectedDate(chore, day, month, year)
                selectedCalendar = calendar
                date.text = DatesSingleton.parseCalendarToString(calendar)
            }
            datePicker.show(supportFragmentManager, CustomDatePicker.TAG)
        }
    }

    private fun setUpImportance() {
        importanceGroup = findViewById(R.id.choreDetailImportance)

        when (chore.points) {
            10 -> importanceGroup.check(R.id.choreDetailLow)
            20 -> importanceGroup.check(R.id.choreDetailMedium)
            30 -> importanceGroup.check(R.id.choreDetailHigh)
        }

        importanceGroup.setOnCheckedChangeListener { _, checkedId ->
            clearFocusOfEditTextAndSetDrawable()
            when (checkedId) {
                R.id.choreDetailLow -> chore.points = 10
                R.id.choreDetailMedium -> chore.points = 20
                R.id.choreDetailHigh -> chore.points = 30
            }
        }
    }

    private fun setUpAssignee() {
        val tThis = this
        assignee = findViewById(R.id.choreDetailAssigneeButton)

        if (viewDetails) {
            chore.assignee?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    val user = UserQueries().getUser(it, SharedPreferences.groupId)
                    if (SharedPreferences.isUserBeingDisplayedCurrentUser(it)) {
                        if (user != null) assignee.text = UtilsSingleton.setTextForCurrentUser(tThis, user.name)
                    } else {
                        if (user != null) assignee.text = user.name
                    }
                }
            }
        }

        assignee.setOnClickListener {
            clearFocusOfEditTextAndSetDrawable()
            UsersBottomSheet().show(supportFragmentManager, UsersBottomSheet.TAG)
        }

        observeAssigneeUponSelection()
    }

    private fun observeAssigneeUponSelection() {
        viewModel.assignee.observe(this, { user ->
            if (SharedPreferences.isUserBeingDisplayedCurrentUser(user.id)) {
                assignee.text = UtilsSingleton.setTextForCurrentUser(this, user.name)
            } else {
                assignee.text = user.name
            }
            chore.assignee = user.id
        })
    }

    private fun clearFocusOfEditTextAndSetDrawable() {
        nameEditText.clearFocus()
        nameEditText.text?.let {
            if (it.trim().isNotEmpty()) icon.setImageDrawable(UtilsSingleton.createAvatar(it.toString()))
        }
    }
}