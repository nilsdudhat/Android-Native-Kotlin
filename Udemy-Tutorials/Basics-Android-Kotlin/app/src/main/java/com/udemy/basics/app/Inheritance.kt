package com.udemy.basics.app

fun main() {
    val cookingRobot = CookingRobot("Cookie")
    cookingRobot.turnOn()

    val ordinaryRobot = OrdinaryRobot("Sevak")
    ordinaryRobot.turnOnLights()

    cookingRobot.cook()
    cookingRobot.notifyMeOnDone()
    ordinaryRobot.cleanKitchen()
}

class OrdinaryRobot(name: String) : GeneralRobot(name) {

    init {
        println("$name is created")
    }

    fun turnOnLights() {
        println("Turn on Lights")
    }

    fun turnOffLights() {
        println("Turn off Lights")
    }

    fun cleanKitchen() {
        println("Clean Kitchen")
    }
}

class CookingRobot(name: String) : GeneralRobot(name) {
    init {
        println("$name is created")
    }

    fun cook() {
        println("$name is cooking")
    }

    fun notifyMeOnDone() {
        println("$name notify me once dinner is ready")
    }
}

open class GeneralRobot(val name: String) {
    fun turnOn() {
        println("Turning on $name")
    }

    fun turnOff() {
        println("Turning off $name")
    }

    fun walk() {
        println("$name is walking")
    }

    fun run() {
        println("$name is running")
    }

    fun stop() {
        println("$name is stopped")
    }
}