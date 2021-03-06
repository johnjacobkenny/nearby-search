package me.kennyj.nearbysearch.data.search.api

import me.kennyj.nearbysearch.data.search.entities.NearbyRestaurantResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NearbySearchService {

    @GET("json?&radius=25000&type=restaurant")
    suspend fun getNearbyRestaurants(
        @Query("key") key: String,
        @Query("location") location: String
    ): NearbyRestaurantResponse
}