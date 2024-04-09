import model.Dungeon
import service.DungeonGenerator

fun main(args: Array<String>) {

    /**
     * Here is small demonstration of how pool works
     * The result of execution will be printed in terminal as connected sequence of [Dungeon]s, oriented vertically
     * Also, if you are interested in some generator's parameters changing - please check out [DungeonGenerator.Configuration]
     */

    val poolSize = 20
    val sequenceLength = 17

    val dungeons = mutableListOf<Dungeon>()
    for (i in 0 .. poolSize){
        dungeons.add(DungeonGenerator.generate())
    }
    val pool = DungeonPool(dungeons)
    val seq = pool.createSequenceX(sequenceLength)
    seq.forEach{ dungeon -> print(dungeon) }

}