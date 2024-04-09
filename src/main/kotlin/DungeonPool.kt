import exception.OutOfAttemptsException
import model.Block
import model.Dungeon
import service.DungeonGenerator
import java.util.*
import kotlin.random.Random

/**
 * Dungeon sequences factory.
 *
 * The main idea of the algorithm is based on the DFS inside graph. But here we have various amount of Dungeons (N) and fixed
 * amount of possible enters and exits from dungeon : 1 + GA , where
 *  GA = 2 * avg(abs(gauss(mean = 0.0, stdDeviation = 0.5))) ~ 1 // 2 tries of 50%
 * following this approximation, we can rely on GA = 1, so that in average every dungeon has about 2 enters and 2 exits.
 * So, we can simplify the task. Let's build a graph, where :
 *  Vertexes - possible connections between dungeons
 *  Edges - dungeons itself
 * In that case due to normal distribution of exits (and enters, later - only exits), every vertex has about
 * 2N/Y edges (N/Y - for each exit, and 2 exits for each dungeon), where :
 *  N - number of dungeons
 *  Y - number of possible exits
 * Following these calculations, if we will start DFS in random vertex, we will have 2N/Y possible paths to go.
 * But because we have a constraint that dungeon shouldn't be used twice, we have additional estimation :
 *  (2N/Y)-(2*1/Y) (minus 1 path, when we will come back after visiting half edges), if we will keep induction :
 *  ((2N/Y)-(2K/Y))^K = ((2N-2K)/Y)^K = (2(N-K)/Y)^K (^K - each step)
 * Following this, we can predict that if K -> N then amount of the paths will reduce - this is worst case scenario
 * then we have :
 * if K <= 2/3 * N we can be sure, that the path will be found in O(N^2) (VARIANT P - positive)
 * if K -> N, then O(N^K) - for that case (VARIANT B - border)
 *
 * Also, this approach will let us modify graph dynamically, adding or removing dungeons (as possible further features)
 */
class DungeonPool
    (dungeons: List<Dungeon>) {

    /**
     * Two maps - "flat" representation of the graph edges
     */
    val poolIn: MutableMap<Int, MutableList<Dungeon>> = mutableMapOf()
    val poolOut: MutableMap<Int, MutableList<Dungeon>> = mutableMapOf()

    var dungeonsNumber = 0

    init {
        for (i in 0..dungeons.map { it.area }.maxOf { it.size }) {
            poolIn[i] = mutableListOf()
            poolOut[i] = mutableListOf()
        }
        //Building graph
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
        dungeonsNumber = dungeons.size
    }

    enum class Variant {
        POSITIVE,
        BORDER
    }

    /**
     * Generates sequence of the compatible [Dungeon]s, using DFS
     */
    fun createSequenceX(length: Int): List<Dungeon> {

        // Early termination if length is 0 or the pool is empty
        if (length <= 0 || length > dungeonsNumber) {
            throw IllegalArgumentException("Length should be in range of 0 and $dungeonsNumber")
        }

        val variant = if (length > (2 * dungeonsNumber) / 3) {
            Variant.BORDER
        } else {
            Variant.POSITIVE // in that case we can just proceed Y/2 attempts for each node without touching worst case scenario
        }

        println(variant)

        return when (variant) {
            Variant.POSITIVE -> {
                try {
                    positiveVariant(length)
                } catch (e: OutOfAttemptsException) {
                    println("Switching strategy")
                    borderVariant(length) // Second attempt to create dungeon with bruteforce strategy
                }
            }

            Variant.BORDER -> {
                borderVariant(length)
            }
        }
    }

    /**
     * Implementation of the case when K -> N
     */
    private fun borderVariant(length: Int): List<Dungeon> {
        val stack = Stack<Dungeon>()
        loop@ for (row in poolOut.values) {
            for (dungeon in row) {
                bruteForceDFS(stack, dungeon, length)
                if (stack.size == length) {
                    break@loop // Break out of the outer loop directly
                }
            }
        }

        if (stack.isEmpty()) {
            throw IllegalArgumentException("Such set of dungeons is not compatible in sequence of length $length")
        }

        return stack
    }

    /**
     * Implementation of the case when K << N
     */
    private fun positiveVariant(length: Int): List<Dungeon> {
        return lightweightDFS(length)
    }

    /**
     * DFS which tries to find suitable path in single try
     */
    private fun lightweightDFS(length: Int): List<Dungeon> {
        val attemptsLimit = 2 * dungeonsNumber / DungeonGenerator.Configuration.ySize
        val currentSequence = mutableListOf<Dungeon>()
        var currentDungeon: Dungeon? = null

        while (currentSequence.size < length) {
            currentDungeon = if (currentDungeon == null) {
                findInitialDungeon(attemptsLimit, currentSequence)
            } else {
                findNextDungeon(
                    currentDungeon,
                    attemptsLimit,
                    currentSequence
                ) // If new Dungeon not found in few attempts - stop the process
            }

            currentDungeon?.let { currentSequence.add(it) }
                ?: throw OutOfAttemptsException()
        }

        return currentSequence
    }

    /**
     * Get initial random [Dungeon] from the available pool
     */
    private fun findInitialDungeon(attemptsLimit: Int, visited: List<Dungeon>): Dungeon? {
        return (1..attemptsLimit).firstNotNullOfOrNull {
            getCompatibleDungeon(Random.nextInt(DungeonGenerator.Configuration.ySize), visited)
        }
    }

    /**
     * Get random not used [Dungeon] for random exit of [currentDungeon]
     */
    private fun findNextDungeon(currentDungeon: Dungeon, attemptsLimit: Int, visited: MutableList<Dungeon>): Dungeon? {
        val exits = getExits(currentDungeon)
        return (1..attemptsLimit).firstNotNullOfOrNull {
            getCompatibleDungeon(exits.random(), visited)
        } ?: rollBack(visited).lastOrNull()
    }

    /**
     * Get random not used compatible [Dungeon] for [requiredExit]
     */
    private fun getCompatibleDungeon(requiredExit: Int, visited: List<Dungeon>): Dungeon? {
        return poolIn[requiredExit]?.filterNot { it in visited }?.randomOrNull()
    }

    /**
     * Get list of exit cells' indexes
     */
    private fun getExits(dungeon: Dungeon): List<Int> {
        val values = mutableListOf<Int>()
        dungeon.area.forEachIndexed { index, row -> if (row.last() == Block.AIR) values.add(index) }
        return values
    }

    /**
     * Removes last element of sequence
     */
    private fun rollBack(sequence: MutableList<Dungeon>): MutableList<Dungeon> {
        sequence.removeLastOrNull()
        return sequence
    }

    /**
     * Implements fair recursive bruteforce through the graph. If the possible sequence exists - this algorithm
     * will find it (what ever it takes :)), otherwise, if path doesn't exist - exception will be thrown.
     */
    private fun bruteForceDFS(sequence: Stack<Dungeon>, current: Dungeon, length: Int) {
        if (sequence.size == length) {
            return // Early return if the sequence is already complete
        }

        for (exit in getExits(current)) {
            val compatibleDungeons = poolIn[exit] ?: continue // Skip if no compatible dungeons
            for (compatibleDungeon in compatibleDungeons) {
                if (compatibleDungeon in sequence) {
                    continue
                }

                sequence.push(compatibleDungeon)
                bruteForceDFS(sequence, compatibleDungeon, length)

                if (sequence.size == length) {
                    return          // Return if the sequence is complete
                } else {
                    sequence.pop()  // Remove the last element if not completed
                }
            }
        }
    }
}