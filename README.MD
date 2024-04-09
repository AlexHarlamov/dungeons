# Dungeons
### Overview
Dungeons - it is a small test project, which implements some algorithms for procedure massive generations and
searching graph dependencies.
## Dungeon generator
Provides functionality of parametrized procedure generation of massive (2D dungeon representation).
Usage :
```kotlin
    val dungeon : Dungeon = DungeonGenerator.generate()
```
All the generation parameters are located in [DungeonGenerator.Configuration](src/main/kotlin/service/DungeonGenerator.kt)
## Dungeon pool
Provides functionality of creation compatible sequences of dungeons, i.e. set of the exits of first dungeon overlaps with set of
the enters of the second dungeon etc.
Usage:
```kotlin
    val dungeonPool : DungeonPool = DungeonPool(dungeonList)
    val dungeonSequence : List<Dungeon> = dungeonPool.createSequenceX(lenght)
```
Main algorithm description and estimations are presented in [DungeonPool.kt](src/main/kotlin/DungeonPool.kt)
### Additional references
In the [Main.kt](src/main/kotlin/Main.kt) you can find a small presentation of implementation