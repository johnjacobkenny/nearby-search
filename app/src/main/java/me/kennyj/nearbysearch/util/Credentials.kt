package me.kennyj.nearbysearch.util

import android.content.pm.PackageManager

const val PACKAGE_NAME = "me.kennyj.nearbysearch"
const val METADATA_KEY_NAME = "me.kennyj.nearbysearch.PLACES_API_KEY"

fun getPlacesAPIKey(packageManager: PackageManager): String? {
    val applicationInfo =
        packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)
    val bundle = applicationInfo.metaData

    return bundle.getString(METADATA_KEY_NAME)
}
