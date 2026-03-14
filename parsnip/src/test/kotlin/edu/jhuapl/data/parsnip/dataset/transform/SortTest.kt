package edu.jhuapl.data.parsnip.dataset.transform

import edu.jhuapl.testkt.shouldBe
import junit.framework.TestCase

class SortTest : TestCase() {

    private val data = listOf(
        mapOf("a" to 2),
        mapOf("a" to null),
        mapOf("a" to 1),
        mapOf("a" to null),
        mapOf("a" to 3)
    )

    fun testSortBy_nullsLast() {
        SortBy("a").invoke(data).map { it["a"] } shouldBe listOf(1, 2, 3, null, null)
    }

    fun testSortByDescending_nullsFirst() {
        // sortedByDescending reverses the comparator, so nulls appear first in descending order
        SortByDescending("a").invoke(data).map { it["a"] } shouldBe listOf(null, null, 3, 2, 1)
    }

}
