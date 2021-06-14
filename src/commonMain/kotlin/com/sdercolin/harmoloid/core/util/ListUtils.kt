package com.sdercolin.harmoloid.core.util

fun <T> List<T>.copy(): List<T> {
    return this.map { it }
}

private fun <T> List<T>.update(index: Int, new: T): List<T> {
    return this.copy().toMutableList().also { it[index] = new }
}

private fun <T> List<T>.update(old: T, new: T): List<T> {
    return update(indexOf(old), new)
}

fun <T> List<T>.update(old: T, updater: (T) -> T): List<T> {
    return update(old, updater(old))
}

fun <T> List<T>.update(index: Int, updater: (T) -> T): List<T> {
    val old = this[index]
    return update(old, updater(old))
}

fun <T> List<T>.sumByLong(selector: (T) -> Long): Long {
    return fold(0L) { acc, element -> acc + selector(element) }
}
