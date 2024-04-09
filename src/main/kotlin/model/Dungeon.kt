package model

import java.security.MessageDigest

class Dungeon(
    val area: Array<Array<Block>>
) {

    /**
     * Hash that required to compare Dungeon configurations
     *
     * Additional note: Mathematically, it is exactly 2^(X*Y) variants of the possible area configurations for dungeons,
     * but in fact, most of them are not suitable. If we are taking in account the fact, that all the dungeons should
     * have a path through, significantly reducing the number of possible configurations. There is no point in calculating
     * possible amount of dungeons, but in case the fact that one of the main rules is that we have to avoid repetitions
     * for the dungeons in the sequence, it would be easier to add such a parameter for the comparisons.
     */
    val dungeonHash: String

    init {
        val matrixString = area.joinToString {
            it.map { cell ->
                when (cell) {
                    Block.AIR -> 0
                    Block.GROUND -> 1
                }
            }.joinToString()
        }

        val bytes = matrixString.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        dungeonHash = digest.joinToString("") { "%02x".format(it) }
    }

    override fun equals(other: Any?): Boolean {
        return when(other){
            is Dungeon -> this.dungeonHash == other.dungeonHash
            else -> super.equals(other)
        }
    }

    override fun toString(): String = buildString {
        for (x in 0 until area.first().size) {
            append("0   ")
            for (y in area.indices) {
                append(
                    when (area[y][x]) {
                        Block.GROUND -> "0   "
                        Block.AIR -> "    "
                    }
                )
            }
            append("0   \n")
        }
    }
}