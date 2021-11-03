package myapp.ui.splash

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import myapp.ui.splash.databinding.SplashViewBinding

class  SplashView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: SplashViewBinding by lazy {
        SplashViewBinding.inflate(LayoutInflater.from(context), this, false)
    }

    init {
        addView(binding.root)
    }

    fun startSplashAnimation() {
    }
}
