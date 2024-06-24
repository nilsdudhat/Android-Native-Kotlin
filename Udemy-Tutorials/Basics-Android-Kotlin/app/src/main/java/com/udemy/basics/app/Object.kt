package com.udemy.basics.app

fun main() {
    val robot = Robot("Chiittie")
    robot.walk()
    robot.run()
    robot.stop()
    robot.announce("Start moving your heads")
}

class Robot(private val name: String) {
    fun walk() {
        println("$name start walking")
    }
    fun run() {
        println("$name start running")
    }
    fun stop() {
        println("$name stop")
    }

    fun announce(announcement: String) {
        println(announcement)
    }
}