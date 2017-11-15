package nu.peg.diffmerge

data class EntityUpdate<O>(
        val oldValue: O,
        val updates: List<AttributeUpdate<O, *>>
) {
    fun applyUpdates(input: O): O {
        updates.forEach { it.applyFn(input) }
        return input
    }
}

data class AttributeUpdate<in O, out T>(
        val name: String,
        val oldValue: T,
        val newValue: T,
        val applyFn: (O) -> Unit
)