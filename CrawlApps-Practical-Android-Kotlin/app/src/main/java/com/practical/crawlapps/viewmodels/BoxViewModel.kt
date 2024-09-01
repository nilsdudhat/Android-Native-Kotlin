package com.practical.crawlapps.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.practical.crawlapps.R
import com.practical.crawlapps.adapters.BoxAdapter
import com.practical.crawlapps.databinding.ActivityBoxBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class BoxFactory(
    val activity: Activity,
    val binding: ActivityBoxBinding,
)

class BoxViewModel(private val factory: BoxFactory) : ViewModel() {

    private var boxAdapter: BoxAdapter? = null
    private val map = mutableMapOf<Int, MutableMap<String, Int>>()

    private var groupID = 0

    private fun assignGroupIDs(position: Int, size: Int, recursivePosition: Int?) {

        if (position < 0 || position >= (size * size)) {
            return
        }

        val currentBox = map[position]!!
        val currentColor = currentBox["color"]

        if (currentBox.containsKey("groupID")) {
            return
        }

        var updatedList = mutableListOf<Int>()

        if (position >= size) { // up side box
            val upSidePosition = position - size
            Log.d("--positions--", "upSidePosition: $position, $upSidePosition")

            val upSideBox = map[upSidePosition]

            val upSideColor = upSideBox!!["color"]

            if (upSideColor == currentColor) {
                if (upSideBox.containsKey("groupID")) {
                    currentBox["groupID"] = upSideBox["groupID"]!!
                }
                updatedList.add(upSidePosition)
            }
        }

        if (((position + 1) % size) != 0) { // right side box
            val rightSidePosition = position + 1
            Log.d("--positions--", "rightSidePosition: $position, $rightSidePosition")

            val rightSideBox = map[rightSidePosition]

            val rightSideColor = rightSideBox!!["color"]

            if (rightSideColor == currentColor) {
                if (rightSideBox.containsKey("groupID")) {
                    currentBox["groupID"] = rightSideBox["groupID"]!!
                }
                updatedList.add(rightSidePosition)
            }
        }

        if ((position % size) != 0) { // left side box
            val leftSidePosition = position - 1
            Log.d("--positions--", "leftSidePosition: $position, $leftSidePosition")

            val leftSideBox = map[leftSidePosition]

            val leftSideColor = leftSideBox!!["color"]

            if (leftSideColor == currentColor) {
                if (leftSideBox.containsKey("groupID")) {
                    currentBox["groupID"] = leftSideBox["groupID"]!!
                }
                updatedList.add(leftSidePosition)
            }
        }

        if (position < ((size * size) - size)) { // down side box
            val downSidePosition = position + size
            Log.d("--positions--", "downSidePosition: $position, $downSidePosition")

            val downSideBox = map[downSidePosition]

            val downSideColor = downSideBox!!["color"]

            if (downSideColor == currentColor) {
                if (downSideBox.containsKey("groupID")) {
                    currentBox["groupID"] = downSideBox["groupID"]!!
                }
                updatedList.add(downSidePosition)
            }
        }

        updatedList = updatedList.distinct().toMutableList()
        if (updatedList.contains(recursivePosition)) {
            updatedList.remove(recursivePosition)
        }

        Log.d(
            "--groupID--",
            "position: $position, groupID: $groupID, recursivePosition: $recursivePosition, updatedList: ${updatedList.toList()}"
        )

        if (!currentBox.containsKey("groupID")) {
            currentBox["groupID"] = groupID++
        }

        map[position] = currentBox

        for (updatedPosition in updatedList) {
            assignGroupIDs(updatedPosition, size, position)
        }
    }

    fun createBoxes(size: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            val list = listOf(
                R.color.yellow,
                R.color.green,
                R.color.blue,
                R.color.red,
            )

            for (position in 0..<(size * size)) {
                val currentBox = mutableMapOf<String, Int>()

                currentBox["color"] = list.random()
                currentBox["position"] = position

                map[position] = currentBox
            }

            viewModelScope.launch(Dispatchers.Main) {
                boxAdapter?.setMap(map)
            }
        }
    }

    fun displayGroupIDs(size: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            groupID = 0

            for (position in 0..<(size * size)) {
                map[position]!!.remove("groupID")
            }

            for (position in 0..<(size * size)) {
                assignGroupIDs(position, size, null)
            }

            viewModelScope.launch(Dispatchers.Main) {
                boxAdapter?.setMap(map)
            }
        }
    }

    fun initialiseRecyclerView(size: Int) {
        factory.binding.rvBoxes.apply {
            if (layoutManager == null) {
                layoutManager = GridLayoutManager(factory.activity, size)
            }
            if (adapter == null) {
                boxAdapter = BoxAdapter()
            }
            adapter = boxAdapter
        }
    }
}