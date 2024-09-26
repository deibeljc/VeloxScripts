package scripts.gui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.text.style.TextAlign
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tribot.script.sdk.Combat

object VeloxCombatGUIState {
  val isRunning = mutableStateOf(false)
  val shouldRun: Boolean
    get() = _shouldRun.get()

  val isGUIVisible = mutableStateOf(false)

  private val _shouldRun = AtomicBoolean(true)
  private var guiJob: Job? = null
  private val isGUICreated = mutableStateOf(false)

  val eatHealthPercentage = mutableStateOf(50f)
  val eatToFull = mutableStateOf(false)

  val preferredCombatStyle = mutableStateOf(Combat.AttackStyle.AGGRESSIVE)

  fun launchGUI() {
    if (!isGUICreated.value) {
      guiJob = CoroutineScope(Dispatchers.Default).launch { application { veloxCombatGUI() } }
      isGUICreated.value = true
    }
    isGUIVisible.value = true
  }

  fun closeGUI() {
    isGUIVisible.value = false
  }

  fun stopRunning() {
    _shouldRun.set(false)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun veloxCombatGUI() {
  Window(
      onCloseRequest = { VeloxCombatGUIState.closeGUI() },
      state = rememberWindowState(width = 400.dp, height = 600.dp),
      resizable = false,
      title = "VeloxCombat GUI",
      visible = VeloxCombatGUIState.isGUIVisible.value) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
              // Scrollable content
              Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                // Eating Section
                Text("Eating Settings", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Eat at health %:")
                  Spacer(modifier = Modifier.width(8.dp))
                  Slider(
                      value = VeloxCombatGUIState.eatHealthPercentage.value,
                      onValueChange = { VeloxCombatGUIState.eatHealthPercentage.value = it },
                      valueRange = 1f..99f,
                      modifier = Modifier.weight(1f))
                  Text("${VeloxCombatGUIState.eatHealthPercentage.value.toInt()}%")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)) {
                      Checkbox(
                          checked = VeloxCombatGUIState.eatToFull.value,
                          onCheckedChange = { VeloxCombatGUIState.eatToFull.value = it })
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("Eat to full")
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Combat Section
                Text("Combat Settings", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                      Text("Preferred Combat Style:", style = MaterialTheme.typography.subtitle1)
                      Spacer(modifier = Modifier.width(4.dp))

                      TooltipArea(
                          tooltip = {
                            Surface(
                                modifier = Modifier.shadow(4.dp),
                                shape = RoundedCornerShape(4.dp)) {
                                  Text(
                                      text =
                                          "The script will use the preferred style unless there's a skill imbalance:\n" +
                                              "- If all skills are within 5 levels, use preferred style\n" +
                                              "- Otherwise, it prioritizes training the lowest skill\n" +
                                              "- If skills are balanced, it defaults to Aggressive for max hit",
                                      modifier = Modifier.padding(8.dp))
                                }
                          },
                          delayMillis = 200,
                          tooltipPlacement =
                              TooltipPlacement.CursorPoint(alignment = Alignment.BottomStart)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                            ) {
                                Text(
                                    "?",
                                    style = MaterialTheme.typography.caption,
                                    textAlign = TextAlign.Center
                                )
                            }
                          }
                    }

                val expandedState = remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { expandedState.value = true }, modifier = Modifier.fillMaxWidth()) {
                      Text(VeloxCombatGUIState.preferredCombatStyle.value.toString())
                    }

                DropdownMenu(
                    expanded = expandedState.value,
                    onDismissRequest = { expandedState.value = false },
                    modifier = Modifier.fillMaxWidth()) {
                      listOf(
                              Combat.AttackStyle.ACCURATE,
                              Combat.AttackStyle.AGGRESSIVE,
                              Combat.AttackStyle.DEFENSIVE,
                              Combat.AttackStyle.CONTROLLED)
                          .forEach { style ->
                            DropdownMenuItem(
                                onClick = {
                                  VeloxCombatGUIState.preferredCombatStyle.value = style
                                  expandedState.value = false
                                }) {
                                  Text(style.toString())
                                }
                          }
                    }
              }
              Button(
                  onClick = {
                    VeloxCombatGUIState.isRunning.value = !VeloxCombatGUIState.isRunning.value
                  },
                  modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(
                        if (VeloxCombatGUIState.isRunning.value) "Stop" else "Start",
                        style = MaterialTheme.typography.button)
                  }
            }
      }
}
