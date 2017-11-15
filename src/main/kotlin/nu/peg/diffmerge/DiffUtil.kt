package nu.peg.diffmerge

fun <I, O> createDiff(existingList: List<O>, newList: List<I>, provider: DiffProvider<I, *, O>): Diff<O> {
    val existingMutable = existingList.toMutableList()
    val newMutable = newList.toMutableList()

    val newItems = mutableListOf<O>()
    val updatedItems = mutableListOf<EntityUpdate<O>>()
    val deletedItems = mutableListOf<O>()

    existingMutable.forEach { existing ->
        val outKey = provider.outKey(existing)
        var furtherProcessing = true
        for (new in newMutable) {
            val inKey = provider.inKey(new)
            if (outKey == inKey) {
                // merge
                val update = provider.merge(new, existing)
                if (update != null) updatedItems.add(update)
                furtherProcessing = false
                newMutable.remove(new)
                break
            }
        }

        // delete
        if (furtherProcessing) deletedItems.add(existing)
    }

    newItems += newMutable.map(provider::create)

    return Diff(newItems, updatedItems, deletedItems)
}

fun <T> applyDiff(existingList: List<T>, diff: Diff<T>): List<T> {
    val outList = existingList.toMutableList()

    outList.removeAll(diff.deletedItems)
    outList.replaceAll { item ->
        diff.updatedItems.forEach { update ->
            if (item == update.oldValue) {
                return@replaceAll update.applyUpdates(item)
            }
        }
        return@replaceAll item
    }
    outList.addAll(diff.newItems)

    return outList
}