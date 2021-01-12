package cn.shawn.impractise.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout

class PanelLayout : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i("PanelLayout", "onAttachedToWindow: ")
        registerKeyboardActionInternal()
    }

    private fun registerKeyboardActionInternal() {
        getKeyboardAwareLayout()?.registerKeyboardActionListener(onKeyboardAction)
    }

    private val onKeyboardAction: (Boolean) -> Unit = { expand ->
        if (expand && isExpand()) {
            collapseInternal()
        }
    }

    fun isExpand() = visibility == View.VISIBLE

    fun toggle() {
        if (isExpand()) {
            collapse()
        } else {
            expand()
        }
    }

    private fun expand() {
        val keyboardAwareLayout = getKeyboardAwareLayout()
        keyboardAwareLayout?.run {
            if (this.isKeyboardExpand) {
                this.closeKeyboard()
                //在KeyboardAwareLayout#onMeasure中接收KeyboardAction的回调事件，
                // 并在KeyboardAwareLayout#onMeasure前改变布局大小，这样处理掉会闪动的那一帧
                registerKeyboardActionListener(object : (Boolean) -> Unit {
                    override fun invoke(expand: Boolean) {
                        if (!expand) {
                            unregisterKeyboardActionListener(this)
                            expandInternal()
                        }
                    }
                })
            } else {
                animExpandInternal()
            }
        }
    }

    private fun collapse() {
        animCollapseInternal()
    }

    private fun expandInternal() {
        visibility = View.VISIBLE
        layoutParams.height = getKeyboardHeight()
    }

    private fun collapseInternal() {
        visibility = View.GONE
        layoutParams.height = 0
    }

    private var animatorExpand: ValueAnimator? = null

    private var animatorCollapse: ValueAnimator? = null

    private fun animExpandInternal() {
        animatorExpand?.cancel()
        val animator = ValueAnimator.ofInt(0, getKeyboardHeight())
        animator.duration = 200L
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                visibility = View.VISIBLE
            }
        })
        animator.addUpdateListener {
            layoutParams.height = it.animatedValue as Int
            requestLayout()
        }
        animator.start()
        animatorExpand = animator
    }

    private fun animCollapseInternal() {
        animatorCollapse?.cancel()
        val animator = ValueAnimator.ofInt(height, 0)
        animator.duration = 200L
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                visibility = View.GONE
            }
        })
        animator.addUpdateListener {
            layoutParams.height = it.animatedValue as Int
            requestLayout()
        }
        animator.start()
        animatorCollapse = animator
    }

    private fun getKeyboardAwareLayout() =
        if (parent is KeyboardAwareLayout) {
            parent as KeyboardAwareLayout
        } else {
            null
        }

    private fun getKeyboardHeight() =
        if (parent is KeyboardAwareLayout) {
            (parent as KeyboardAwareLayout).keyboardHeight
        } else {
            KeyboardUtil.getKeyboardHeight(context)
        }
}