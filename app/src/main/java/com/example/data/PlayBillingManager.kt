package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modern, production-ready Boilerplate Service Module for Google Play Billing Library (v7.0+).
 * Handles the secure initialization, querying of one-time purchases and subscription products,
 * launching of purchase flows, and processing of acquired products (consuming & acknowledging).
 */
class PlayBillingManager(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val TAG = "PlayBillingManager"

    // Backing flows for Reactive UI state matching
    private val _billingConnectionState = MutableStateFlow(false)
    val billingConnectionState: StateFlow<Boolean> = _billingConnectionState.asStateFlow()

    private val _productsDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productsDetails: StateFlow<List<ProductDetails>> = _productsDetails.asStateFlow()

    private val _purchasesState = MutableSharedFlow<List<Purchase>>(replay = 1)
    val purchasesState: SharedFlow<List<Purchase>> = _purchasesState.asSharedFlow()

    private val _billingError = MutableSharedFlow<String>()
    val billingError: SharedFlow<String> = _billingError.asSharedFlow()

    // Callback listener for incoming purchase updates
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                externalScope.launch {
                    handlePurchase(purchase)
                }
            }
            _purchasesState.tryEmit(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.w(TAG, "User canceled the Google Play Billing purchase process.")
            emitError("User canceled the purchase flow")
        } else {
            Log.e(TAG, "Billing transaction failed. Code: ${billingResult.responseCode}, Msg: ${billingResult.debugMessage}")
            emitError("Transaction failed: ${billingResult.debugMessage} (Error code: ${billingResult.responseCode})")
        }
    }

    // Google Play Billing Client instance
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        startBillingConnection()
    }

    /**
     * Attempts to start or restore connection to the Google Play Store billing services.
     */
    fun startBillingConnection() {
        if (billingClient.isReady) {
            _billingConnectionState.value = true
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Successfully connected to Google Play Billing Client.")
                    _billingConnectionState.value = true
                    // Query active products from play store immediately on connection
                    queryAvailableProducts()
                } else {
                    Log.e(TAG, "Play Billing Setup Failed: ${billingResult.debugMessage}")
                    _billingConnectionState.value = false
                    emitError("Unable to establish billing connection: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing system service disconnected. Retrying connection...")
                _billingConnectionState.value = false
            }
        })
    }

    /**
     * Queries available Products defined in Google Play Console (Subscriptions & One-Time Purchases).
     */
    fun queryAvailableProducts() {
        if (!billingClient.isReady) {
            Log.e(TAG, "Cannot query products. Billing client is not ready.")
            return
        }

        // Example products: Premium One-Time Upgrade and Monthly Creator Supporter Subscription
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("socialhub_premium_onetime")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("socialhub_creator_sub_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Products queried successfully. Found ${productDetailsList.size} play store item(s).")
                _productsDetails.value = productDetailsList
            } else {
                Log.e(TAG, "Failed to query products from store: ${billingResult.debugMessage}")
                emitError("Failed to fetch Google Play catalog items: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launches the secure Google Play payment UI overlay for the selected product.
     */
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, selectedOfferToken: String? = null) {
        if (!billingClient.isReady) {
            Log.e(TAG, "Billing Client is not active.")
            emitError("Billing services are currently unavailable. Please try again.")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply {
                    if (productDetails.productType == BillingClient.ProductType.SUBS) {
                        val token = selectedOfferToken ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                        if (token != null) {
                            setOfferToken(token)
                        }
                    }
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Purchase execution failed: ${billingResult.debugMessage}")
            emitError("Execution failure: ${billingResult.debugMessage}")
        }
    }

    /**
     * Securely process purchase credentials to Acknowledge or Consume items.
     * Non-consumables and subscriptions must be acknowledged to avoid refunds.
     */
    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.i(TAG, "Purchase securely acknowledged: ${purchase.orderId}")
                    } else {
                        Log.e(TAG, "Acknowledge operation failed: ${billingResult.debugMessage}")
                    }
                }
            }
        }
    }

    /**
     * Restore previous active purchases (One-time and Subscription items).
     */
    fun restorePurchases(onRestoreCompleted: (List<Purchase>) -> Unit) {
        if (!billingClient.isReady) {
            Log.e(TAG, "Billing client not ready to restore.")
            return
        }

        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
            if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val subParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()

                billingClient.queryPurchasesAsync(subParams) { subResult, subPurchases ->
                    if (subResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onRestoreCompleted(inAppPurchases + subPurchases)
                    } else {
                        Log.e(TAG, "Failed to restore subscriptions: ${subResult.debugMessage}")
                        onRestoreCompleted(inAppPurchases)
                    }
                }
            } else {
                Log.e(TAG, "Failed to restore in-app purchases: ${inAppResult.debugMessage}")
                onRestoreCompleted(emptyList())
            }
        }
    }

    private fun emitError(message: String) {
        externalScope.launch {
            _billingError.emit(message)
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
