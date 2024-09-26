package scripts

import org.tribot.script.sdk.Log
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.antiban.Antiban
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.walking.GlobalWalking
import org.tribot.script.sdk.walking.LocalWalking

fun walkTo(destination: WorldTile, name: String?, setState: (state: String) -> Boolean): Boolean {
    val currentPos = MyPlayer.getTile()

    Log.info("Walking to $name")
    setState("Walking to $name")

    if (currentPos.distanceTo(destination) <= 5) {
        Log.info("We have made it to $name")
        return true
    }

    for (attempt in 0..3) run {
        Log.info("Attempt $attempt to walk to $name")
        if (Antiban.shouldTurnOnRun()) {
            Options.setRunEnabled(true)
        }

        var canWalk = LocalWalking.walkTo(destination)

        if (!canWalk) {
            // Attempt to walk with global walking
            canWalk = GlobalWalking.walkTo(destination)
        }

        if (canWalk) {
            Log.info("Walking to $name")

            val reachedDestination =
                Waiting.waitUntil {
                    MyPlayer.getTile().distanceTo(destination) <= 5 && !MyPlayer.isMoving()
                }

            if (reachedDestination) {
                Log.info("We have made it to $name")
                return true
            } else {
                Log.info("We failed to make it to $name after $attempt attempts")
            }

            if (attempt < 3) {
                Log.info("We will try again")
                Waiting.waitNormal(3000, 1000)
            }
        }
    }

    Log.error("We failed to make it to $name after 3 attempts")
    return false
}
