import model.Block
import model.Dungeon
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import service.DungeonGenerator

class DungeonPoolTest {

    @Test
    fun `if sequence parameters incorrect, then exception thrown`() {
        val pool0 = DungeonPool(emptyList())
        assertThrows(IllegalArgumentException::class.java) { pool0.createSequenceX(0) }
        assertThrows(IllegalArgumentException::class.java) { pool0.createSequenceX(1) }
        val dungeons = mutableListOf<Dungeon>()
        for (i in 0..1) {
            dungeons.add(DungeonGenerator.generate())
        }
        val pool1 = DungeonPool(dungeons)
        assertThrows(IllegalArgumentException::class.java) { pool1.createSequenceX(0) }
        assertThrows(IllegalArgumentException::class.java) { pool1.createSequenceX(3) }
    }

    @Test
    fun `if path doesn't exist, then exception thrown`() {
        val arrayForDungeon0 = arrayOf(
            arrayOf(Block.GROUND, Block.GROUND, Block.AIR, Block.GROUND),
            arrayOf(Block.GROUND, Block.GROUND, Block.AIR, Block.GROUND),
            arrayOf(Block.GROUND, Block.GROUND, Block.AIR, Block.GROUND),
            arrayOf(Block.GROUND, Block.GROUND, Block.AIR, Block.GROUND)
        )
        val arrayForDungeon1 = arrayOf(
            arrayOf(Block.GROUND, Block.AIR, Block.GROUND, Block.GROUND),
            arrayOf(Block.GROUND, Block.AIR, Block.GROUND, Block.GROUND),
            arrayOf(Block.GROUND, Block.AIR, Block.GROUND, Block.GROUND),
            arrayOf(Block.GROUND, Block.AIR, Block.GROUND, Block.GROUND)
        )
        val dungeon0 = Dungeon(arrayForDungeon0)
        val dungeon1 = Dungeon(arrayForDungeon1)

        //Different non-connectable dungeons
        val pool0 = DungeonPool(listOf(dungeon0, dungeon1))
        assertThrows(IllegalStateException::class.java) { pool0.createSequenceX(2) }

        //Equals connectable dungeons B variant
        val pool1 = DungeonPool(listOf(dungeon0, dungeon0))
        assertThrows(IllegalStateException::class.java) { pool1.createSequenceX(2) }

        //Equals connectable dungeons P variant
        val pool2 = DungeonPool(listOf(dungeon0, dungeon0, dungeon0, dungeon0, dungeon0, dungeon0))
        assertThrows(IllegalStateException::class.java) { pool2.createSequenceX(2) }

        val arrayForDungeon2 = arrayOf(
            arrayOf(Block.GROUND, Block.AIR,    Block.GROUND, Block.GROUND),
            arrayOf(Block.GROUND, Block.GROUND, Block.GROUND, Block.GROUND),
            arrayOf(Block.GROUND, Block.AIR,    Block.GROUND, Block.GROUND),
            arrayOf(Block.GROUND, Block.AIR,    Block.GROUND, Block.GROUND)
        )
        val arrayForDungeon3 = arrayOf(
            arrayOf(Block.GROUND, Block.GROUND, Block.GROUND, Block.AIR),
            arrayOf(Block.GROUND, Block.GROUND, Block.GROUND, Block.AIR),
            arrayOf(Block.GROUND, Block.GROUND, Block.GROUND, Block.AIR),
            arrayOf(Block.GROUND, Block.GROUND, Block.GROUND, Block.AIR)
        )

        val dungeon2 = Dungeon(arrayForDungeon2)
        val dungeon3 = Dungeon(arrayForDungeon3)

        //Required length is not reachable B variant
        val pool3 = DungeonPool(listOf(dungeon1, dungeon2, dungeon1))
        assertThrows(IllegalStateException::class.java) { pool3.createSequenceX(3) }

        //Required length is not reachable P variant
        val pool4 = DungeonPool(listOf(dungeon1, dungeon2, dungeon1, dungeon3, dungeon3))
        assertThrows(IllegalStateException::class.java) { pool4.createSequenceX(3) }
    }

    @Test
    fun `in random pool always exist paths for the positive variant`() {
        val testAmount = 1000

        var endTime: Long
        var startTime = System.nanoTime()
        var durationInSeconds: Double

        for (i in 0..testAmount) {                          // in 1000 different pools

            val percentage = (i.toDouble() / testAmount.toDouble()) * 100
            if (percentage % 10.0 == 0.0){
                endTime = System.nanoTime()
                durationInSeconds = (endTime - startTime) / 1_000_000_000.0
                println(String.format("%.0f%%", percentage) + " - ${String.format("%.3f", durationInSeconds)} seconds")
                startTime = System.nanoTime()
            }

            val dungeons = mutableListOf<Dungeon>()
            val n = 200
            for (j in 0..n) {                                // of size = 200
                dungeons.add(DungeonGenerator.generate())
            }
            val pool = DungeonPool(dungeons)
            for (k in 1..2 * n / 3) {
                assertDoesNotThrow { pool.createSequenceX(k) }      // all the paths withs length < 2n/y are exist
            }
        }
    }
}