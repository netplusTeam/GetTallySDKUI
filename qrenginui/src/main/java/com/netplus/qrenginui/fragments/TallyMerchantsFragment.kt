package com.netplus.qrenginui.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netplus.qrengine.backendRemote.model.merchants.AllMerchantResponse
import com.netplus.qrengine.backendRemote.model.merchants.Merchant
import com.netplus.qrengine.utils.MERCHANTS_BASE_URL
import com.netplus.qrengine.utils.TOKEN
import com.netplus.qrengine.utils.TallyQrcodeGenerator
import com.netplus.qrengine.utils.TallyResponseCallback
import com.netplus.qrengine.utils.gone
import com.netplus.qrengine.utils.visible
import com.netplus.qrenginui.R
import com.netplus.qrenginui.adapters.AllMerchantAdapter
import com.netplus.qrenginui.utils.ProgressDialogUtil
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class TallyMerchantsFragment : Fragment(), AllMerchantAdapter.Interaction {

    private lateinit var recyclerView: RecyclerView
    private lateinit var allMerchantAdapter: AllMerchantAdapter
    private val tallyQrcodeGenerator = TallyQrcodeGenerator()
    private lateinit var qrInfoLayout: LinearLayout
    private val progressDialogUtil by lazy { ProgressDialogUtil(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_tally_merchants, container, false)

        recyclerView = rootView.findViewById(R.id.merchant_recycler)
        qrInfoLayout = rootView.findViewById(R.id.merchant_info_layout)

        initView()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and configure the map
        Configuration.getInstance().load(
            requireContext().applicationContext,
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        observer()
    }

    private fun observer() {
        progressDialogUtil.showProgressDialog("Loading...")
        tallyQrcodeGenerator.getAllMerchants(
            url = MERCHANTS_BASE_URL,
            token = TOKEN,
            limit = 20,
            page = 1,
            object : TallyResponseCallback<AllMerchantResponse> {
                override fun success(data: AllMerchantResponse?) {
                    progressDialogUtil.dismissProgressDialog()
                    if (data?.data?.isEmpty() == true) {
                        switchViewVisibility(isListEmpty = true)
                    } else {
                        switchViewVisibility(isListEmpty = false)
                        allMerchantAdapter = AllMerchantAdapter(
                            this@TallyMerchantsFragment,
                            data?.data ?: emptyList()
                        )
                        recyclerView.adapter = allMerchantAdapter
                    }
                }

                override fun failed(message: String?) {
                    progressDialogUtil.dismissProgressDialog()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onItemSelected(merchant: Merchant, map: MapView) {
        showAddressOnMap(map)
    }

    private fun showAddressOnMap(map: MapView) {
        val geoPoint = GeoPoint(6.4550575, 3.3941795)

        // Set the map to zoom in to the location
        val mapController = map.controller
        mapController.setZoom(20.0)  // Adjust this value as needed for the desired zoom level
        mapController.setCenter(geoPoint)

        val marker = Marker(map)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)
    }

    private fun switchViewVisibility(isListEmpty: Boolean) {
        if (isListEmpty) {
            recyclerView.gone()
            qrInfoLayout.visible()
        } else {
            recyclerView.visible()
            qrInfoLayout.gone()
        }
    }

    override fun onPause() {
        super.onPause()
        progressDialogUtil.dismissProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialogUtil.dismissProgressDialog()
    }
}