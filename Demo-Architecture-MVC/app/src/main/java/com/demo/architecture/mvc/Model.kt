package com.demo.architecture.mvc

import java.util.Observable

class Model : Observable() {

    // declaring a list of integer
    private val list: MutableList<Int>

    // constructor to initialize the list
    init {
        // reserving the space for list elements
        list = ArrayList(3)

        // adding elements into the list
        list.add(0)
        list.add(0)
        list.add(0)
    }

    // defining getter and setter functions
    // function to return appropriate count
    // value at correct index
    @Throws(IndexOutOfBoundsException::class)
    fun getValueAtIndex(index: Int) : Int {
        return list[index]
    }

    // function to make changes in the activity button's
    // count value when user touch it
    @Throws(IndexOutOfBoundsException::class)
    fun setValueAtIndex(index: Int) {
        list[index] = list[index] + 1
        setChanged()
        notifyObservers()
    }
}