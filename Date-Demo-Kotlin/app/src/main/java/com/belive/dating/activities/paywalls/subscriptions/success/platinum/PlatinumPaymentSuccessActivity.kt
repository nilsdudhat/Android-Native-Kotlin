package com.belive.dating.activities.paywalls.subscriptions.success.platinum

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belive.dating.activities.MixPanelActivity
import com.belive.dating.databinding.ActivityPlatinumPaymentSuccessBinding
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.setSystemBarColors
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class PlatinumPaymentSuccessActivity : MixPanelActivity() {

    val binding: ActivityPlatinumPaymentSuccessBinding by lazy {
        ActivityPlatinumPaymentSuccessBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(PlatinumPaymentSuccessActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(PlatinumPaymentSuccessActivity::class.java.simpleName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSystemBarColors(getColorFromAttr(android.R.attr.windowBackground))

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000)

        setUI()
    }

    private fun setUI() {
        binding.txtDesc.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(
                "You're now a Belive <font color='#FFFFFF'><b>Platinum</b></font> Member! Enjoy exclusive features and a premium experience",
                Html.FROM_HTML_MODE_LEGACY,
            )
        } else {
            Html.fromHtml(
                "You're now a Belive <font color='#FFFFFF'><b>Platinum</b></font> Member! Enjoy exclusive features and a premium experience",
            )
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                "#333333".toColorInt(),
                "#242424".toColorInt(),
                "#1D1D1D".toColorInt(),
                "#101010".toColorInt(),
                "#0C0C0C".toColorInt(),
                "#0A0A0A".toColorInt(),
                "#060606".toColorInt(),
                "#030303".toColorInt(),
                "#000000".toColorInt(),
            )
        )
        gradientDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
        gradientDrawable.gradientRadius = 450f
        gradientDrawable.setGradientCenter(0.5f, 0.48f)
        binding.background.background = gradientDrawable

        binding.konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                fadeOutEnabled = true,
                spread = 360,
                colors = listOf(0xC7DAE9, 0xABBDC8, 0xD4DEE5),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.4),
            )
        )
    }
}