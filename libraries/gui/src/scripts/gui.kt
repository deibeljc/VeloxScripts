package scripts.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

  fun launchGUI() {
    if (!isGUICreated.value) {
      guiJob = CoroutineScope(Dispatchers.Default).launch { application { VeloxCombatGUI() } }
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

@Composable
fun VeloxCombatGUI() {
  Window(
      onCloseRequest = { VeloxCombatGUIState.closeGUI() },
      state = rememberWindowState(width = 300.dp, height = 600.dp),
      resizable = false,
      title = "VeloxCombat GUI",
      visible = VeloxCombatGUIState.isGUIVisible.value) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween) {
              Column {
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

                // Combat Section (empty for now)
                Text("Combat Settings", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Combat settings will be added here.", style = MaterialTheme.typography.body2)
              }

              // Start/Stop Button
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
