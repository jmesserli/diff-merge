package nu.peg.diffmerge

interface DiffProvider<in I, out K, O> {
    fun inKey(input: I): K
    fun outKey(output: O): K

    fun create(input: I): O
    fun merge(input: I, output: O): EntityUpdate<O>?
}