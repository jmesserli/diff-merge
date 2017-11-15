package nu.peg.diffmerge

data class Diff<O>(
        val newItems: List<O>,
        val updatedItems: List<EntityUpdate<O>>,
        val deletedItems: List<O>
) {
    fun formatChanges(): String {
        val sb = StringBuilder()

        sb.append("+++ New Items +++\n")
        newItems.forEach { sb.append("+ $it\n") }

        sb.append("\n~~~ Updated Items ~~~\n")
        updatedItems.forEach {
            sb.append("~ ${it.oldValue}\n")
            it.updates.forEach {
                sb.append("  - ${it.name}: ${it.oldValue} -> ${it.newValue}\n")
            }
            sb.append("\n")
        }

        sb.append("\n--- Deleted Items ---\n")
        deletedItems.forEach { sb.append("- $it\n") }

        return sb.toString()
    }
}