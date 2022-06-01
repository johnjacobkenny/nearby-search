package me.kennyj.nearbysearch.data.entities

data class NearbyRestaurantResponse(
    val html_attributions: List<Any>,
    val next_page_token: String,
    val results: List<Result>,
    val status: String
)