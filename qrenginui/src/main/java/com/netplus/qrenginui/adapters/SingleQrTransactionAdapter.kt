package com.netplus.qrenginui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.Row
import com.netplus.qrengine.utils.convertDateToReadableFormat
import com.netplus.qrengine.utils.toDecimalFormat
import com.netplus.qrenginui.R

class SingleQrTransactionAdapter(
    private val interaction: Interaction? = null,
    private val transaction: List<Row>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SingleQrTransactionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_qr_transactions_item, parent, false), interaction
        )
    }

    override fun getItemCount() = transaction.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SingleQrTransactionViewHolder ->
                holder.bind(transaction[position])
        }
    }

    class SingleQrTransactionViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        private val merchantName = itemView.findViewById<TextView>(R.id.merchant_name)
        private val transactionAmount = itemView.findViewById<TextView>(R.id.transaction_amount)
        private val transactionDate = itemView.findViewById<TextView>(R.id.transaction_date)

        fun bind(transaction: Row) {
            merchantName.text = transaction.merchantName
            transactionAmount.text = transaction.amount.toString().toDecimalFormat(true)
            transactionDate.text = convertDateToReadableFormat(transaction.dateCreated)
        }
    }

    interface Interaction {
        fun onItemSelected()
    }
}
