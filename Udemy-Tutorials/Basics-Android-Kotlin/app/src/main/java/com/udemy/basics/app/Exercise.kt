package com.udemy.basics.app

import java.util.Scanner

fun main() {
//    exercise1()
//    exercise2()
//    exercise3()
//    exercise4()
//    exercise5()
//    exercise6()
//    exercise7()
//    exercise8()
//    exercise9()
//    exercise10()
//    exercise11()
//    exercise12()
//    exercise13()
//    exercise14()
    exercise15()
}

fun exercise15() {
    //   *****
    //  *******
    // *********
    //  *******
    //   *****
    //    ***
    //     *

    val rows = 5
    val charLengthInRow = 9

    for (i in 3..rows) {
        val spaces = rows - i
        for (j in 1..spaces) {
            print(" ")
        }
        val stars = charLengthInRow - (spaces * 2)
        for (j in 1..stars) {
            print("*")
        }
        for (j in 1..spaces) {
            print(" ")
        }
        println()
    }
    for (i in (rows - 1) downTo 1) {
        val spaces = rows - i
        for (j in 1..spaces) {
            print(" ")
        }
        val stars = charLengthInRow - (spaces * 2)
        for (j in 1..stars) {
            print("*")
        }
        for (j in 1..spaces) {
            print(" ")
        }
        println()
    }
}

fun exercise14() {
    val temp = "KANCHANBEN"
    var s1 = "KANCHANBEN"
    var s2 = "NILESH"

    temp.forEach {
        if (s2.contains(it)) {
            s1 = s1.replaceFirst("$it", "")
            s2 = s2.replaceFirst("$it", "")
        }
    }

    print("$s1$s2")
}

fun exercise13() {
    // ****1****
    // ***123***
    // **12345**
    // *1234567*
    // 123456789

    val rows = 5
    for (i in 1..rows) {
        val spaces = rows - i
        for (j in 1..spaces) {
            print(" ")
        }
        val numberLength = 9 - (spaces * 2)
        for (j in 1..numberLength) {
            print(j)
        }
        for (j in 1..spaces) {
            print(" ")
        }
        println()
    }
}

fun exercise12() {
    // 12345
    // ****
    // 123
    // **
    // 1
    // write code for above pattern with simple logic
    val rows = 5
    for (i in rows downTo 1) {
        for (j in 1..i) {
            print(if (i % 2 == 0) "*" else j)
        }
        println()
    }
}

fun exercise11() {
    // A
    // **
    // DEF
    // ****
    // KLMNO
    // write code for above pattern with simple logic
    val rows = 5
    var alphabet = 'A'
    for (i in 1..rows) {
        for (j in 1..i) {
            print(if (i % 2 == 0) "*" else alphabet)
            alphabet++
        }
        println()
    }
    println()
}

fun exercise10() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return // non-local return directly to the caller of foo()
        print(it)
    }
    println("this point is unreachable")
}

fun exercise9() {
    for (i in 1..5) {
        abc@ for (j in 1..5) {
            if (j == i) {
                break@abc
            }
            println("$i $j")
        }
    }
}

fun exercise8() {
    val scanner = Scanner(System.`in`)
    print("Enter three numbers: ")

    val x = scanner.nextInt()
    val y = scanner.nextInt()
    val z = scanner.nextInt()

    val max = maxOf(x, y, z)
    println("Max is: $max")

    val min = minOf(x, y, z)
    println("Min is: $min")
}

fun exercise7() {
    val array = arrayOf(1, 2, 5, 46, 4697, 421)

    val odds = array.count { it % 2 == 1 }
    val evens = array.count { it % 2 == 0 }

    println("Odds: $odds")
    println("Evens: $evens")
}

fun exercise6() {
    val array1 = arrayOf(1, 4, 6, -4, 41)
    val array2 = arrayOf(1, -2, -3, 9, 5)

    val resultArray = IntArray(array1.size)

    for (i in array1.indices) {
        resultArray[i] = array1[i] * array2[i]
    }

    for (i in resultArray) {
        println(i)
    }
}

fun exercise5() {
    // reverse given String

    val statement = "My name is Nilesh, I am 26 years old."

    val reverseStatement = statement.reversed()
    println(reverseStatement)
}

fun exercise4() {
    val statement = "My name is Nilesh Dudhat."

    val size = statement.length
    println("Size of statement is $size")

    val words = statement.split(" ")
    println("Number of words in statement is ${words.size}")

    val numberOfSpaces = statement.count { it.isWhitespace() }
    println("Number of spaces in statement is $numberOfSpaces")

    val numberOfVowels =
        statement.count { it == 'a' || it == 'e' || it == 'i' || it == 'o' || it == 'u' }
    println("Number of vowels in statement is $numberOfVowels")

    val numberOfConsonants =
        statement.count { it != 'a' && it != 'e' && it != 'i' && it != 'o' && it != 'u' }
    println("Number of consonants in statement is $numberOfConsonants")

    val numberOfDigits = statement.count { it.isDigit() }
    println("Number of digits in statement is $numberOfDigits")
}

fun exercise3() {
    // swap values

    val scanner = Scanner(System.`in`)
    print("Enter two number: ")

    // using temp variable
    var a = scanner.nextInt()
    var b = scanner.nextInt()

    val temp = b
    b = a
    a = temp

    println("a = $a")
    println("b = $b")

    // without using other variable
    a = a + b
    b = a - b
    a = a - b

    println("a = $a")
    println("b = $b")
}

fun exercise2() {
    val scanner = Scanner(System.`in`)
    print("Enter a number: ")

    val radius = scanner.nextInt()

    // Circle
    val area = 3.14 * radius * radius
    val perimeter = 2 * 3.14 * radius

    println("Area is: $area")
    println("Perimeter is: $perimeter")
}

fun exercise1() {
    val scanner = Scanner(System.`in`)
    print("Enter two number: ")

    val a = scanner.nextInt()
    val b = scanner.nextInt()

    val division = a.toDouble() / b.toDouble()
    println("Division is: $division")

    val remainder = a % b
    println("Remainder is: $remainder")
}