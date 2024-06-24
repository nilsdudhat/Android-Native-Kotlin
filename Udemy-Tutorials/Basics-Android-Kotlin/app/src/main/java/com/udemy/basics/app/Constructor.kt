package com.udemy.basics.app

fun main() {
    val narayan = Narayan("Narayan", "Chennai")
    narayan.start()
    narayan.startClasses()

    println("Name of robot is ${narayan.name}")
    narayan.location = "Ahmedabad"
}

class Narayan : School {

    constructor(name: String, location: String) : super(name = name, location = location)
    constructor(name: String) : super(name = name)

    init {
        println("$name is created")
    }

    fun startClasses() {
        println("start all classes")
    }
}

// primary constructor
abstract class School() {

    var name: String = ""
        get() {
            println("getting name of robot")
            return field
        }
        set(value) {
            println("changing name of the robot to $value")
            field = value
        }

    var location: String = ""
        get() {
            println("getting location")
            return field
        }
        set(value) {
            println("changing location to $value")
            field = value
        }

    init {
        name = ""
        location = ""
    }

    // secondary constructor
    constructor(name: String, location: String) : this() {
        this.name = name
        this.location = location
    }

    // secondary constructor
    constructor(name: String) : this() {
        this.name = name
        location = ""
    }

    fun start() {
        println("Start School")
    }
}