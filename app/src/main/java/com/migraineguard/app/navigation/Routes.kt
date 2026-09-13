package com.migraineguard.app.navigation

object Routes {
    const val Onboarding = "onboarding"
    const val SignIn = "sign_in"
    const val ProfileSetup = "profile_setup"
    const val Home = "home"
    const val Migraine = "migraine"
    const val Track = "track"
    const val Food = "food"
    const val Water = "water"
    const val Insights = "insights"
    const val Settings = "settings"
    const val BodyMetrics = "body_metrics"
    const val Medications = "medications"
}

enum class BottomDest(
    val route: String,
    val label: String,
) {
    Home(Routes.Home, "Home"),
    Migraine(Routes.Migraine, "Migraine"),
    Track(Routes.Track, "Fitness"),
    Food(Routes.Food, "Food"),
    Water(Routes.Water, "Water"),
    Insights(Routes.Insights, "Insights"),
}
