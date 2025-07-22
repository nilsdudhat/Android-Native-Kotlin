package com.belive.dating.activities.paywalls.subscriptions.success.gold

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
import com.belive.dating.databinding.ActivityGoldPaymentSuccessBinding
import com.belive.dating.extensions.getColorFromAttr
import com.belive.dating.extensions.setSystemBarColors
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class GoldPaymentSuccessActivity : MixPanelActivity() {

    val binding: ActivityGoldPaymentSuccessBinding by lazy {
        ActivityGoldPaymentSuccessBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()

        mixPanel?.timeEvent(GoldPaymentSuccessActivity::class.java.simpleName)
    }

    override fun onPause() {
        super.onPause()

        mixPanel?.track(GoldPaymentSuccessActivity::class.java.simpleName)
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
                "You're now a Belive <font color='#F8D96B'><b>Gold</b></font> Member! Enjoy exclusive features and a premium experience",
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            Html.fromHtml(
                "You're now a Belive <font color='#F8D96B'><b>Gold</b></font> Member! Enjoy exclusive features and a premium experience"
            )
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                "#302911".toColorInt(),
                "#211D0F".toColorInt(),
                "#1C180C".toColorInt(),
                "#121007".toColorInt(),
                "#0D0C06".toColorInt(),
                "#090806".toColorInt(),
                "#050505".toColorInt(),
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
                colors = listOf(0xF6E37A, 0xC9A33D, 0xA7852C),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.4),
            )
        )
    }
}