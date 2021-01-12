package cn.shawn.impractise.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import cn.shawn.impractise.R
import kotlin.math.abs

class KeyboardAwareLayout : LinearLayout {

    companion object {

        private const val TAG = "KeyboardAwareLayout"

        private const val THRESHOLD_KEYBOARD_HEIGHT = 100

        private const val MAX_KEYBOARD_HEIGHT = 500

    }

    private var inputWidgetId = View.NO_ID

    //DecorView需要绘制的区域高度
    private var prevVisibleHeight = 0

    var isKeyboardExpand = false

    var keyboardHeight = KeyboardUtil.getKeyboardHeight(context)

    private val keyboardActionListeners = mutableSetOf<(Boolean) -> Unit>()

    private val keyboardHeightChangeListeners = mutableSetOf<(Int) -> Unit>()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        orientation = VERTICAL
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.KeyboardAwareLayout)
        inputWidgetId = typeArray.getResourceId(R.styleable.KeyboardAwareLayout_input_widget_id, View.NO_ID)
        typeArray.recycle()
    }

    /**
     * 1，在onMeasure时判断软键盘是否需要弹起、收起状态，并将软键盘待执行的状态通知到需要感知软键盘状态的子View；
     * 2，在decorView.viewTreeObserver#addOnGlobalLayoutListener 中更新软键盘高度，并通知到子View;
     * onGlobalLayout 会在layout完成后回调
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        updateKeyboardStatus()
        updateKeyboardHeight()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun updateKeyboardStatus() {
        //layout没完成前不会有高度
        val decorHeight = rootView.height

        //获取可见区域，根据可见区域的高度变化感知软键盘的动作
        val currVisibleArea = Rect()
        rootView.getWindowVisibleDisplayFrame(currVisibleArea)
        val currVisibleHeight = currVisibleArea.bottom - currVisibleArea.top

        val diffVisibleHeight = prevVisibleHeight - currVisibleHeight
        if (prevVisibleHeight > 0 && abs(diffVisibleHeight) > dp2px(THRESHOLD_KEYBOARD_HEIGHT)) {
            if (diffVisibleHeight > 0) {
                notifyKeyboardAction(true)
            } else {
                notifyKeyboardAction(false)
            }
        }
        prevVisibleHeight = currVisibleHeight
        Log.i(TAG, "onMeasure: decorHeight:$decorHeight visibleArea:$currVisibleArea")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.i(TAG, "onLayout: ")
        super.onLayout(changed, l, t, r, b)
    }

    private fun updateKeyboardHeight() {
        //onGlobalLayout 会在onLayout完成后回调
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (!isKeyboardExpand) return
                val decorHeight = rootView.height

                val currVisibleArea = Rect()
                rootView.getWindowVisibleDisplayFrame(currVisibleArea)
                val currVisibleHeight = currVisibleArea.bottom - currVisibleArea.top

                val statusBarHeight = getStatusBarHeight()
                val bottomBarHeight = getBottomBarHeight()

                val keyboardHeight =
                    decorHeight - statusBarHeight - currVisibleHeight - bottomBarHeight

                Log.i(TAG, "onGlobalLayout: $keyboardHeight")
                notifyKeyboardHeight(keyboardHeight)
            }
        })
    }

    fun closeKeyboard() {
        getInputView()?.run {
            KeyboardUtil.closeKeyboardPanel(this)
        }
    }

    fun openKeyboard() {
        getInputView()?.run {
            KeyboardUtil.openKeyboardPanel(this)
        }
    }

    private fun getInputView() = findViewById<View>(inputWidgetId)

    fun registerKeyboardActionListener(listener: (Boolean) -> Unit) {
        keyboardActionListeners.add(listener)
    }

    fun unregisterKeyboardActionListener(listener: (Boolean) -> Unit) {
        keyboardActionListeners.remove(listener)
    }

    fun registerKeyboardHeightChangeListener(listener: (Int) -> Unit) {
        keyboardHeightChangeListeners.add(listener)
    }

    fun unregisterKeyboardHeightChangeListener(listener: (Int) -> Unit) {
        keyboardHeightChangeListeners.remove(listener)
    }

    private fun notifyKeyboardAction(isExpand: Boolean) {
        if (isKeyboardExpand == isExpand) {
            return
        }
        isKeyboardExpand = isExpand
        keyboardActionListeners.forEach {
            it.invoke(isExpand)
        }
        Log.i(TAG, "updateKeyboardAction: $isKeyboardExpand")
    }

    private fun notifyKeyboardHeight(height: Int) {
        if (height == keyboardHeight || height > dp2px(MAX_KEYBOARD_HEIGHT)) return
        keyboardHeight = height
        keyboardHeightChangeListeners.forEach {
            it.invoke(height)
        }
        KeyboardUtil.saveKeyboardHeight(context, height)
        Log.i(TAG, "updateKeyboardHeight: $keyboardHeight")
    }

    private fun dp2px(value: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    )


}