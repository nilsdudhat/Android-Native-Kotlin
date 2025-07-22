package com.belive.dating.activities.paywalls.subscriptions.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.databinding.ItemPlanFeatureBinding
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.logger
import com.belive.dating.payment.PlanFeatureModel
import com.belive.dating.payment.ProductType
import com.revenuecat.purchases.Offering

class PlanFeatureAdapter : RecyclerView.Adapter<PlanFeatureAdapter.ViewHolder>() {

    var list = arrayListOf<PlanFeatureModel>()
        set(value) {
            field = value
            notifyItemRangeChanged(0, value.size)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlanFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = list[position]
        holder.binding.model = feature
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun update(offering: Offering, productType: ProductType) {
        logger("--meta_data--", "identifier: ${offering.identifier}")

        try {
            val offerUnit =
                offering.availablePackages[0].product.period?.unit.toString().lowercase().let {
                    it.replaceFirstChar { char -> char.uppercaseChar() }
                }

            if (offerUnit.equals("week", true)) {
                val metadata: Map<String, Any> = offering.metadata

                if (metadata.containsKey("diamonds")) {
                    val planTitle = StringBuilder().append("1 Month")

                    val diamonds = metadata["diamonds"] as String
                    val item = list.find { it.tag == "diamonds" }
                    item?.highlight = diamonds
                    item?.tickIcon = R.drawable.ic_lock
                    item?.tickColor = R.color.platinum_plan
                    item?.title = StringBuilder().append("Diamonds for ").append(planTitle).append(" Plan").toString()
                    item?.let { list.set(list.indexOf(item), it) }
                    notifyItemChanged(list.indexOf(item))
                }
            } else {
                val metadata: Map<String, Any> = offering.metadata

                if (metadata.containsKey("diamonds")) {
                    val planTitle = StringBuilder()
                        .append(offering.availablePackages[0].product.period?.value).append(" ")
                        .append(offerUnit)

                    val diamonds = metadata["diamonds"] as String
                    val diamondMultiplier = metadata["diamond_multiplier"] as String
                    val item = list.find { it.tag == "diamonds" }
                    item?.highlight = (diamonds.toInt() * diamondMultiplier.toInt()).toString()
                    item?.title =
                        StringBuilder().append("Diamonds for ").append(planTitle).append(" Plan")
                            .toString()
                    item?.tickIcon = R.drawable.ic_tick
                    if (productType == ProductType.GOLD) {
                        item?.tickColor = R.color.gold_plan
                    }
                    item?.let { list.set(list.indexOf(item), it) }
                    notifyItemChanged(list.indexOf(item))
                }
            }
            val metadata: Map<String, Any> = offering.metadata

            if (metadata.containsKey("super_likes")) {
                val superLikes = metadata["super_likes"] as String
                val item = list.find { it.tag == "super_likes" }
                item?.highlight = superLikes
                item?.let { list.set(list.indexOf(item), it) }
                notifyItemChanged(list.indexOf(item))
            }
            if (metadata.containsKey("boosts")) {
                val boosts = metadata["boosts"] as String
                val item = list.find { it.tag == "boosts" }
                item?.highlight = boosts
                item?.let { list.set(list.indexOf(item), it) }
                notifyItemChanged(list.indexOf(item))
            }
        } catch (e: Exception) {
            catchLog("PlanFeatureAdapter update: ${e.printStackTrace()}")
        }
    }

    class ViewHolder(val binding: ItemPlanFeatureBinding) : RecyclerView.ViewHolder(binding.root)
}