package com.udemy.basics.app

fun main() {
    sayHello(name = "Nilesh", age = "27")

    println()
    println()

    println("Sum is ${sum(1, 2)}")

    println()
    println()

    println("Sum of 5.5 and 6.6 is ${sum(5.5, 6.6)}")
    println("Sum of 5 and 6 and 7 is ${sum(5, 6, 7)}")
}

// simple function
fun sayHello(name: String, age: String = "not specified") {
    println("My name is $name and I am $age years old")
}

// function with return type
fun sum(x: Int, y: Int): Int {
    return x + y
}

// function with overloading (same name but different parameters)
fun sum(x: Int, y: Int, z: Int): Int {
    return x + y + z
}

// function with overloading (same name but different parameters)
fun sum(x: Double, y: Double): Double {
    return x + y
}