package com.netplus.qrenginui.adapters

import android.annotation.SuppressLint
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import com.netplus.qrengine.utils.decodeBase64ToBitmap
import com.netplus.qrengine.utils.decryptBase64
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.saveImageToGallery
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R

class TokenizedCardsAdapter(
    private val interaction: Interaction? = null,
    private val encryptedQrModel: List<EncryptedQrModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TokenizedCardsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.all_tokenized_card_items, parent, false), interaction
        )
    }

    override fun getItemCount() = encryptedQrModel.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenizedCardsViewHolder -> {
                holder.bind(encryptedQrModel[position], position)
            }
        }
    }

    class TokenizedCardsViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        private var smallImage = itemView.findViewById<ImageView>(R.id.small_card_image_view)
        private var tokenizedCardImage = itemView.findViewById<ImageView>(R.id.tokenized_card_image)
        private var dropDownIcon = itemView.findViewById<ImageView>(R.id.drop_down_icon)
        private var saveQrIcon = itemView.findViewById<ImageView>(R.id.save_qr_icon)
        private var pushUpIcon = itemView.findViewById<ImageView>(R.id.push_up_icon)
        private var bankName = itemView.findViewById<TextView>(R.id.bank_name)
        private var bankAndSchemeName = itemView.findViewById<TextView>(R.id.card_and_bank_scheme)
        private var dateCreated = itemView.findViewById<TextView>(R.id.date_created)
        private var viewTransactions =
            itemView.findViewById<AppCompatButton>(R.id.view_transaction_btn)
        private var topConstraints = itemView.findViewById<ConstraintLayout>(R.id.top_constraint)
        private var bottomConstraint =
            itemView.findViewById<ConstraintLayout>(R.id.bottm_constraints)

        @SuppressLint("SetTextI18n")
        fun bind(encryptedQrModel: EncryptedQrModel, position: Int) {

            val qrBitmap = decodeBase64ToBitmap(
                decryptBase64(
                    encryptedQrModel.image.toString(),
                    encryptedQrModel.qrcodeId.toString()
                ).substringAfter("data:image/png;base64,")
            )
            smallImage.setImageBitmap(qrBitmap)
            tokenizedCardImage.setImageBitmap(qrBitmap)
            bankName.text = encryptedQrModel.issuingBank
            bankAndSchemeName.text =
                "${encryptedQrModel.issuingBank} ${encryptedQrModel.cardScheme}"
            dateCreated.text = encryptedQrModel.date

            dropDownIcon.setOnClickListener {
                TransitionManager.beginDelayedTransition(bottomConstraint)
                if (bottomConstraint.visibility == View.GONE) {
                    bottomConstraint.visible()
                    topConstraints.gone()
                }
            }

            pushUpIcon.setOnClickListener {
                TransitionManager.beginDelayedTransition(topConstraints)
                if (topConstraints.visibility == View.GONE) {
                    topConstraints.visible()
                    bottomConstraint.gone()
                }
            }

            saveQrIcon.setOnClickListener {
                saveImageToGallery(itemView.context, tokenizedCardImage)
                Toast.makeText(itemView.context, "Saved successfully", Toast.LENGTH_SHORT).show()
            }

            viewTransactions.setOnClickListener {
                interaction?.onItemSelected(position, encryptedQrModel)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(
            absoluteAdapterPosition: Int,
            encryptedQrModel: EncryptedQrModel
        )
    }
}