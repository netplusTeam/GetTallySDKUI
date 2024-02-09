package com.netplus.qrenginui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.merchants.Merchant
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import org.osmdroid.views.MapView

class AllMerchantAdapter(
    private val interaction: Interaction? = null,
    private val merchants: List<Merchant>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AllMerchantViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_qr_transactions_item, parent, false), interaction
        )
    }

    override fun getItemCount() = merchants.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AllMerchantViewHolder -> {
                holder.bind(merchants[position])
            }
        }
    }

    class AllMerchantViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        private val merchantName = itemView.findViewById<TextView>(R.id.merchant_name)
        private val merchantEmail = itemView.findViewById<TextView>(R.id.transaction_amount)
        private val merchantAddress = itemView.findViewById<TextView>(R.id.transaction_date)
        private val container = itemView.findViewById<ConstraintLayout>(R.id.container)
        private val map = itemView.findViewById<MapView>(R.id.map)
        private val directionIcon = itemView.findViewById<ImageView>(R.id.direction_icon)

        fun bind(merchant: Merchant) {
            directionIcon.visible()
            merchantName.text = merchant.contact_name
            merchantEmail.text = merchant.ptsp
            merchantAddress.text = merchant.slip_footer
            directionIcon.setOnClickListener {
                if (map.visibility == View.VISIBLE) {
                    map.gone()
                } else map.visible()
            }
            container.setOnClickListener { interaction?.onItemSelected(merchant, map) }
        }
    }

    interface Interaction {
        fun onItemSelected(merchant: Merchant, map: MapView)
    }
}