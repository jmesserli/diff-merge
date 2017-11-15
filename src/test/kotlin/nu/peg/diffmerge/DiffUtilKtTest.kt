package nu.peg.diffmerge

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.jupiter.api.Test

internal class DiffUtilKtTest {
    private class SimpleDiffProvider<T> : DiffProvider<T, T, T> {
        override fun inKey(input: T): T = input
        override fun outKey(output: T): T = output
        override fun create(input: T): T = input
        override fun merge(input: T, output: T): EntityUpdate<T>? = null
    }

    data class MutableTuple<T, E>(var key: T, var value: E)

    private class TupleDiffProvider<K, V> : DiffProvider<MutableTuple<K, V>, K, MutableTuple<K, V>> {
        override fun inKey(input: MutableTuple<K, V>): K = input.key

        override fun outKey(output: MutableTuple<K, V>): K = output.key

        override fun create(input: MutableTuple<K, V>): MutableTuple<K, V> = MutableTuple(input.key, input.value)

        override fun merge(input: MutableTuple<K, V>, output: MutableTuple<K, V>): EntityUpdate<MutableTuple<K, V>>? {
            if (input.value == output.value) {
                return null
            }

            return EntityUpdate(output, listOf(
                    AttributeUpdate("value", output.value, input.value, { output.value = input.value })
            ))
        }
    }

    @Test
    fun noChanges() {
        val list = listOf("Hello", "World")

        val diff = createDiff(list, list, SimpleDiffProvider())

        assert(diff.deletedItems).isEmpty()
        assert(diff.updatedItems).isEmpty()
        assert(diff.newItems).isEmpty()
    }

    @Test
    fun newItem() {
        val list = listOf("hello", "world")
        val newItem = "mate"
        val newList = list + listOf(newItem)

        val diff = createDiff(list, newList, SimpleDiffProvider())
        assert(diff.deletedItems).isEmpty()
        assert(diff.updatedItems).isEmpty()
        assert(diff.newItems).hasSize(1)
        assert(diff.newItems.first()).isEqualTo(newItem)
    }

    @Test
    fun deletedItem() {
        val world = "world"
        val list = listOf("hello", world)
        val newList = list - listOf(world)

        val diff = createDiff(list, newList, SimpleDiffProvider())
        assert(diff.deletedItems).hasSize(1)
        assert(diff.deletedItems.first()).isEqualTo(world)
        assert(diff.updatedItems).isEmpty()
        assert(diff.newItems).isEmpty()
    }

    @Test
    fun mergeItem() {
        val oldValue = MutableTuple("hello", "world")
        val list = listOf(oldValue)
        val newList = listOf(MutableTuple("hello", "m8"))

        val diff = createDiff(list, newList, TupleDiffProvider())
        assert(diff.deletedItems).isEmpty()
        assert(diff.updatedItems).hasSize(1)
        val update = diff.updatedItems.first()
        assert(update.oldValue).isEqualTo(oldValue)
        assert(update.updates).hasSize(1)
        val attUpdate = update.updates.first()
        assert(attUpdate.name).isEqualTo("value")
        assert(attUpdate.oldValue).isNotEqualTo(attUpdate.newValue)
        assert(attUpdate.oldValue).isEqualTo("world")
        assert(attUpdate.newValue).isEqualTo("m8")
        attUpdate.applyFn(update.oldValue)
        assert(oldValue.value).isEqualTo(attUpdate.newValue)
        assert(diff.newItems).isEmpty()
    }
}