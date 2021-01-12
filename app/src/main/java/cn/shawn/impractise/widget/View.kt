package cn.shawn.impractise.widget

import android.graphics.Rect
import android.view.View

fun View.getStatusBarHeight() : Int {
    var statusBarHeight = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}

fun View.getBottomBarHeight() :Int {
    try {
        val attachInfoField = View::class.java.getDeclaredField("mAttachInfo")
        attachInfoField.isAccessible = true
        val attachInfo = attachInfoField[this]
        if (attachInfo != null) {
            val stableInsetsField = attachInfo.javaClass.getDeclaredField("mStableInsets")
            stableInsetsField.isAccessible = true
            val insets = stableInsetsField[attachInfo] as Rect
            return insets.bottom
        }
    } catch (exp: Exception) { }
    return 0
}