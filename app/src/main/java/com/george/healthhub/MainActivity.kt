package com.george.healthhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val health = HealthConnectManager(this)
        setContent { HealthHubApp(health) }
    }
}

@Composable
fun HealthHubApp(health: HealthConnectManager, vm: AppViewModel = viewModel()) {
    val accent by vm.accent.collectAsState()
    val scheme = lightColorScheme(
        primary = accent.color,
        secondary = accent.color.copy(alpha = .82f),
        primaryContainer = accent.color.copy(alpha = .13f),
        surface = Color(0xFFFFFBFE),
        surfaceVariant = Color(0xFFF3F1F4),
        background = Color(0xFFFFFBFE)
    )
    MaterialTheme(colorScheme = scheme, typography = Typography()) {
        HealthHubScaffold(health = health, accent = accent, onAccent = vm::setAccent)
    }
}

@Composable
private fun HealthHubScaffold(health: HealthConnectManager, accent: Accent, onAccent: (Accent) -> Unit) {
    var tab by remember { mutableStateOf(AppTab.Today) }
    var settings by remember { mutableStateOf(false) }
    var selectedMetric by remember { mutableStateOf<Metric?>(null) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var selectedMeal by remember { mutableStateOf<Meal?>(null) }
    var pantry by remember { mutableStateOf(DemoData.pantry) }
    val permissionLauncher = rememberLauncherForActivityResult(health.permissionContract) { }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (settings) "Settings" else titleFor(tab), fontWeight = FontWeight.SemiBold)
                        if (!settings && tab == AppTab.Today) Text("Tuesday, 4 August", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    if (settings || selectedMetric != null || selectedWorkout != null || selectedMeal != null) {
                        IconButton(onClick = { settings = false; selectedMetric = null; selectedWorkout = null; selectedMeal = null }) { Icon(Icons.Outlined.ArrowBack, "Back") }
                    }
                },
                actions = { if (!settings) IconButton(onClick = { settings = true }) { Icon(Icons.Outlined.Settings, "Settings") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (!settings && selectedMetric == null && selectedWorkout == null && selectedMeal == null) {
                NavigationBar {
                    AppTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(iconFor(item), item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                settings -> SettingsScreen(accent, onAccent, health) { permissionLauncher.launch(health.permissions) }
                selectedMetric != null -> MetricDetail(selectedMetric!!)
                selectedWorkout != null -> WorkoutDetail(selectedWorkout!!)
                selectedMeal != null -> MealDetail(selectedMeal!!, pantry)
                tab == AppTab.Today -> TodayScreen(
                    onMetric = { selectedMetric = it },
                    onWorkout = { tab = AppTab.Training },
                    onFood = { tab = AppTab.Food }
                )
                tab == AppTab.Food -> FoodScreen { selectedMeal = it }
                tab == AppTab.Kitchen -> KitchenScreen(pantry, { pantry = it }, DemoData.week.flatten())
                tab == AppTab.Health -> HealthScreen { selectedMetric = it }
                else -> TrainingScreen { selectedWorkout = it }
            }
        }
    }
}

private fun titleFor(tab: AppTab) = when (tab) {
    AppTab.Today -> "Good morning, George"
    AppTab.Food -> "Food & meal plan"
    AppTab.Kitchen -> "Kitchen & groceries"
    AppTab.Health -> "Health & body"
    AppTab.Training -> "Training"
}

private fun iconFor(tab: AppTab): ImageVector = when (tab) {
    AppTab.Today -> Icons.Outlined.Home
    AppTab.Food -> Icons.Outlined.Restaurant
    AppTab.Kitchen -> Icons.Outlined.Kitchen
    AppTab.Health -> Icons.Outlined.FavoriteBorder
    AppTab.Training -> Icons.Outlined.DirectionsBike
}

@Composable private fun Page(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) } }
}

@Composable private fun HubCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .68f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable private fun SectionTitle(title: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        trailing?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable private fun TodayScreen(onMetric: (Metric) -> Unit, onWorkout: () -> Unit, onFood: () -> Unit) = Page {
    HubCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(.74f, Modifier.fillMaxSize(), strokeWidth = 10.dp, trackColor = MaterialTheme.colorScheme.surface)
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("1,840", fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("of 2,500 kcal", style = MaterialTheme.typography.labelSmall) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroRow("Protein", 112, 156)
                MacroRow("Carbs", 184, 260)
                MacroRow("Fat", 58, 78)
            }
        }
    }
    SectionTitle("Today at a glance", "Up to date")
    val headline = listOf("Sleep", "Steps", "Weight", "Resting HR")
    headline.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.forEach { name ->
                val metric = DemoData.metrics.first { it.name == name }
                Card(Modifier.weight(1f).clickable { onMetric(metric) }, shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(metric.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(metric.display, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        Text(metric.average, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
    SectionTitle("Latest training", "View training")
    HubCard(Modifier.clickable(onClick = onWorkout)) {
        Text("THIS MORNING · CYCLING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text("Richmond Park loops", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Stat("42.6 km", "Distance"); Stat("1:34", "Time"); Stat("146 bpm", "Avg HR") }
    }
    SectionTitle("Today’s food", "Log food")
    HubCard(Modifier.clickable(onClick = onFood)) { DemoData.meals.forEach { MealRow(it) } }
}

@Composable private fun MacroRow(label: String, current: Int, target: Int) {
    Column { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, style = MaterialTheme.typography.labelMedium); Text("$current / $target g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }; LinearProgressIndicator(current.toFloat()/target, Modifier.fillMaxWidth().padding(top = 4.dp)) }
}

@Composable private fun Stat(value: String, label: String) { Column { Text(value, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall) } }

@Composable private fun MealRow(meal: Meal) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Icon(Icons.Outlined.Restaurant, null, Modifier.padding(9.dp), tint = MaterialTheme.colorScheme.primary) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(meal.name, fontWeight = FontWeight.Medium); Text("${meal.slot} · ${if (meal.logged) "logged" else "planned"}", style = MaterialTheme.typography.labelSmall) }
        Text("${meal.calories}", fontWeight = FontWeight.Bold)
    }
}

@Composable private fun FoodScreen(onMeal: (Meal) -> Unit) {
    var day by remember { mutableIntStateOf(1) }
    Page {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("M","T","W","T","F","S","S").forEachIndexed { index, label ->
                FilterChip(selected = day == index, onClick = { day = index }, label = { Text(label) })
            }
        }
        DemoData.week[day].forEach { meal ->
            HubCard(Modifier.clickable { onMeal(meal) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${meal.slot.uppercase()} · ${meal.calories} KCAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Text("${meal.protein} g protein", style = MaterialTheme.typography.labelSmall) }
                Text(meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { meal.ingredients.take(3).forEach { AssistChip(onClick = {}, label = { Text("${it.name} ${formatQty(it.quantity)}${it.unit}") }) } }
            }
        }
        FilledTonalButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Add custom food or recipe") }
    }
}

@Composable private fun MealDetail(meal: Meal, pantry: List<PantryItem>) = Page {
    Text(meal.slot.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Text(meal.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    HubCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Stat("${meal.calories}", "kcal"); Stat("${meal.protein} g", "Protein"); Stat("${meal.carbs} g", "Carbs"); Stat("${meal.fat} g", "Fat") } }
    SectionTitle("What you need")
    HubCard {
        meal.ingredients.forEach { ingredient ->
            val available = pantry.firstOrNull { it.name.equals(ingredient.name, true) }?.quantity ?: 0.0
            val missing = max(0.0, ingredient.quantity - available)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text(ingredient.name, fontWeight = FontWeight.Medium); Text("Need ${formatQty(ingredient.quantity)}${ingredient.unit}", style = MaterialTheme.typography.labelSmall) }
                Text(if (missing == 0.0) "In stock" else "Get ${formatQty(missing)}${ingredient.unit}", color = if (missing == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Log this meal") }
}

@Composable private fun KitchenScreen(pantry: List<PantryItem>, update: (List<PantryItem>) -> Unit, meals: List<Meal>) {
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    val groceries = remember(pantry) { calculateGroceries(meals, pantry) }
    Page {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search fridge or pantry") }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
        SectionTitle("In your kitchen", "${pantry.size} items")
        pantry.filter { it.name.contains(query, true) }.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { item -> Card(Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(14.dp)) { Text(item.name, fontWeight = FontWeight.Bold); Text("${formatQty(item.quantity)} ${item.unit}", style = MaterialTheme.typography.labelMedium) } }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } }
        OutlinedButton(onClick = { showAdd = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text(" Add item") }
        SectionTitle("Weekly shopping list", "${groceries.size} missing")
        HubCard { groceries.forEach { item -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Checkbox(false, {}); Column { Text(item.name, fontWeight = FontWeight.Medium); Text("Buy ${formatQty(item.quantity)} ${item.unit}", style = MaterialTheme.typography.labelSmall) } } } }
    }
    if (showAdd) AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Add to kitchen") }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Food") }); OutlinedTextField(qty, { qty = it }, label = { Text("Quantity") }) } }, confirmButton = { TextButton(onClick = { val amount = qty.toDoubleOrNull(); if (name.isNotBlank() && amount != null) update(pantry + PantryItem(name, amount, "g")); showAdd = false; name = ""; qty = "" }) { Text("Add") } }, dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } })
}

private fun calculateGroceries(meals: List<Meal>, pantry: List<PantryItem>): List<GroceryItem> {
    val needed = meals.flatMap { it.ingredients }.groupBy { it.name to it.unit }.mapValues { (_, rows) -> rows.sumOf { it.quantity } }
    return needed.mapNotNull { (key, need) -> val have = pantry.firstOrNull { it.name.equals(key.first, true) && it.unit == key.second }?.quantity ?: 0.0; (need-have).takeIf { it > 0 }?.let { GroceryItem(key.first, it, key.second) } }
}

@Composable private fun HealthScreen(onMetric: (Metric) -> Unit) = Page {
    val weight = DemoData.metrics.first()
    Text("LATEST · TODAY 07:42", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    Text(weight.display, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
    Text("7-day average ${weight.average.substringBefore(" average")}")
    HubCard { SectionTitle("Weight trend", "−1.8 kg this month"); Sparkline(weight.values, Modifier.fillMaxWidth().height(150.dp)) }
    SectionTitle("All measurements", "30-day trends")
    DemoData.metrics.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { metric -> MetricCard(metric, Modifier.weight(1f)) { onMetric(metric) } }; if (row.size == 1) Spacer(Modifier.weight(1f)) } }
    Text("Calculated body-composition estimates are useful as trends, not medical measurements.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun MetricCard(metric: Metric, modifier: Modifier, click: () -> Unit) { Card(modifier.clickable(onClick = click), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(13.dp)) { Text(metric.name, style = MaterialTheme.typography.labelMedium); Text(metric.display, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 5.dp)); Sparkline(metric.values, Modifier.fillMaxWidth().height(42.dp)); Text(metric.average, style = MaterialTheme.typography.labelSmall) } } }

@Composable private fun MetricDetail(metric: Metric) {
    var range by remember { mutableStateOf("Month") }
    val avg = metric.values.average()
    Page {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("Week","Month","3 months","Year").forEach { FilterChip(range == it, { range = it }, { Text(it) }) } }
        Text("${range.uppercase()} AVERAGE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text("${formatMetric(avg, metric.unit)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SummaryBox("Latest", metric.display, Modifier.weight(1f)); SummaryBox("Lowest", formatMetric(metric.values.min().toDouble(), metric.unit), Modifier.weight(1f)); SummaryBox("Highest", formatMetric(metric.values.max().toDouble(), metric.unit), Modifier.weight(1f)) }
        HubCard { SectionTitle("$range trend"); Sparkline(metric.values, Modifier.fillMaxWidth().height(190.dp)) }
        SectionTitle("All readings")
        HubCard { metric.values.reversed().forEachIndexed { index, value -> Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(if(index == 0) "Today" else "${index * 4} days ago"); Text(formatMetric(value.toDouble(), metric.unit), fontWeight = FontWeight.Bold) } } }
    }
}

@Composable private fun SummaryBox(label: String, value: String, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall); Text(value, fontWeight = FontWeight.Bold) } } }

@Composable private fun Sparkline(values: List<Float>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        drawLine(grid, Offset(0f,size.height*.25f), Offset(size.width,size.height*.25f))
        drawLine(grid, Offset(0f,size.height*.75f), Offset(size.width,size.height*.75f))
        val lo = values.min(); val hi = values.max(); val spread = max(hi-lo, .1f)
        val path = Path()
        values.forEachIndexed { index, value -> val x = index * size.width / max(1, values.lastIndex); val y = size.height - ((value-lo)/spread)*size.height*.78f - size.height*.11f; if(index==0) path.moveTo(x,y) else path.lineTo(x,y) }
        drawPath(path, color, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

@Composable private fun TrainingScreen(onWorkout: (Workout) -> Unit) = Page {
    Text("THIS WEEK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    Text("4 h 18 min", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
    Text("4 activities · 96.4 km · +12% vs last week")
    HubCard { SectionTitle("Training load", "Last 7 days"); Sparkline(listOf(28f,72f,12f,48f,90f,18f,62f), Modifier.fillMaxWidth().height(110.dp)) }
    SectionTitle("Recent activities")
    HubCard { DemoData.workouts.forEach { workout -> Row(Modifier.fillMaxWidth().clickable { onWorkout(workout) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Icon(if(workout.type == "Cycling") Icons.Outlined.DirectionsBike else Icons.Outlined.FitnessCenter, null, Modifier.padding(10.dp)) }; Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(workout.name, fontWeight = FontWeight.Bold); Text("${workout.date} · ${workout.duration}", style = MaterialTheme.typography.labelSmall) }; Text("${workout.calories} kcal", fontWeight = FontWeight.Bold) } } }
}

@Composable private fun WorkoutDetail(workout: Workout) = Page {
    Text(workout.date.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    Text(workout.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(workout.type)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryBox("Duration", workout.duration, Modifier.weight(1f)); SummaryBox("Active calories", "${workout.calories} kcal", Modifier.weight(1f)) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryBox("Average HR", workout.heartRate, Modifier.weight(1f)); SummaryBox("Time in zones", if(workout.type == "Strength training") "31 min" else "38 min", Modifier.weight(1f)) }
    HubCard { SectionTitle("Heart rate", "Workout duration"); Sparkline(listOf(112f,138f,124f,153f,132f,160f,128f,151f,139f,164f,132f,146f), Modifier.fillMaxWidth().height(160.dp)) }
    HubCard { Text(if (workout.type == "Strength training") "This imported workout includes timing, calories and available heart-rate data. Exercise, set, rep and weight tracking stays ready for the later strength-tracking phase." else "This activity summary was imported through Health Connect. Source apps remain behind the scenes so all your data lives in one view.") }
}

@Composable private fun SettingsScreen(accent: Accent, onAccent: (Accent) -> Unit, health: HealthConnectManager, requestHealth: () -> Unit) = Page {
    SectionTitle("Appearance")
    HubCard {
        Text("App colour", fontWeight = FontWeight.Bold)
        Text("Choose the accent used across charts, buttons and progress rings.", style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Accent.entries.forEach { choice -> Box(Modifier.size(42.dp).background(choice.color, CircleShape).then(if(choice == accent) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier).clickable { onAccent(choice) }, contentAlignment = Alignment.Center) { if(choice == accent) Icon(Icons.Outlined.Check, choice.label, tint = Color.White) } }
        }
        Text(accent.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
    SectionTitle("Health data")
    HubCard {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.FavoriteBorder, null); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text("Health Connect", fontWeight = FontWeight.Bold); Text(if(health.client != null) "Available · permission required" else "Not available on this device", style = MaterialTheme.typography.labelSmall) } }
        Button(onClick = requestHealth, enabled = health.client != null, modifier = Modifier.fillMaxWidth()) { Text("Connect health data") }
        Text("Reads activity, sleep, weight, body measurements and workout summaries. Nutrition can be written back with your permission.", style = MaterialTheme.typography.bodySmall)
    }
    SectionTitle("Behind the scenes")
    HubCard { Text("Samsung Health, RENPHO, Strava and Hevy can continue supplying records to Health Connect. The main app experience stays source-neutral, as requested.") }
    SectionTitle("About")
    HubCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Health Hub MVP"); Text("0.1.0") }; Text("Detailed strength sets, reps and weights are scheduled for a later phase.", style = MaterialTheme.typography.bodySmall) }
}

private fun formatQty(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
private fun formatMetric(value: Double, unit: String): String = (if (value >= 100) "%.0f" else "%.1f").format(value) + if(unit.isBlank()) "" else " $unit"
