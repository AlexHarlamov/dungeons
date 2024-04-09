package service

import model.Block
import model.Dungeon
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Implementation level of Dungeon generator logic. Implements Dungeon's procedure generation.
 */
object DungeonGenerator {

    private val random = Random()

    /**
     * Parameters for the form-factor of generated [Dungeon]s
     */
    object Configuration {
        const val xSize = 7               // Size of the dungeon horizontally
        const val ySize = 4               // Size of the dungeon vertically
        const val stdDeviationY = 0.4     // Deviation from the main path exits (increase to make the variety wider)
        const val stdDeviationX = 0.2     // Deviation from the main path (increase to make the path wider)
        const val mean = 0.0              // All the deviations are based on Gauss normal distribution, this parameter controls its mean
    }

    /**
     * Main builder's entry point.
     * Procedure of dungeon creation is based on the idea, that all the dungeons should have at least one entrance
     * and at least one exit. So that, the main "skeleton" of the [Dungeon] it is its path through. The algorithm is
     * separated on the few simple steps :
     *  1. Create main points (turns) of the path
     *  2. Connect points between each other (create path through)
     *  3. Expand path to create dungeon interior
     */
    fun generate(): Dungeon {

        val rowsArray = Array(Configuration.ySize) {
            Array(Configuration.xSize) {
                Block.GROUND
            }
        }

        generateMainNodes(rowsArray)        // Generates main path pattern
        spreadXMain(rowsArray)              // Builds carcase for path building
        drawPath(rowsArray)                 // Finishes the main path through dungeon
        spreadYMain(rowsArray)              // Creates additional space (generates variety for exits)
        spreadXPath(rowsArray)              // Creates additional space (generates variety for interior)

        return Dungeon(rowsArray)
    }

    /**
     * Generates main path nodes (turns), randomly spreading them all the way through the dungeon.
     */
    private fun generateMainNodes(area: Array<Array<Block>>) {
        for (x in 0 until Configuration.xSize step 2) {
            val nodeYIndex = random.nextInt(Configuration.ySize)
            area[nodeYIndex][x] = Block.AIR
        }
    }

    /**
     * Provides additional spaces for the future path generation
     */
    private fun spreadXMain(area: Array<Array<Block>>) {
        for (y in 0 until Configuration.ySize) {
            for (x in 0 until Configuration.xSize step 2) {
                if (area[y][x] == Block.AIR) {
                    area[y][add(x, 1, Configuration.xSize)] = Block.AIR
                    area[y][minus(x, 1)] = Block.AIR
                }
            }
        }
    }

    /**
     * Generates paths between the nodes
     */
    private fun drawPath(area: Array<Array<Block>>) {
        for (x in minOf(1, Configuration.xSize) until Configuration.xSize step 2) {
            var isPath = false
            for (y in 0 until Configuration.ySize) {
                if (area[y][x] == Block.AIR) {
                    isPath = !isPath
                } else {
                    if (isPath) {
                        area[y][x] = Block.AIR
                    }
                }
            }
        }
    }

    /**
     * Generates additional spaces around main nodes
     */
    private fun spreadYMain(area: Array<Array<Block>>) {
        for (x in 0 until Configuration.xSize step 2) {
            for (y in 0 until Configuration.ySize) {
                if (area[y][x] == Block.AIR) {
                    val spreadUp = gaussianRandomSpread(Configuration.mean, Configuration.stdDeviationY)
                    val spreadDown = gaussianRandomSpread(Configuration.mean, Configuration.stdDeviationY)
                    area[minus(y, spreadUp)][x] = Block.AIR
                    area[add(y, spreadDown, Configuration.ySize)][x] = Block.AIR
                    break
                }
            }
        }
    }

    /**
     * Generates additional spaces all the way through the dungeon
     */
    private fun spreadXPath(area: Array<Array<Block>>) {
        for (x in minOf(1, Configuration.xSize) until Configuration.xSize step 2) {
            for (y in 0 until Configuration.ySize) {
                if (area[y][x] == Block.AIR) {
                    val spreadRight = gaussianRandomSpread(Configuration.mean, Configuration.stdDeviationX)
                    val spreadLeft = gaussianRandomSpread(Configuration.mean, Configuration.stdDeviationX)
                    area[y][add(x, spreadRight, Configuration.xSize)] = Block.AIR
                    area[y][minus(x, spreadLeft)] = Block.AIR
                }
            }
        }
    }

    /**
     * Generates and normalizes random values in Gauss normal distribution
     */
    private fun gaussianRandomSpread(mean: Double, stdDeviation: Double): Int {
        return (mean + stdDeviation * random.nextGaussian()).roundToInt().absoluteValue
    }

    /**
     * Normalizes coordinates from the upper bound
     */
    private fun add(base: Int, change: Int, lim: Int): Int {
        val res = base + change
        return if (res >= lim) lim - 1 else res
    }

    /**
     * Normalizes coordinates from the lower bound
     */
    private fun minus(base: Int, change: Int): Int {
        val res = base - change
        return if (res <= 0) 0 else res
    }
}
