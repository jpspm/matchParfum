package com.matchparfum.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The handful of Material Symbols the app needs, inlined so we don't depend on the
 * (no longer published for Compose Multiplatform) material-icons artifacts.
 */
object AppIcons {
    val Search = icon(
        "Search",
        "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 " +
            "5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5z" +
            "M9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z",
    )
    val Close = icon(
        "Close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
    )
    val ArrowBack = icon(
        "ArrowBack",
        "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z",
        autoMirror = true,
    )
    val OpenInNew = icon(
        "OpenInNew",
        "M19,19H5V5h7V3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-7h-2v7z" +
            "M14,3v2h3.59l-9.83,9.83 1.41,1.41L19,6.41V10h2V3h-7z",
    )
    val Star = icon(
        "Star",
        "M12,17.27L18.18,21l-1.64,-7.03L22,9.24l-7.19,-0.61L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21z",
    )
    val Sparkle = icon(
        "Sparkle",
        "M19,9l1.25,-2.75L23,5l-2.75,-1.25L19,1l-1.25,2.75L15,5l2.75,1.25L19,9z" +
            "M11.5,9.5L9,4 6.5,9.5 1,12l5.5,2.5L9,20l2.5,-5.5L17,12l-5.5,-2.5z" +
            "M19,15l-1.25,2.75L15,19l2.75,1.25L19,23l1.25,-2.75L23,19l-2.75,-1.25L19,15z",
    )
    val CloudOff = icon(
        "CloudOff",
        "M24,15c0,-2.64 -2.05,-4.78 -4.65,-4.96C18.67,6.59 15.64,4 12,4c-1.33,0 -2.57,0.36 -3.65,0.97" +
            "l1.49,1.49C10.51,6.17 11.23,6 12,6c3.04,0 5.5,2.46 5.5,5.5v0.5H19c1.66,0 3,1.34 3,3 " +
            "0,0.99 -0.48,1.85 -1.21,2.4l1.41,1.41c1.09,-0.92 1.8,-2.27 1.8,-3.81z" +
            "M4.41,3.86L3,5.27l2.77,2.77h-0.42C2.34,8.36 0,10.91 0,14c0,3.31 2.69,6 6,6h11.73l2,2" +
            "L21.14,20.59 4.41,3.86zM6,18c-2.21,0 -4,-1.79 -4,-4s1.79,-4 4,-4h1.73l8,8H6z",
    )

    private fun icon(name: String, pathData: String, autoMirror: Boolean = false): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror,
        ).addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black)).build()
}
