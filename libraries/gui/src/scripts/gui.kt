package scripts.gui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tribot.script.sdk.Combat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

object VeloxCombatGUIState {
  val isRunning = mutableStateOf(false)
  val shouldRun: Boolean
    get() = _shouldRun.get()

  val isGUIVisible = mutableStateOf(false)

  private val _shouldRun = AtomicBoolean(true)
  private var guiJob: Job? = null
  private val isGUICreated = mutableStateOf(false)

  val eatHealthPercentage = mutableStateOf(50f)
  val foodToReplenish = mutableStateOf(10)
  val eatToFull = mutableStateOf(false)

  val preferredCombatStyle = mutableStateOf(Combat.AttackStyle.AGGRESSIVE)

  val lootItems = mutableStateListOf<String>()

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

@Composable
fun veloxCombatGUI() {
  Window(
    onCloseRequest = { VeloxCombatGUIState.closeGUI() },
    state = rememberWindowState(width = 400.dp, height = 600.dp),
    resizable = false,
    title = "VeloxCombat GUI",
    visible = VeloxCombatGUIState.isGUIVisible.value
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      val tabIndex = remember { mutableStateOf(0) }
      val tabs = listOf("Eating", "Combat", "Looting")

      TabRow(selectedTabIndex = tabIndex.value) {
        tabs.forEachIndexed { index, title ->
          Tab(
            text = { Text(title) },
            selected = tabIndex.value == index,
            onClick = { tabIndex.value = index })
        }
      }

      // Scrollable content
      Box(
        modifier =
        Modifier.padding(top = 16.dp)
          .weight(1f)
          .verticalScroll(rememberScrollState())
      ) {
        when (tabIndex.value) {
          0 -> EatingSettings()
          1 -> CombatSettings()
          2 -> LootingSettings()
        }
      }

      Button(
        onClick = {
          VeloxCombatGUIState.isRunning.value = !VeloxCombatGUIState.isRunning.value
        },
        modifier = Modifier.fillMaxWidth().height(56.dp)
      ) {
        Text(
          if (VeloxCombatGUIState.isRunning.value) "Stop" else "Start",
          style = MaterialTheme.typography.button
        )
      }
    }
  }
}

@Composable
fun EatingSettings() {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Eat at health %:", modifier = Modifier.width(120.dp))
      Box(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Slider(
            value = VeloxCombatGUIState.eatHealthPercentage.value,
            onValueChange = { VeloxCombatGUIState.eatHealthPercentage.value = it },
            valueRange = 1f..99f,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            "${VeloxCombatGUIState.eatHealthPercentage.value.toInt()}%",
            modifier = Modifier.width(40.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Food to replenish:", modifier = Modifier.width(120.dp))
      Box(modifier = Modifier.weight(1f)) {
        Text("${VeloxCombatGUIState.foodToReplenish.value}", modifier = Modifier.width(40.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Slider(
            value = VeloxCombatGUIState.foodToReplenish.value.toFloat(),
            onValueChange = { VeloxCombatGUIState.foodToReplenish.value = it.roundToInt() },
            valueRange = 1f..20f,
            steps = 19,
          )
        }
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(vertical = 8.dp)
    ) {
      Checkbox(
        checked = VeloxCombatGUIState.eatToFull.value,
        onCheckedChange = { VeloxCombatGUIState.eatToFull.value = it })
      Spacer(modifier = Modifier.width(8.dp))
      Text("Eat to full")
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CombatSettings() {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Text("Preferred Combat Style:", style = MaterialTheme.typography.subtitle1)
      Spacer(modifier = Modifier.width(4.dp))

      TooltipArea(
        tooltip = {
          Surface(modifier = Modifier.shadow(4.dp), shape = RoundedCornerShape(4.dp)) {
            Text(
              text =
              "The script will use the preferred style unless there's a skill imbalance:\n" +
                      "- If all skills are within 5 levels, use preferred style\n" +
                      "- Otherwise, it prioritizes training the lowest skill\n" +
                      "- If skills are balanced, it defaults to Aggressive for max hit",
              modifier = Modifier.padding(8.dp)
            )
          }
        },
        delayMillis = 200,
        tooltipPlacement = TooltipPlacement.CursorPoint(alignment = Alignment.BottomStart)
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier =
          Modifier.size(16.dp)
            .border(
              1.dp,
              MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
              CircleShape
            )
        ) {
          Text("?", style = MaterialTheme.typography.caption, textAlign = TextAlign.Center)
        }
      }
    }

    val expandedState = remember { mutableStateOf(false) }

    OutlinedButton(onClick = { expandedState.value = true }, modifier = Modifier.fillMaxWidth()) {
      Text(VeloxCombatGUIState.preferredCombatStyle.value.toString())
    }

    DropdownMenu(
      expanded = expandedState.value,
      onDismissRequest = { expandedState.value = false },
      modifier = Modifier.fillMaxWidth()
    ) {
      listOf(
        Combat.AttackStyle.ACCURATE,
        Combat.AttackStyle.AGGRESSIVE,
        Combat.AttackStyle.DEFENSIVE,
        Combat.AttackStyle.CONTROLLED
      )
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
}

@Composable
fun LootingSettings() {
  val inputValue = remember { mutableStateOf("") }

  Column {
    // Input field and Add button
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = inputValue.value,
        onValueChange = { inputValue.value = it },
        label = { Text("Enter item name") },
        modifier = Modifier.weight(1f)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = {
          if (inputValue.value.isNotBlank()) {
            VeloxCombatGUIState.lootItems.add(inputValue.value.trim())
            inputValue.value = ""
          }
        }) {
        Text("Add")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // List of loot items
    Text("Items to Loot:", style = MaterialTheme.typography.subtitle1)
    Spacer(modifier = Modifier.height(8.dp))

    if (VeloxCombatGUIState.lootItems.isEmpty()) {
      Text("No items added yet", style = MaterialTheme.typography.body2)
    } else {
      VeloxCombatGUIState.lootItems.forEachIndexed { index, item ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(item, modifier = Modifier.weight(1f))
          IconButton(onClick = { VeloxCombatGUIState.lootItems.removeAt(index) }) {
            Text("X", color = MaterialTheme.colors.error)
          }
        }
      }
    }
  }
}
