package cn.shawn.impractise.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager

class KeyboardUtil {

    companion object {

        private const val KEYBOARD_DEFAULT_HEIGHT = 200

        private const val TABLE_KEYBOARD = "im_keyboard"

        private const val KEY_KEYBOARD_HEIGHT = "im_keyboard"

        fun saveKeyboardHeight(ctx:Context, height:Int) {
            ctx.getSharedPreferences(TABLE_KEYBOARD, Context.MODE_PRIVATE)
                .edit().putInt(KEY_KEYBOARD_HEIGHT, height).apply()
        }

        fun getKeyboardHeight(ctx:Context) =
            ctx.getSharedPreferences(TABLE_KEYBOARD, Context.MODE_PRIVATE)
                .getInt(KEY_KEYBOARD_HEIGHT, getDefaultKeyboardHeight(ctx, KEYBOARD_DEFAULT_HEIGHT.toFloat()))

        private fun getDefaultKeyboardHeight(ctx:Context, default:Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, default, ctx.resources.displayMetrics).toInt()

        fun closeKeyboardPanel(view:View) {
            val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun openKeyboardPanel(view: View) {
            if (view.requestFocus()) {
                val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(view, 0)
            }
        }
    }
}