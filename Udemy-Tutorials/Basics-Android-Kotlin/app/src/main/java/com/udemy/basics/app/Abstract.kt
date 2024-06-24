package com.udemy.basics.app

fun main() {

}

class B : A() {
    override fun a() {

    }
}

class C: A() {
    override fun a() {
     
    }
}

abstract class A {
    abstract fun a()
}