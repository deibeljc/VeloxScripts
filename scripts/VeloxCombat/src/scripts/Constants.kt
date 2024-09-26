package scripts

import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.WorldTile

// The fishing area
val FISHING_AREA =
    Area.fromPolygon(
        listOf(
            WorldTile(3246, 3159, 0),
            WorldTile(3246, 3156, 0),
            WorldTile(3247, 3153, 0),
            WorldTile(3243, 3150, 0),
            WorldTile(3241, 3148, 0),
            WorldTile(3240, 3146, 0),
            WorldTile(3235, 3147, 0),
            WorldTile(3233, 3151, 0),
            WorldTile(3240, 3157, 0),
            WorldTile(3243, 3160, 0)
        )
    )
