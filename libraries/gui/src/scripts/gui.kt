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
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonReader
import com.google.gson.TypeAdapter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.util.ScriptSettings

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

  val isScriptStopping = mutableStateOf(false)

  val buryBones = mutableStateOf(false)

  private const val SETTINGS_FILE_NAME = "velox_combat_settings"

  private data class Settings(
    @SerializedName("eatHealthPercentage")
    val eatHealthPercentage: Float,

    @SerializedName("foodToReplenish")
    val foodToReplenish: Int,

    @SerializedName("eatToFull")
    val eatToFull: Boolean,

    @SerializedName("preferredCombatStyle")
    val preferredCombatStyle: Combat.AttackStyle,

    @SerializedName("lootItems")
    val lootItems: List<String>,

    @SerializedName("buryBones")
    val buryBones: Boolean
  )

  private val gsonBuilder = GsonBuilder()
    .setPrettyPrinting()
    .serializeNulls()
    .registerTypeAdapter(Combat.AttackStyle::class.java, object : TypeAdapter<Combat.AttackStyle>() {
      override fun write(out: JsonWriter, value: Combat.AttackStyle?) {
        out.value(value?.name)
      }

      override fun read(`in`: JsonReader): Combat.AttackStyle {
        val style = `in`.nextString()
        return Combat.AttackStyle.valueOf(style)
      }
    }).create()

  private val scriptSettings = ScriptSettings.builder()
    .gson(gsonBuilder)
    .build()

  private fun saveSettings() {
    val settings = Settings(
      eatHealthPercentage.value,
      foodToReplenish.value,
      eatToFull.value,
      preferredCombatStyle.value,
      lootItems.toList(),
      buryBones.value
    )
    scriptSettings.save(SETTINGS_FILE_NAME, settings)
  }

  private fun loadSettings() {
    scriptSettings.load(SETTINGS_FILE_NAME, Settings::class.java).ifPresent { settings ->
      eatHealthPercentage.value = settings.eatHealthPercentage
      foodToReplenish.value = settings.foodToReplenish
      eatToFull.value = settings.eatToFull
      preferredCombatStyle.value = settings.preferredCombatStyle
      lootItems.clear()
      lootItems.addAll(settings.lootItems)
      buryBones.value = settings.buryBones
    }
  }

  init {
    loadSettings()
  }

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

  fun initiateStop() {
    isScriptStopping.value = true
    isRunning.value = false
  }

  fun updateEatHealthPercentage(value: Float) {
    eatHealthPercentage.value = value
    saveSettings()
  }

  fun updateFoodToReplenish(value: Int) {
    foodToReplenish.value = value
    saveSettings()
  }

  fun updateEatToFull(value: Boolean) {
    eatToFull.value = value
    saveSettings()
  }

  fun updatePreferredCombatStyle(style: Combat.AttackStyle) {
    preferredCombatStyle.value = style
    saveSettings()
  }

  fun addLootItem(item: String) {
    lootItems.add(item)
    saveSettings()
  }

  fun removeLootItem(index: Int) {
    lootItems.removeAt(index)
    saveSettings()
  }

  fun updateBuryBones(value: Boolean) {
    buryBones.value = value
    saveSettings()
  }
}

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
                          .verticalScroll(rememberScrollState())) {
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
                  modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(
                        if (VeloxCombatGUIState.isRunning.value) "Stop" else "Start",
                        style = MaterialTheme.typography.button)
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
              onValueChange = { VeloxCombatGUIState.updateEatHealthPercentage(it) },
              valueRange = 1f..99f,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
              "${VeloxCombatGUIState.eatHealthPercentage.value.toInt()}%",
              modifier = Modifier.width(40.dp))
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
              onValueChange = { VeloxCombatGUIState.updateFoodToReplenish(it.roundToInt()) },
              valueRange = 1f..20f,
              steps = 19,
          )
        }
      }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)) {
          Checkbox(
              checked = VeloxCombatGUIState.eatToFull.value,
              onCheckedChange = { VeloxCombatGUIState.updateEatToFull(it) })
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
                  modifier = Modifier.padding(8.dp))
            }
          },
          delayMillis = 200,
          tooltipPlacement = TooltipPlacement.CursorPoint(alignment = Alignment.BottomStart)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier.size(16.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                            CircleShape)) {
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
        modifier = Modifier.fillMaxWidth()) {
          listOf(
                  Combat.AttackStyle.ACCURATE,
                  Combat.AttackStyle.AGGRESSIVE,
                  Combat.AttackStyle.DEFENSIVE,
                  Combat.AttackStyle.CONTROLLED)
              .forEach { style ->
                DropdownMenuItem(
                    onClick = {
                      VeloxCombatGUIState.updatePreferredCombatStyle(style)
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
    // Bury bones checkbox
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)) {
          Checkbox(
              checked = VeloxCombatGUIState.buryBones.value,
              onCheckedChange = { VeloxCombatGUIState.updateBuryBones(it) })
          Spacer(modifier = Modifier.width(8.dp))
          Text("Bury bones")
        }

    Spacer(modifier = Modifier.height(16.dp))

    // Input field and Add button
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
          value = inputValue.value,
          onValueChange = { inputValue.value = it },
          label = { Text("Enter item name") },
          modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.width(8.dp))
      Button(
          onClick = {
            if (inputValue.value.isNotBlank()) {
              VeloxCombatGUIState.addLootItem(inputValue.value.trim())
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
            verticalAlignment = Alignment.CenterVertically) {
              Text(item, modifier = Modifier.weight(1f))
              IconButton(onClick = { VeloxCombatGUIState.removeLootItem(index) }) {
                Text("X", color = MaterialTheme.colors.error)
              }
            }
      }
    }
  }
}