package com.belive.dating.activities.paywalls.topups.diamond

import android.icu.text.NumberFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemDiamondPaywallBinding
import com.belive.dating.extensions.logger
import com.revenuecat.purchases.Offering
import java.text.DecimalFormatSymbols
import java.util.Locale

class DiamondPaywallAdapter(
    val viewModel: DiamondPaywallViewModel,
    private val onOfferClickListener: OnOfferClickListener,
) :
    RecyclerView.Adapter<DiamondPaywallAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0

    interface OnOfferClickListener {
        fun onOfferClick(position: Int)
    }

    var paywallList = arrayListOf<Offering>()
        set(value) {
            logger("--diamonds--", "update")
            field = value
            notifyItemRangeChanged(0, value.size)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiamondPaywallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        logger("--diamonds--", "position: $position")

        val offering = paywallList[position]

        val offerPrice = offering.availablePackages[0].product.price.formatted

        holder.binding.price = offerPrice
        val tagValue = offering.metadata["tag"]
        holder.binding.tag = if (tagValue is String) {
            tagValue.ifEmpty {
                ""
            }
        } else {
            ""
        }
        holder.binding.tag = offering.metadata["tag"] as String
        holder.binding.title = offering.metadata["title"] as String
        val boostCount = (offering.metadata["diamond_count"] as String).toInt()
        val priceInDouble = convertFormattedPriceToDouble(offerPrice)
        val pricePerLike = String.format(Locale.getDefault(), "%.2f", priceInDouble / boostCount).replace(".00", "")
        holder.binding.pricePerBoost = StringBuilder().append(offerPrice[0]).append(pricePerLike).append(" / Diamond").toString()

        val isSelected = position == selectedPosition
        holder.binding.isSelected = isSelected

        holder.binding.layoutMain.setOnClickListener {
            if (selectedPosition == holder.bindingAdapterPosition) {
                return@setOnClickListener
            }

            val previousPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onOfferClickListener.onOfferClick(holder.bindingAdapterPosition)
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

    override fun getItemCount(): Int {
        logger("--diamonds--", "getItemCount: ${paywallList.size}")
        return paywallList.size
    }

    fun setSelectedPosition(selectedPosition: Int) {
        this.selectedPosition = selectedPosition
        notifyItemRangeChanged(0, paywallList.size)
    }

    class ViewHolder(val binding: ItemDiamondPaywallBinding) : RecyclerView.ViewHolder(binding.root)
}