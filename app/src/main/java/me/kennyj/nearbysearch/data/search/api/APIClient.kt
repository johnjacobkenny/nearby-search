package me.kennyj.nearbysearch.data.search.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIClient {
    companion object {
        fun getService(): NearbySearchService {
            return Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(NearbySearchService::class.java)
        }
    }
}