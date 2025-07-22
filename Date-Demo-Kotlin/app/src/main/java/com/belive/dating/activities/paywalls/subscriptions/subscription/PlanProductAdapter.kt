package com.belive.dating.activities.paywalls.subscriptions.subscription

import android.icu.text.NumberFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemPlanProductBinding
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getScreenWidth
import com.belive.dating.extensions.logger
import com.belive.dating.payment.ProductType
import com.belive.dating.payment.activePlan
import com.revenuecat.purchases.Offering
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

class PlanProductAdapter(var callBack: OnPlanListener, private val productType: ProductType) :
    RecyclerView.Adapter<PlanProductAdapter.ViewHolder>() {

    private var selectedPosition: Int
    private var basePlanPrice: Double

    init {
        selectedPosition = 0
        basePlanPrice = 0.00
    }

    var list: ArrayList<Offering> = arrayListOf()
        set(value) {
            field = value

            val basePlan = value[0]
            basePlanPrice =
                convertFormattedPriceToDouble(basePlan.availablePackages[0].product.price.formatted)

            logger("--products--", "basePlanPrice: $basePlanPrice")

            notifyItemRangeChanged(0, value.size)
        }

    interface OnPlanListener {
        fun onClickCallBack(offer: Offering)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = ItemPlanProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val layoutParams = holder.binding.main.layoutParams
        layoutParams.width = (getScreenWidth() / 2.5).toInt()
        holder.itemView.layoutParams = layoutParams

        val model = list[position]

        val unit = model.availablePackages[0].product.period?.unit.toString().lowercase().let {
            it.replaceFirstChar { char -> char.uppercaseChar() }
        }

        val title = StringBuilder()
            .append(model.availablePackages[0].product.period?.value).append(" ")
            .append(unit)

        val timeDuration = model.availablePackages[0].product.period?.iso8601
        val timeDelta: Number = when (timeDuration) {
            "P1W" -> 7 / 7
            "P1M" -> 30.3 / 7
            "P6M" -> 182 / 7
            "P1Y" -> 364 / 7
            else -> 0
        }

        holder.binding.apply {
            val priceArray = model.availablePackages[0].product.price.formatted
            val convertedPrice = convertFormattedPriceToDouble(priceArray)
            val subtitle =
                String.format(Locale.getDefault(), "%.2f", convertedPrice / timeDelta.toDouble())
                    .replace(".00", "")

            try {
                val discount =
                    (((basePlanPrice - subtitle.toDouble()) / basePlanPrice) * 100.00).roundToInt()
                logger("--pricing--", "discount: $discount")
                this.discount = discount
            } catch (e: Exception) {
                catchLog("onBindViewHolder discount: ${e.printStackTrace()}")
            }

            logger("--pricing--", "timeDelta: $timeDelta")
            logger("--pricing--", "convertedPrice: $convertedPrice")
            logger("--pricing--", "subtitle: $subtitle")
            logger("--pricing--", "basePlanPrice: $basePlanPrice")

            val symbol = priceArray[0]
            this.price = StringBuilder().append(symbol).append(subtitle)
            this.title = title.toString()
            this.isSelected = selectedPosition == holder.bindingAdapterPosition
            this.premiumType = this@PlanProductAdapter.productType
            this.isActive = if (activePlan == null) {
                false
            } else {
                try {
                    model.availablePackages[0].product.id.contains(activePlan!!.productPlanIdentifier!!)
                } catch (e: Exception) {
                    false
                }
            }
            executePendingBindings()

            main.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.bindingAdapterPosition

                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                callBack.onClickCallBack(model)
            }
        }
    }

    private fun convertFormattedPriceToDouble(formattedPrice: String): Double {
        val numericString = formattedPrice.replace(Regex("[^\\d.]"), "")
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NumberFormat.getInstance(Locale.getDefault()).parse(numericString).toDouble()
            } else {
                val decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
                val groupingSeparator = decimalFormatSymbols.groupingSeparator
                val decimalSeparator = decimalFormatSymbols.decimalSeparator

                val formattedString = numericString
                    .replace(groupingSeparator.toString(), "") // Remove thousands separator
                    .replace(decimalSeparator.toString(), ".") // Ensure decimal uses '.'

                formattedString.toDouble() // Convert to double
            }
        } catch (e: Exception) {
            0.0 // Handle invalid inputs gracefully
        }
    }

    fun setSelectedPosition(selectedPosition: Int) {
        this.selectedPosition = selectedPosition
        notifyItemRangeChanged(0, list.size)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemPlanProductBinding) : RecyclerView.ViewHolder(binding.root)
}