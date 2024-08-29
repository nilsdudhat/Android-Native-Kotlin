package com.floatingbutoon.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    // Use the FloatingActionButton for all the add person
    // and add alarm
    var mAddAlarmFab: FloatingActionButton? = null
    var mAddPersonFab: FloatingActionButton? = null

    // Use the ExtendedFloatingActionButton to handle the
    // parent FAB
    var mAddFab: ExtendedFloatingActionButton? = null

    // These TextViews are taken to make visible and
    // invisible along with FABs except parent FAB's action
    // name
    var addAlarmActionText: TextView? = null
    var addPersonActionText: TextView? = null

    // to check whether sub FABs are visible or not
    var isAllFabsVisible: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register all the FABs with their appropriate IDs
        // This FAB button is the Parent
        mAddFab = findViewById(R.id.add_fab)

        // FAB button
        mAddAlarmFab = findViewById(R.id.add_alarm_fab)
        mAddPersonFab = findViewById(R.id.add_person_fab)

        // Also register the action name text, of all the
        // FABs. except parent FAB action name text
        addAlarmActionText = findViewById(R.id.add_alarm_action_text)
        addPersonActionText = findViewById(R.id.add_person_action_text)

        // Now set all the FABs and all the action name
        // texts as GONE
        mAddAlarmFab?.visibility = View.GONE
        mAddPersonFab?.visibility = View.GONE
        addAlarmActionText?.visibility = View.GONE
        addPersonActionText?.visibility = View.GONE

        // make the boolean variable as false, as all the
        // action name texts and all the sub FABs are
        // invisible
        isAllFabsVisible = false

        // Set the Extended floating action button to
        // shrinked state initially
        mAddFab?.shrink()

        // We will make all the FABs and action name texts
        // visible only when Parent FAB button is clicked So
        // we have to handle the Parent FAB button first, by
        // using setOnClickListener you can see below
        mAddFab?.setOnClickListener(
            View.OnClickListener {
                if (!isAllFabsVisible!!) {
                    // when isAllFabsVisible becomes
                    // true make all the action name
                    // texts and FABs VISIBLE.

                    mAddAlarmFab?.show()
                    mAddPersonFab?.show()
                    addAlarmActionText?.visibility = View.VISIBLE
                    addPersonActionText?.visibility = View.VISIBLE

                    // Now extend the parent FAB, as
                    // user clicks on the shrinked
                    // parent FAB
                    mAddFab?.extend()

                    // make the boolean variable true as
                    // we have set the sub FABs
                    // visibility to GONE
                    isAllFabsVisible = true
                } else {
                    // when isAllFabsVisible becomes
                    // true make all the action name
                    // texts and FABs GONE.

                    mAddAlarmFab?.hide()
                    mAddPersonFab?.hide()
                    addAlarmActionText?.visibility = View.GONE
                    addPersonActionText?.visibility = View.GONE

                    // Set the FAB to shrink after user
                    // closes all the sub FABs
                    mAddFab?.shrink()

                    // make the boolean variable false
                    // as we have set the sub FABs
                    // visibility to GONE
                    isAllFabsVisible = false
                }
            })

        // below is the sample action to handle add person
        // FAB. Here it shows simple Toast msg. The Toast
        // will be shown only when they are visible and only
        // when user clicks on them
        mAddPersonFab?.setOnClickListener(
            View.OnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Person Added",
                    Toast.LENGTH_SHORT
                ).show()
            })

        // below is the sample action to handle add alarm
        // FAB. Here it shows simple Toast msg The Toast
        // will be shown only when they are visible and only
        // when user clicks on them
        mAddAlarmFab?.setOnClickListener(
            View.OnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Alarm Added",
                    Toast.LENGTH_SHORT
                ).show()
            })
    }
}
