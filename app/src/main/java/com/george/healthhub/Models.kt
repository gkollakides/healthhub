package com.george.healthhub

import androidx.compose.ui.graphics.Color

enum class AppTab(val label: String) { Today("Today"), Food("Food"), Kitchen("Kitchen"), Health("Health"), Training("Training") }

data class Meal(
    val name: String,
    val slot: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val ingredients: List<Ingredient>,
    val logged: Boolean = false
)

data class Ingredient(val name: String, val quantity: Double, val unit: String)
data class PantryItem(val name: String, val quantity: Double, val unit: String)
data class GroceryItem(val name: String, val quantity: Double, val unit: String, val checked: Boolean = false)
data class Metric(val name: String, val display: String, val average: String, val values: List<Float>, val unit: String)
data class Workout(val name: String, val type: String, val date: String, val duration: String, val calories: Int, val heartRate: String)

enum class Accent(val label: String, val color: Color) {
    Blue("Ocean", Color(0xFF006A68)),
    Green("Forest", Color(0xFF386A20)),
    Purple("Iris", Color(0xFF6750A4)),
    Orange("Sunset", Color(0xFF9C4327)),
    Pink("Berry", Color(0xFF9C405B)),
    Slate("Slate", Color(0xFF455A64))
}

object DemoData {
    val meals = listOf(
        Meal("Eggs, bacon & toast", "Breakfast", 460, 30, 34, 22, listOf(Ingredient("Eggs", 3.0, ""), Ingredient("Bacon", 80.0, "g"), Ingredient("Bread", 2.0, "slices")), true),
        Meal("Chicken, rice & feta", "Lunch", 719, 62, 72, 20, listOf(Ingredient("Chicken thighs", 220.0, "g"), Ingredient("Rice", 100.0, "g"), Ingredient("Feta", 65.0, "g")), true),
        Meal("Cake + ham bagel", "Snack", 705, 34, 91, 23, listOf(Ingredient("Bagels", 1.0, ""), Ingredient("Ham", 100.0, "g"), Ingredient("Cake", 1.0, "slice")), true),
        Meal("White fish, orzo & salad", "Dinner", 600, 47, 68, 14, listOf(Ingredient("White fish", 190.0, "g"), Ingredient("Orzo", 180.0, "g"), Ingredient("Mixed salad", 150.0, "g")))
    )
    val week = List(7) { meals }
    val pantry = listOf(PantryItem("Chicken thighs", 820.0, "g"), PantryItem("Eggs", 8.0, ""), PantryItem("Rice", 450.0, "g"), PantryItem("Feta", 80.0, "g"), PantryItem("Orzo", 300.0, "g"), PantryItem("Ham", 300.0, "g"))
    val metrics = listOf(
        Metric("Weight", "84.2 kg", "84.6 kg average", listOf(86f,85.7f,85.3f,85f,84.8f,84.5f,84.2f), "kg"),
        Metric("Body fat", "22.8%", "23.0% average", listOf(23.4f,23.3f,23.2f,23.2f,23f,22.9f,22.8f), "%"),
        Metric("Body water", "54.1%", "53.9% average", listOf(53.7f,53.8f,53.8f,54f,53.9f,54f,54.1f), "%"),
        Metric("Skeletal muscle", "42.6%", "42.4% average", listOf(42.1f,42.2f,42.2f,42.3f,42.4f,42.5f,42.6f), "%"),
        Metric("Sleep", "7 h 42 min", "7 h 28 min average", listOf(7.1f,7.6f,6.9f,7.8f,7.4f,7.2f,7.7f), "h"),
        Metric("Steps", "8,430", "8,140 average", listOf(7200f,8100f,9400f,6800f,10200f,7900f,8430f), ""),
        Metric("Resting HR", "58 bpm", "59 bpm average", listOf(61f,60f,60f,59f,60f,58f,58f), "bpm")
    )
    val workouts = listOf(
        Workout("Richmond Park loops", "Cycling", "Today · 06:48", "1 h 34 min", 812, "146 bpm"),
        Workout("Evening run", "Running", "Wednesday · 18:22", "44 min", 526, "152 bpm"),
        Workout("Upper body", "Strength training", "Tuesday · 18:12", "48 min", 284, "122 bpm")
    )
}
