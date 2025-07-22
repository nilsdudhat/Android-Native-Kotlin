package com.belive.dating.extensions

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

fun View.getActualHeight(): Int {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    return measuredHeight
}

fun View.throttleFirstClick(intervalMillis: Long = 1000L, onClick: (View) -> Unit) {
    val scope = CoroutineScope(Dispatchers.Main)

    val clicksFlow = callbackFlow {
        setOnClickListener {
            trySend(it)
        }
        awaitClose { setOnClickListener(null) }
    }

    clicksFlow
        .throttleFirst(intervalMillis)
        .onEach { view ->
            onClick(view)
        }
        .launchIn(scope)
}

private var lastEmissionTime: Long? = null

fun <T> Flow<T>.throttleFirst(windowMillis: Long = 1000L): Flow<T> {
    return this.transform { value ->
        val currentTime = System.currentTimeMillis()

        if (currentTime - (lastEmissionTime ?: 0) >= windowMillis) {
            lastEmissionTime = currentTime
            emit(value)
        }
    }
}

fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    onItemClicked: ((Int) -> Unit),
    onBackPressedDispatcher: OnBackPressedDispatcher,
): NavController {

    val graphIdToTagMap = mutableMapOf<Int, String>()
    val graphIdToItemIdMap = mutableMapOf<Int, Int>()
    var selectedNavController: NavController? = null

    menu.forEachIndexed { index, menuItem ->
        val navGraphId = navGraphIds.getOrNull(index) ?: return@forEachIndexed
        val fragmentTag = "bottomNav#$index"
        graphIdToTagMap[menuItem.itemId] = fragmentTag
        graphIdToItemIdMap[navGraphId] = menuItem.itemId
    }

    val defaultItemId = menu[0].itemId

    onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (selectedItemId != defaultItemId) {
                selectedItemId = defaultItemId // Switch to default fragment
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed() // Exit app
                isEnabled = true
            }
        }
    })

    setOnItemSelectedListener { item ->
        try {
            val newFragmentTag = graphIdToTagMap[item.itemId] ?: return@setOnItemSelectedListener false
            var selectedFragment = fragmentManager.findFragmentByTag(newFragmentTag) as? NavHostFragment

            fragmentManager.beginTransaction().apply {
                try {
                    // Hide all other fragments
                    fragmentManager.fragments.forEach { fragment ->
                        if (fragment.isAdded && fragment != selectedFragment) hide(fragment)
                    }

                    if (selectedFragment == null) {
                        // Create the fragment only if needed
                        val newGraphId = graphIdToItemIdMap.entries.find { it.value == item.itemId }?.key
                            ?: return@setOnItemSelectedListener false
                        selectedFragment = NavHostFragment.create(newGraphId)
                        add(containerId, selectedFragment, newFragmentTag)
                    } else {
                        show(selectedFragment)
                    }

                    commitNow()
                } catch (e: Exception) {
                    catchLog("setupWithNavController: " + gsonString(e))
                }
            }

            selectedNavController = selectedFragment!!.navController

            onItemClicked(item.itemId)
        } catch (e: Exception) {
            catchLog("setupWithNavController: " + gsonString(e))
        }
        return@setOnItemSelectedListener true
    }

    // Load the first fragment by default
    val firstGraphId = navGraphIds.first()
    val firstItemId = graphIdToItemIdMap[firstGraphId] ?: return selectedNavController!!
    val firstFragmentTag = graphIdToTagMap[firstItemId] ?: return selectedNavController!!
    var firstFragment = fragmentManager.findFragmentByTag(firstFragmentTag) as? NavHostFragment

    if (firstFragment == null) {
        firstFragment = NavHostFragment.create(firstGraphId)
        fragmentManager.beginTransaction()
            .add(containerId, firstFragment, firstFragmentTag)
            .commitNow()
    }

    selectedNavController = firstFragment.navController

    // Ensure default selection
    selectedItemId = firstItemId

    return selectedNavController
}

fun TabLayout.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    onTabClicked: ((Int) -> Unit),
): NavController {

    val graphIdToTagMap = mutableMapOf<Int, String>()
    var selectedNavController: NavController? = null

    // Map each navigation graph ID to a unique tag
    for (index in 0 until tabCount) {
        val navGraphId = navGraphIds.getOrNull(index) ?: continue
        val fragmentTag = "tabNav#$index"
        graphIdToTagMap[navGraphId] = fragmentTag
    }

    addOnTabSelectedListener(object : OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let {
                val newFragmentTag = graphIdToTagMap[navGraphIds[it.position]] ?: return
                var selectedFragment = fragmentManager.findFragmentByTag(newFragmentTag) as? NavHostFragment

                fragmentManager.beginTransaction().apply {
                    // Hide all other fragments
                    fragmentManager.fragments.forEach { fragment ->
                        if (fragment.isAdded && fragment != selectedFragment) hide(fragment)
                    }

                    if (selectedFragment == null) {
                        // Create the fragment only if needed
                        selectedFragment = NavHostFragment.create(navGraphIds[it.position])
                        add(containerId, selectedFragment!!, newFragmentTag)
                    } else {
                        show(selectedFragment!!)
                    }

                    commitNow()
                }

                selectedNavController = selectedFragment!!.navController
                onTabClicked(it.position)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {

        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    })

    // Load the first fragment by default
    val firstGraphId = navGraphIds.first()
    val firstFragmentTag = graphIdToTagMap[firstGraphId] ?: return selectedNavController!!
    var firstFragment = fragmentManager.findFragmentByTag(firstFragmentTag) as? NavHostFragment

    if (firstFragment == null) {
        firstFragment = NavHostFragment.create(firstGraphId)
        fragmentManager.beginTransaction()
            .add(containerId, firstFragment, firstFragmentTag)
            .commitNow()
    }

    selectedNavController = firstFragment.navController

    return selectedNavController!!
}


fun View.getMaxAvailableWidth(callback: (Int) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            // Remove the listener to avoid multiple callbacks
            viewTreeObserver.removeOnGlobalLayoutListener(this)

            // Pass the width to the callback
            callback(width)
        }
    })
}

fun View.setBackgroundAnimation(colorArray: IntArray?) {
    val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colorArray)
    background = gradientDrawable
    viewTreeObserver.addOnGlobalLayoutListener {
        gradientDrawable.setBounds(-2 * width, 0, width, height)
        val animation = ValueAnimator.ofInt(0, 2 * width)
        animation.addUpdateListener { animation1: ValueAnimator ->
            gradientDrawable.setBounds(
                -2 * width + Math.round((animation1.animatedValue as Number).toFloat()),
                0,
                width + Math.round((animation1.animatedValue as Number).toFloat()),
                height
            )
        }
        animation.repeatMode = ValueAnimator.REVERSE
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = ValueAnimator.INFINITE
        animation.setDuration(1500)
        animation.start()
    }
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun pxToDp(px: Int): Int {
    return (px / Resources.getSystem().displayMetrics.density).toInt()
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}