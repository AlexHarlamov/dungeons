import model.Block
import model.Dungeon

class DungeonPool {

    /**
     * Two maps - "flat" representation of the graph edges
     */
    val poolIn: MutableMap<Int, MutableList<Dungeon>> = mutableMapOf()
    val poolOut: MutableMap<Int, MutableList<Dungeon>> = mutableMapOf()

    constructor(dungeons: List<Dungeon>) {
        for (i in 0 .. dungeons.map { it.area }.maxOf { it.size }) {
            poolIn[i] = mutableListOf()
            poolOut[i] = mutableListOf()
        }
        dungeons.forEach { dungeon: Dungeon ->
            dungeon.area.forEachIndexed { rowIndex, dungeonRow ->
                if (dungeonRow.first() == Block.AIR) {
                    poolIn[rowIndex]!!.add(dungeon)
                }
                if (dungeonRow.last() == Block.AIR) {
                    poolOut[rowIndex]!!.add(dungeon)
                }
            }
        }
    }

    fun createSequenceX(length: Int): List<Dungeon> {
        return emptyList()
    }

}