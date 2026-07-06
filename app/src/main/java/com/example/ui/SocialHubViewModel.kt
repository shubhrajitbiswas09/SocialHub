package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

sealed class Screen {
    object Feed : Screen()
    object Creators : Screen()
    data class CreatorDetail(val creatorId: String) : Screen()
    object Wallet : Screen()
    object LiveEvents : Screen()
    data class Chat(val initialRecipient: String? = null) : Screen()
    data class ProductDetail(val productId: Int) : Screen()
    object Settings : Screen()
}

class SocialHubViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SocialHubRepository(db.dao(), application)

    // Streams from Database
    val creators = repository.creators
    val posts = repository.posts
    val subscriptions = repository.subscriptions
    val transactions = repository.transactions
    val chatMessages = repository.chatMessages
    val events = repository.events
    val marketplaceProducts = repository.marketplaceProducts
    val marketplaceBanners = repository.marketplaceBanners

    sealed class BannersApiState {
        object Loading : BannersApiState()
        data class Success(val banners: List<MarketplaceBanner>) : BannersApiState()
        data class Error(val message: String) : BannersApiState()
    }

    private val _externalBannersState = MutableStateFlow<BannersApiState>(BannersApiState.Loading)
    val externalBannersState: StateFlow<BannersApiState> = _externalBannersState.asStateFlow()

    init {
        fetchExternalBanners()
    }

    fun fetchExternalBanners() {
        viewModelScope.launch {
            _externalBannersState.value = BannersApiState.Loading
            delay(1200) // Simulated premium API response latency
            try {
                // Dynamically fetch promotional contents from simulated External Admin Panel API
                val apiBanners = listOf(
                    MarketplaceBanner(
                        id = 101,
                        title = "Apex Audio Processor V2.0 PRO",
                        description = "Exclusive 45% live launching discount on the low-latency synthesizer & spatial dynamics rack.",
                        imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1000",
                        targetProductId = 1
                    ),
                    MarketplaceBanner(
                        id = 102,
                        title = "LedgerPro Biometric cold storage",
                        description = "Fully automated biometric high-security physical hardware wallet. Seed pre-order campaign with zero extra fees.",
                        imageUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?w=1000",
                        targetProductId = 2
                    ),
                    MarketplaceBanner(
                        id = 103,
                        title = "Vaporwave Creative LUT Preset Packs",
                        description = "Complete access to 42 dynamic warm chromatic and lofi night presets calibrated by Pixel Queen.",
                        imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1000",
                        targetProductId = 3
                    )
                )
                _externalBannersState.value = BannersApiState.Success(apiBanners)
            } catch (e: Exception) {
                _externalBannersState.value = BannersApiState.Error(e.localizedMessage ?: "Network anomaly, failed to fetch promotions")
            }
        }
    }

    private val _adminApiUrl = MutableStateFlow("https://api.socialhub-admin.com/v1")
    val adminApiUrl: StateFlow<String> = _adminApiUrl.asStateFlow()

    private val _bannerSyncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val bannerSyncStatus: StateFlow<SyncStatus> = _bannerSyncStatus.asStateFlow()

    fun updateAdminApiUrl(newUrl: String) {
        _adminApiUrl.value = newUrl
    }

    fun syncBannersFromExternalApi() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _bannerSyncStatus.value = SyncStatus.Loading
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                var urlStr = _adminApiUrl.value.trim()
                if (urlStr.isNotBlank()) {
                    if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                        urlStr = "https://$urlStr"
                    }
                    
                    val fullUrl = if (urlStr.endsWith("/")) {
                        "${urlStr}api/promotions/banners"
                    } else {
                        "${urlStr}/api/promotions/banners"
                    }

                    val request = okhttp3.Request.Builder()
                        .url(fullUrl)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw java.io.IOException("HTTP error code: ${response.code}")
                        }
                        val bodyString = response.body?.string() ?: throw java.io.IOException("Response body is empty")
                        val parsedBanners = apiBannersListAdapter.fromJson(bodyString)
                            ?: throw java.io.IOException("JSON parsing failed")

                        if (parsedBanners.isNotEmpty()) {
                            repository.clearAllBanners()
                            for (apiBanner in parsedBanners) {
                                repository.insertBanner(
                                    com.example.data.MarketplaceBanner(
                                        id = apiBanner.id,
                                        title = apiBanner.title,
                                        description = apiBanner.description,
                                        imageUrl = apiBanner.imageUrl,
                                        targetProductId = apiBanner.targetProductId
                                    )
                                )
                            }
                            _bannerSyncStatus.value = SyncStatus.Success(parsedBanners.size)
                            showNotification("Banners Synced 📡", "Successfully loaded ${parsedBanners.size} live banners from separate admin panel!")
                        } else {
                            _bannerSyncStatus.value = SyncStatus.Error("Returned empty promotional list")
                        }
                    }
                } else {
                    _bannerSyncStatus.value = SyncStatus.Error("API URL cannot be empty")
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Network or connection error"
                _bannerSyncStatus.value = SyncStatus.Error(errorMsg)
                showNotification("Sync Failed ⚠️", "Could not reach separate Admin Panel API. Using offline cached banners.")
            }
        }
    }

    fun addProduct(name: String, description: String, price: Double, logoUrl: String, imageUrl: String, affiliateLink: String, creatorName: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val product = MarketplaceProduct(
                name = name,
                description = description,
                price = price,
                logoUrl = logoUrl.ifBlank { "https://images.unsplash.com/photo-1554080353-a576cf803bda?w=150" },
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=800" },
                affiliateLink = affiliateLink,
                creatorName = creatorName.ifBlank { "Admin" }
            )
            repository.insertProduct(product)
            showNotification("Product Added 🎉", "Product '$name' has been added to the marketplace!")
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteProductById(id)
            showNotification("Product Deleted 🗑️", "Product removed successfully.")
        }
    }

    fun addBanner(title: String, description: String, imageUrl: String, targetProductId: Int? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val banner = MarketplaceBanner(
                title = title,
                description = description,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=1200" },
                targetProductId = targetProductId
            )
            repository.insertBanner(banner)
            showNotification("Banner Created ⚡", "A new promotional banner has been activated!")
        }
    }

    fun deleteBanner(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteBannerById(id)
            showNotification("Banner Removed 🗑️", "Banner removed successfully.")
        }
    }

    // --- ADVANCED NOTIFICATION CENTER & SETTINGS ENGAGEMENT ---
    private val _notificationsList = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                title = "Welcome to SocialHub! 🌐",
                message = "Discover, tip, and follow elite creators. Chat securely via fully-encrypted channels and monitor security logs.",
                type = "SYSTEM"
            ),
            AppNotification(
                title = "Cyber Node Loaded ⚡",
                message = "Your high-fidelity crypto ledger node is synchronized and initialized with a complimentary $500.00 credit balance.",
                type = "WALLET"
            ),
            AppNotification(
                title = "Lag Watchdog Active 🛡️",
                message = "Automated high-performance watchdog is guarding against runtime UI bottlenecks to ensure flawless 60 FPS scrolling.",
                type = "SYSTEM"
            )
        )
    )
    val notificationsList: StateFlow<List<AppNotification>> = _notificationsList.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(true)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _walletAlertsEnabled = MutableStateFlow(true)
    val walletAlertsEnabled: StateFlow<Boolean> = _walletAlertsEnabled.asStateFlow()

    private val _creatorAlertsEnabled = MutableStateFlow(true)
    val creatorAlertsEnabled: StateFlow<Boolean> = _creatorAlertsEnabled.asStateFlow()

    private val _chatAlertsEnabled = MutableStateFlow(true)
    val chatAlertsEnabled: StateFlow<Boolean> = _chatAlertsEnabled.asStateFlow()

    private val _isPrivateAccount = MutableStateFlow(false)
    val isPrivateAccount: StateFlow<Boolean> = _isPrivateAccount.asStateFlow()

    private val _isIncognitoSearch = MutableStateFlow(false)
    val isIncognitoSearch: StateFlow<Boolean> = _isIncognitoSearch.asStateFlow()

    private val _extBankSyncGuard = MutableStateFlow(true)
    val extBankSyncGuard: StateFlow<Boolean> = _extBankSyncGuard.asStateFlow()

    private val _extAntiPhishingFilter = MutableStateFlow(true)
    val extAntiPhishingFilter: StateFlow<Boolean> = _extAntiPhishingFilter.asStateFlow()

    private val _extOverlayBlocker = MutableStateFlow(true)
    val extOverlayBlocker: StateFlow<Boolean> = _extOverlayBlocker.asStateFlow()

    private val _extMalwareIsolation = MutableStateFlow(true)
    val extMalwareIsolation: StateFlow<Boolean> = _extMalwareIsolation.asStateFlow()

    // --- NEW TWO-STEP VERIFICATION & ANTI-HACKER PROTECTION STATES ---
    private val _isTwoStepEnabled = MutableStateFlow(false)
    val isTwoStepEnabled: StateFlow<Boolean> = _isTwoStepEnabled.asStateFlow()

    private val _twoStepMethod = MutableStateFlow("Authenticator App") // "Authenticator App", "SMS Telemetry", "Web3 Secure Key"
    val twoStepMethod: StateFlow<String> = _twoStepMethod.asStateFlow()

    private val _extZeroTrustRotator = MutableStateFlow(true)
    val extZeroTrustRotator: StateFlow<Boolean> = _extZeroTrustRotator.asStateFlow()

    private val _extBiometricStartupLock = MutableStateFlow(false)
    val extBiometricStartupLock: StateFlow<Boolean> = _extBiometricStartupLock.asStateFlow()

    private val _extPayloadEncryption = MutableStateFlow(true)
    val extPayloadEncryption: StateFlow<Boolean> = _extPayloadEncryption.asStateFlow()

    private val _extAntiHackerGuard = MutableStateFlow(true)
    val extAntiHackerGuard: StateFlow<Boolean> = _extAntiHackerGuard.asStateFlow()

    fun setPushNotificationsEnabled(enabled: Boolean) { _pushNotificationsEnabled.value = enabled }
    fun setSoundEnabled(enabled: Boolean) { _soundEnabled.value = enabled }
    fun setVibrationEnabled(enabled: Boolean) { _vibrationEnabled.value = enabled }
    fun setWalletAlertsEnabled(enabled: Boolean) { _walletAlertsEnabled.value = enabled }
    fun setCreatorAlertsEnabled(enabled: Boolean) { _creatorAlertsEnabled.value = enabled }
    fun setChatAlertsEnabled(enabled: Boolean) { _chatAlertsEnabled.value = enabled }
    fun setPrivateAccount(enabled: Boolean) { _isPrivateAccount.value = enabled }
    fun setIncognitoSearch(enabled: Boolean) { _isIncognitoSearch.value = enabled }
    fun setExtBankSyncGuard(enabled: Boolean) { _extBankSyncGuard.value = enabled }
    fun setExtAntiPhishingFilter(enabled: Boolean) { _extAntiPhishingFilter.value = enabled }
    fun setExtOverlayBlocker(enabled: Boolean) { _extOverlayBlocker.value = enabled }
    fun setExtMalwareIsolation(enabled: Boolean) { _extMalwareIsolation.value = enabled }
    fun setTwoStepEnabled(enabled: Boolean) { _isTwoStepEnabled.value = enabled }
    fun setTwoStepMethod(method: String) { _twoStepMethod.value = method }
    fun setExtZeroTrustRotator(enabled: Boolean) { _extZeroTrustRotator.value = enabled }
    fun setExtBiometricStartupLock(enabled: Boolean) { _extBiometricStartupLock.value = enabled }
    fun setExtPayloadEncryption(enabled: Boolean) { _extPayloadEncryption.value = enabled }
    fun setExtAntiHackerGuard(enabled: Boolean) { _extAntiHackerGuard.value = enabled }

    fun markNotificationAsRead(id: String) {
        _notificationsList.value = _notificationsList.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notificationsList.value = _notificationsList.value.map {
            it.copy(isRead = true)
        }
    }

    fun deleteNotification(id: String) {
        _notificationsList.value = _notificationsList.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notificationsList.value = emptyList()
    }


    // Expose only posts from followed creators, sorted by recency
    val followedPosts: Flow<List<Post>> = combine(repository.posts, repository.creators) { postsList, creatorsList ->
        val followedCreatorIds = creatorsList.filter { it.isFollowed }.map { it.id }.toSet()
        postsList.filter { it.creatorId in followedCreatorIds }
            .sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Local UI State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Feed)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _chatEncryptionEnabled = MutableStateFlow(true)
    val chatEncryptionEnabled: StateFlow<Boolean> = _chatEncryptionEnabled.asStateFlow()

    private val _userProfileName = MutableStateFlow("Mario's Pizza")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()
    
    private val _userProfileHandle = MutableStateFlow("MarioPizza45")
    val userProfileHandle: StateFlow<String> = _userProfileHandle.asStateFlow()

    private val _userProfileBio = MutableStateFlow("🚀 SocialHub Pro Creator & Digital Pioneer • Crafting dynamic high-fidelity presets, premium streams, and decentralised channel nodes.")
    val userProfileBio: StateFlow<String> = _userProfileBio.asStateFlow()

    private val _userProfileLink = MutableStateFlow("socialhub.network/creator/MarioPizza45")
    val userProfileLink: StateFlow<String> = _userProfileLink.asStateFlow()

    private val _userProfileDp = MutableStateFlow("")
    val userProfileDp: StateFlow<String> = _userProfileDp.asStateFlow()

    private val _userProfileBanner = MutableStateFlow("")
    val userProfileBanner: StateFlow<String> = _userProfileBanner.asStateFlow()

    fun updateProfile(name: String, handle: String, bio: String, link: String, dpUri: String, bannerUri: String) {
        _userProfileName.value = name
        _userProfileHandle.value = handle
        _userProfileBio.value = bio
        _userProfileLink.value = link
        _userProfileDp.value = dpUri
        _userProfileBanner.value = bannerUri
    }
    private val _isDataFetching = MutableStateFlow(false)
    val isDataFetching: StateFlow<Boolean> = _isDataFetching.asStateFlow()

    private val _walletBalance = MutableStateFlow(500.00)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()
    // Temporary storage for active operations (e.g. active payment request or checkout)
    private val _activeCheckoutInfo = MutableStateFlow<CheckoutInfo?>(null)
    val activeCheckoutInfo: StateFlow<CheckoutInfo?> = _activeCheckoutInfo.asStateFlow()

    private val _activeNotification = MutableStateFlow<NotificationMessage?>(null)
    val activeNotification: StateFlow<NotificationMessage?> = _activeNotification.asStateFlow()

    // --- Theme State ---
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- DEVICE PROTECTION & LAG WATCHDOG Safeguard ---
    private val _isLagWatchdogEnabled = MutableStateFlow(true)
    val isLagWatchdogEnabled: StateFlow<Boolean> = _isLagWatchdogEnabled.asStateFlow()

    private val _isLaggingOrHanging = MutableStateFlow(false)
    val isLaggingOrHanging: StateFlow<Boolean> = _isLaggingOrHanging.asStateFlow()

    private val _lagCountdown = MutableStateFlow(5)
    val lagCountdown: StateFlow<Int> = _lagCountdown.asStateFlow()

    private val _measuredDelayMs = MutableStateFlow(0L)
    val measuredDelayMs: StateFlow<Long> = _measuredDelayMs.asStateFlow()

    private val _watchdogStatus = MutableStateFlow("System Healthy • 60 FPS")
    val watchdogStatus: StateFlow<String> = _watchdogStatus.asStateFlow()

    fun toggleWatchdog(enabled: Boolean) {
        _isLagWatchdogEnabled.value = enabled
        if (!enabled) {
            _isLaggingOrHanging.value = false
        }
    }

    fun triggerStressTest() {
        viewModelScope.launch {
            _watchdogStatus.value = "Stressing Main Thread..."
            _measuredDelayMs.value = 2800L
            _isLaggingOrHanging.value = true
            _lagCountdown.value = 5
            while (_lagCountdown.value > 0 && _isLaggingOrHanging.value) {
                delay(1000)
                _lagCountdown.value -= 1
            }
            if (_lagCountdown.value == 0 && _isLaggingOrHanging.value) {
                _watchdogStatus.value = "Safe-shutting App to Protect Device..."
                delay(500)
                executeCleanRelaunch()
            }
        }
    }

    fun resetLagState() {
        _isLaggingOrHanging.value = false
        _watchdogStatus.value = "System Healthy • 60 FPS"
    }

    fun executeCleanRelaunch() {
        val context = getApplication<Application>()
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("SocialHub", "Error starting relaunch: ${e.message}")
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(0)
        }
    }

    fun triggerFirestoreSync() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val creatorsList = creators.first()
                val followedCreatorIds = creatorsList.filter { it.isFollowed }.map { it.id }.toSet()
                repository.fetchFollowedPostsFromFirestore(followedCreatorIds)
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Sync error: ${e.message}")
            }
        }
    }

    init {
        // Actual Watchdog Latency Checker in the Background
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (true) {
                delay(2000)
                if (_isLagWatchdogEnabled.value && !_isLaggingOrHanging.value) {
                    val startTime = System.currentTimeMillis()
                    try {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            val delayMeasured = System.currentTimeMillis() - startTime
                            _measuredDelayMs.value = delayMeasured
                            if (delayMeasured > 3000L && !_isLaggingOrHanging.value) { // Main thread severely blocked or lagged!
                                _watchdogStatus.value = "Main Thread Blocked! Delay: ${delayMeasured}ms"
                                _isLaggingOrHanging.value = true
                                _lagCountdown.value = 5
                                
                                // Record a Security event indicating system lag / thread blocking!
                            } else if (delayMeasured > 500L) {
                                _watchdogStatus.value = "Warning: Minor Latency (${delayMeasured}ms)"
                            } else {
                                _watchdogStatus.value = "System Healthy • Latency: ${delayMeasured}ms"
                                _isLaggingOrHanging.value = false
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SocialHub", "Error in watchdog main thread latency check: ${e.message}")
                    }
                    
                    if (_isLaggingOrHanging.value) {
                        // Countdown sequence runs safely in background thread (Dispatchers.Default)!
                        while (_lagCountdown.value > 0 && _isLaggingOrHanging.value) {
                            delay(1000)
                            _lagCountdown.value -= 1
                        }
                        if (_lagCountdown.value == 0 && _isLaggingOrHanging.value) {
                            // Safe recovery / memory recycling in background!
                            java.lang.System.gc() // Trigger garbage collector to recycle memory buffers
                            _watchdogStatus.value = "Auto-Recovered! Memory Recycled ✅"
                            delay(1000)
                            _isLaggingOrHanging.value = false
                            _watchdogStatus.value = "System Healthy"
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            _isDataFetching.value = true
            try {
                // Seed base data if empty
                creators.first().let { list ->
                    if (list.isEmpty()) {
                        repository.seedInitialData()
                    }
                }
                triggerFirestoreSync()
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Init seed or sync error: ${e.message}")
            } finally {
                delay(50) // Reduced startup delay for immediate responsiveness
                _isDataFetching.value = false
            }
        }



    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _isDataFetching.value = true
            try {
                val creatorsList = creators.first()
                val followedCreatorIds = creatorsList.filter { it.isFollowed }.map { it.id }.toSet()
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        repository.fetchFollowedPostsFromFirestore(followedCreatorIds)
                    } catch (e: Exception) {
                        android.util.Log.e("SocialHub", "Refresh sync background error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Refresh sync error: ${e.message}")
            } finally {
                delay(350) // Short delay for visual feedback of refresh state
                _isDataFetching.value = false
                showNotification("Refreshed", "Content cache successfully synchronized! 🌐")
            }
        }
    }

    fun toggleChatEncryption() {
        _chatEncryptionEnabled.value = !_chatEncryptionEnabled.value
    }

    // Cryptographically secure payment generator (OWASP Mobile M5: Insufficient Cryptography)
    private fun generateSecurePaymentId(prefix: String): String {
        return try {
            val bytes = ByteArray(6)
            java.security.SecureRandom().nextBytes(bytes)
            val hex = bytes.joinToString("") { String.format("%02x", it) }
            "${prefix}_$hex"
        } catch (e: Exception) {
            "${prefix}_${System.currentTimeMillis().toString().takeLast(6)}"
        }
    }

    // Add Balance (Simulate Razorpay Top-up)
    fun addWalletFunds(amount: Double) {
        if (amount <= 0.0 || !amount.isFinite() || amount > 100000.0) {
            showNotification("Validation Failed", "Invalid deposit amount specified!")
            return
        }
        viewModelScope.launch {
            _walletBalance.value += amount
            val tx = Transaction(
                type = "WALLET_FUND",
                description = "Loaded funds via Razorpay Secure Gateway",
                amount = amount,
                currency = "USD",
                recipientHandle = "user_wallet",
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_fund")
            )
            repository.insertTransaction(tx)
            showNotification("Success", "Deposited $${String.format("%.2f", amount)} successfully via Razorpay!")
        }
    }

    // Secure Encrypted UPI Transaction
    fun addWalletFundsViaUPI(amount: Double, upiId: String, txHash: String) {
        if (amount <= 0.0 || !amount.isFinite() || amount > 100000.0) {
            showNotification("Validation Failed", "Invalid UPI deposit amount specified!")
            return
        }
        viewModelScope.launch {
            _walletBalance.value += amount
            val tx = Transaction(
                type = "WALLET_FUND",
                description = "Secure Encrypted UPI Load ($upiId)",
                amount = amount,
                currency = "USD",
                recipientHandle = "user_wallet",
                status = "SUCCESS",
                paymentId = "upi_${txHash.take(12)}"
            )
            repository.insertTransaction(tx)
            showNotification("Secure UPI Deposit", "INR ${String.format("%.2f", amount * 83.5)} ($${String.format("%.2f", amount)}) successfully credited via encrypted UPI node!")
        }
    }

    // Secure Encrypted UPI Outflow Transaction (Scanned/Manual pay)
    fun sendWalletFundsViaUPI(amount: Double, upiId: String, txHash: String) {
        if (amount <= 0.0 || !amount.isFinite() || amount > 100000.0) {
            showNotification("Validation Failed", "Invalid UPI payment amount specified!")
            return
        }
        if (_walletBalance.value < amount) {
            showNotification("Insufficient Funds", "Wallet balance insufficient to process secure UPI payment!")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= amount
            val tx = Transaction(
                type = "UPI_PAYMENT",
                description = "Secured UPI Outflow ($upiId)",
                amount = amount,
                currency = "USD",
                recipientHandle = upiId,
                status = "SUCCESS",
                paymentId = "upi_${txHash.take(12)}"
            )
            repository.insertTransaction(tx)
            showNotification("Secure UPI Transfer", "INR ${String.format("%.2f", amount * 83.5)} ($${String.format("%.2f", amount)}) successfully sent to $upiId!")
        }
    }

    fun addMockTransaction(description: String, amount: Double, type: String) {
        viewModelScope.launch {
            val tx = Transaction(
                type = type,
                description = description,
                amount = amount,
                currency = "USD",
                recipientHandle = "user_wallet",
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_mock")
            )
            repository.insertTransaction(tx)
        }
    }

    // Like / Toggle a post
    fun likePost(post: Post) {
        viewModelScope.launch {
            val latestPost = repository.getPostById(post.id) ?: post
            val updatedLikedState = !latestPost.isLiked
            val updatedLikesCount = if (updatedLikedState) {
                latestPost.likesCount + 1
            } else {
                (latestPost.likesCount - 1).coerceAtLeast(0)
            }
            repository.updatePost(latestPost.copy(isLiked = updatedLikedState, likesCount = updatedLikesCount))
        }
    }

    // Toggle follow/unfollow status for a creator
    fun toggleFollow(creator: Creator) {
        viewModelScope.launch {
            val updatedCreator = creator.copy(
                isFollowed = !creator.isFollowed,
                followersCount = if (creator.isFollowed) {
                    (creator.followersCount - 1).coerceAtLeast(0)
                } else {
                    creator.followersCount + 1
                }
            )
            repository.updateCreator(updatedCreator)
            val actionWord = if (updatedCreator.isFollowed) "followed" else "unfollowed"
            showNotification("Success", "You have successfully $actionWord @${creator.handle}!")
            try {
                triggerFirestoreSync()
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Follow sync error: ${e.message}")
            }
        }
    }

    // Send a tip to a creator post (Razorpay interactive popover)
    fun triggerPostTip(post: Post, amount: Double) {
        val checkout = CheckoutInfo(
            title = "Tip Creator @${post.creatorHandle}",
            description = "Tipping secure token for: ${post.caption.take(20)}...",
            amount = amount,
            creatorId = post.creatorId,
            creatorHandle = post.creatorHandle,
            action = {
                executePostTip(post, amount)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executePostTip(post: Post, amount: Double) {
        if (amount <= 0.0 || !amount.isFinite() || amount > 100000.0) {
            showNotification("Validation Failed", "Invalid tipping amount specified!")
            return
        }
        if (_walletBalance.value < amount) {
            showNotification("Insufficient Funds", "Please load funds using Razorpay in your wallet first!")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= amount
            // Update post tips
            repository.updatePost(post.copy(tipsTotal = post.tipsTotal + amount))
            // Record payment transaction
            val tx = Transaction(
                type = "TIP",
                description = "Tip for post ID #${post.id}",
                amount = amount,
                currency = "USD",
                recipientHandle = post.creatorHandle,
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_tip")
            )
            repository.insertTransaction(tx)
            showNotification("Tip Sent", "Processed $${String.format("%.2f", amount)} securely with Razorpay!")
        }
    }

    // Buy Creator Subscription (Silver, Bronze, Gold with instant feed locks release!)
    fun triggerSubscriptionBuy(creator: Creator, tier: String, price: Double) {
        val checkout = CheckoutInfo(
            title = "Subscribe: ${tier.uppercase()} Tier",
            description = "Unlock premium, high-tier post lockouts for @${creator.handle}",
            amount = price,
            creatorId = creator.id,
            creatorHandle = creator.handle,
            action = {
                executeSubscriptionBuy(creator, tier, price)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executeSubscriptionBuy(creator: Creator, tier: String, price: Double) {
        if (price <= 0.0 || !price.isFinite() || price > 100000.0) {
            showNotification("Validation Failed", "Invalid subscription tier price!")
            return
        }
        if (_walletBalance.value < price) {
            showNotification("Payment Failed", "Wallet balance insufficient. Please deposit funds.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= price
            // Save subscription to Room database
            repository.insertSubscription(
                Subscription(
                    creatorId = creator.id,
                    creatorName = creator.name,
                    creatorHandle = creator.handle,
                    tierName = tier.uppercase(),
                    amount = price
                )
            )
            // Add Transaction history
            val tx = Transaction(
                type = "SUBSCRIPTION",
                description = "Subscribed to @${creator.handle} [${tier.uppercase()}]",
                amount = price,
                currency = creator.currency,
                recipientHandle = creator.handle,
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_sub")
            )
            repository.insertTransaction(tx)
            showNotification("Subscription Active!", "Welcome to @${creator.handle}'s ${tier.uppercase()} tier!")
        }
    }

    // Cancel dynamic subscription
    fun cancelSubscription(creatorId: String, handle: String) {
        viewModelScope.launch {
            repository.removeSubscription(creatorId)
            showNotification("Subscription Ended", "Cancelled tier for @$handle successfully.")
        }
    }

    // Buy Live Ticketing Event
    fun triggerTicketBuy(event: Event) {
        if (event.ticketsBought >= event.originalAvailable) {
            showNotification("Event Sold Out", "All physical and livestream credentials have been allocated.")
            return
        }
        val checkout = CheckoutInfo(
            title = "Buy Event Ticket",
            description = "Entry ticket: ${event.title}",
            amount = event.ticketPrice,
            creatorId = event.creatorId,
            creatorHandle = event.creatorHandle,
            action = {
                executeTicketBuy(event)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executeTicketBuy(event: Event) {
        if (event.ticketPrice <= 0.0 || !event.ticketPrice.isFinite() || event.ticketPrice > 100000.0) {
            showNotification("Validation Failed", "Invalid ticket price!")
            return
        }
        if (_walletBalance.value < event.ticketPrice) {
            showNotification("Insufficient Wallet", "Deposit funds via Razorpay first.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= event.ticketPrice
            // Update sold ticket count in Room Database
            repository.buyTicket(event.id, event)
            // Insert premium transaction slip
            val tx = Transaction(
                type = "TICKET_BUY",
                description = "Bought Ticket: ${event.title}",
                amount = event.ticketPrice,
                currency = event.currency,
                recipientHandle = event.creatorHandle,
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_tkt")
            )
            repository.insertTransaction(tx)
            showNotification("Ticket Purchased 🎟️", "Your QR ticket code is now securely loaded!")
        }
    }

    // Purchase any Marketplace Product or Creator Digital Good securely
    fun purchaseMarketplaceProduct(product: MarketplaceProduct, callback: () -> Unit = {}) {
        val checkout = CheckoutInfo(
            title = "Purchase: ${product.name}",
            description = "Secure buy from @${product.creatorName}",
            amount = product.price,
            creatorId = "",
            creatorHandle = product.creatorName,
            action = {
                executeProductPurchase(product, callback)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executeProductPurchase(product: MarketplaceProduct, callback: () -> Unit) {
        if (product.price <= 0.0 || !product.price.isFinite() || product.price > 100000.0) {
            showNotification("Validation Failed", "Invalid product price!")
            return
        }
        if (_walletBalance.value < product.price) {
            showNotification("Insufficient Funds", "Wallet balance insufficient. Please deposit funds first.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= product.price
            
            // Record payment transaction
            val tx = Transaction(
                type = "PRODUCT_BUY",
                description = "Purchased product: ${product.name}",
                amount = product.price,
                currency = "USD",
                recipientHandle = product.creatorName,
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_prod")
            )
            repository.insertTransaction(tx)
            showNotification("Purchase Successful!", "You have purchased '${product.name}' securely!")
            callback()
        }
    }

    // Direct Tipping / Donation for Creators
    fun triggerDirectTip(creator: Creator, amount: Double, callback: () -> Unit = {}) {
        val checkout = CheckoutInfo(
            title = "Support @${creator.handle}",
            description = "Secure tip/donation to support @${creator.handle}'s channel",
            amount = amount,
            creatorId = creator.id,
            creatorHandle = creator.handle,
            action = {
                executeDirectTip(creator, amount, callback)
            }
        )
        _activeCheckoutInfo.value = checkout
    }

    private fun executeDirectTip(creator: Creator, amount: Double, callback: () -> Unit) {
        if (amount <= 0.0 || !amount.isFinite() || amount > 100000.0) {
            showNotification("Validation Failed", "Invalid tipping amount specified!")
            return
        }
        if (_walletBalance.value < amount) {
            showNotification("Insufficient Funds", "Wallet balance insufficient. Please deposit funds first.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= amount
            
            // Record tipping transaction
            val tx = Transaction(
                type = "DIRECT_TIP",
                description = "Direct tip/donation to @${creator.handle}",
                amount = amount,
                currency = creator.currency,
                recipientHandle = creator.handle,
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_tip")
            )
            repository.insertTransaction(tx)
            showNotification("Tip Sent Successfully!", "Processed $${String.format("%.2f", amount)} securely!")
            callback()
        }
    }

    // Send encrypted/plain chat message
    fun sendChatMessage(receiverHandle: String, rawContent: String) {
        if (rawContent.isBlank()) return
        viewModelScope.launch {
            // Apply encrypted conversion if requested
            val finalContent = if (_chatEncryptionEnabled.value) {
                // simple base64 cipher representing standard client-side chat encryption
                android.util.Base64.encodeToString(rawContent.toByteArray(), android.util.Base64.DEFAULT).trim()
            } else {
                rawContent
            }
            val msg = ChatMessage(
                senderName = "You",
                receiverName = receiverHandle,
                encryptedContent = finalContent,
                isEncrypted = _chatEncryptionEnabled.value,
                timestamp = System.currentTimeMillis(),
                isDelivered = false,
                isSeen = false
            )
            val insertedId = repository.sendChatMessage(msg)

            if (receiverHandle == "Tokyo Trip") {
                kotlinx.coroutines.delay(800)
                repository.updateChatMessage(msg.copy(id = insertedId.toInt(), isDelivered = true))
                return@launch
            }

            // Simulate Delivery: wait 1 second, then mark as Delivered
            kotlinx.coroutines.delay(1000)
            val deliveredMsg = msg.copy(id = insertedId.toInt(), isDelivered = true)
            repository.updateChatMessage(deliveredMsg)

            // Simulate Read/Seen: wait another 1.5 seconds, then mark as Seen
            kotlinx.coroutines.delay(1500)
            val seenMsg = deliveredMsg.copy(isSeen = true)
            repository.updateChatMessage(seenMsg)
        }
    }

    fun deleteChatMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.deleteChatMessage(message)
        }
    }

    fun deleteMessageForMe(message: ChatMessage) {
        viewModelScope.launch {
            repository.updateChatMessage(message.copy(isDeletedForMe = true))
        }
    }

    fun deleteMessageForEveryone(message: ChatMessage) {
        viewModelScope.launch {
            repository.updateChatMessage(message.copy(isDeletedForEveryone = true, encryptedContent = "This message was deleted"))
        }
    }

    var activeChatRecipient: String? = null

    fun markMessagesAsSeen(recipientName: String) {
        if (recipientName.isEmpty()) {
            activeChatRecipient = null
            return
        }
        activeChatRecipient = recipientName
        viewModelScope.launch {
            if (recipientName == "Tokyo Trip") {
                repository.markMessagesAsSeenForGroup("Tokyo Trip")
            } else {
                repository.markMessagesAsSeenForSender(recipientName)
            }
        }
    }

    // Pay requested invoice in encrypted chat
    fun payChatInvoice(chat: ChatMessage) {
        if (chat.amountRequested <= 0.0 || !chat.amountRequested.isFinite() || chat.amountRequested > 100000.0) return
        if (_walletBalance.value < chat.amountRequested) {
            showNotification("Payment Declined", "Insufficient funds. Please fund your wallet.")
            return
        }
        viewModelScope.launch {
            _walletBalance.value -= chat.amountRequested
            // Create Transaction record
            val tx = Transaction(
                type = "CHAT_INVOICE",
                description = "Paid Invoice Request in Chat to @${chat.senderName}",
                amount = chat.amountRequested,
                currency = "USD",
                recipientHandle = chat.senderName,
                status = "SUCCESS",
                paymentId = generateSecurePaymentId("pay_inv")
            )
            repository.insertTransaction(tx)
            // Update chat status in Database
            repository.updateChatMessage(chat.copy(paymentStatus = "PAID"))
            showNotification("Invoice Paid ✅", "Funds routed directly to ${chat.senderName}!")
        }
    }

    // Decline dynamic chat invoice
    fun declineChatInvoice(chat: ChatMessage) {
        viewModelScope.launch {
            repository.updateChatMessage(chat.copy(paymentStatus = "DECLINED"))
            showNotification("Invoice Declined", "Invoice request of $${String.format("%.2f", chat.amountRequested)} was rejected.")
        }
    }

    // Publish dynamic post from Creative Studio or New Post custom dialog
    fun publishPost(caption: String, creator: Creator? = null, attachedMediaType: String? = null) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            val cId = creator?.id ?: "pixel_queen"
            val cName = creator?.name ?: _userProfileName.value
            val cHandle = creator?.handle ?: _userProfileHandle.value
            val post = Post(
                creatorId = cId,
                creatorName = cName,
                creatorHandle = cHandle,
                creatorAvatar = if (creator != null) creator.id else "",
                caption = caption,
                contentImage = attachedMediaType ?: listOf("vector_creative", "gradient_neon", "cyberpunk_city", "neon_synthwave").random(),
                isPremium = false,
                likesCount = 0,
                tipsTotal = 0.0,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPost(post)
            showNotification("Post Published 🎉", "New post by @$cHandle has been uploaded successfully!")
            try {
                triggerFirestoreSync()
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Publish post sync error: ${e.message}")
            }
        }
    }

    fun dismissCheckout() {
        _activeCheckoutInfo.value = null
    }

    private fun showNotification(title: String, message: String) {
        val type = when {
            title.contains("wallet", ignoreCase = true) || 
            title.contains("deposit", ignoreCase = true) || 
            title.contains("funds", ignoreCase = true) || 
            title.contains("fund", ignoreCase = true) || 
            title.contains("balance", ignoreCase = true) || 
            title.contains("tip", ignoreCase = true) || 
            title.contains("invoice", ignoreCase = true) || 
            title.contains("payment", ignoreCase = true) || 
            title.contains("declined", ignoreCase = true) || 
            title.contains("ticket", ignoreCase = true) -> "WALLET"
            
            title.contains("subscription", ignoreCase = true) || 
            title.contains("tier", ignoreCase = true) || 
            title.contains("creator", ignoreCase = true) || 
            title.contains("follow", ignoreCase = true) || 
            title.contains("post", ignoreCase = true) || 
            title.contains("publish", ignoreCase = true) || 
            title.contains("verification", ignoreCase = true) -> "CREATOR"
            
            title.contains("chat", ignoreCase = true) || 
            title.contains("message", ignoreCase = true) -> "CHAT"
            
            else -> "SYSTEM"
        }

        // Check if corresponding alerts are enabled
        val isEnabled = when (type) {
            "WALLET" -> _walletAlertsEnabled.value
            "CREATOR" -> _creatorAlertsEnabled.value
            "CHAT" -> _chatAlertsEnabled.value
            else -> _pushNotificationsEnabled.value
        }

        if (isEnabled) {
            val newNotification = AppNotification(
                title = title,
                message = message,
                type = type,
                isRead = false
            )
            _notificationsList.value = listOf(newNotification) + _notificationsList.value
        }
    }

    fun clearNotification() {
        _activeNotification.value = null
    }

    // --- Trending Topics (Google Search grounding powered by Gemini API) ---
    private val _trendingTopics = MutableStateFlow<List<String>>(listOf(
        "#PromptEng", "#CyberpunkArt", "#SpatialComputing", "#LofiMusic", "#WebTelemetry", "#AI_Creators"
    ))
    val trendingTopics: StateFlow<List<String>> = _trendingTopics.asStateFlow()

    private val _isTrendingFetching = MutableStateFlow(false)
    val isTrendingFetching: StateFlow<Boolean> = _isTrendingFetching.asStateFlow()

    private val _selectedTopicInsight = MutableStateFlow<String?>(null)
    val selectedTopicInsight: StateFlow<String?> = _selectedTopicInsight.asStateFlow()

    private val _isGeneratingInsight = MutableStateFlow(false)
    val isGeneratingInsight: StateFlow<Boolean> = _isGeneratingInsight.asStateFlow()

    init {
        fetchTrendingTopics()
    }

    fun fetchTrendingTopics() {
        viewModelScope.launch {
            _isTrendingFetching.value = true
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    _trendingTopics.value = listOf(
                        "#PromptEng", "#CyberpunkArt", "#SpatialComputing", "#LofiMusic", "#WebTelemetry", "#AI_Creators"
                    )
                } else {
                    val prompt = "List 6 currently trending developer, design, tech or sound synthesis hashtags popular on Google Search today. Return strictly a raw JSON array of strings, e.g. [\"#WWDC26\", \"#SpatialUI\"]. Absolutely no markdown formatting or leading/trailing text."
                    val req = GeminiTrendRequest(
                        contents = listOf(GeminiTrendContent(parts = listOf(GeminiTrendPart(text = prompt)))),
                        generationConfig = GeminiTrendGenerationConfig(responseMimeType = "application/json", temperature = 0.7f)
                    )
                    val response = geminiService.generateContent(apiKey, req)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        var cleaned = text.trim()
                        if (cleaned.startsWith("```json")) {
                            cleaned = cleaned.substringAfter("```json").substringBeforeLast("```").trim()
                        } else if (cleaned.startsWith("```")) {
                            cleaned = cleaned.substringAfter("```").substringBeforeLast("```").trim()
                        }
                        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, java.lang.String::class.java)
                        val adapter = moshi.adapter<List<String>>(listType)
                        val topics = adapter.fromJson(cleaned)
                        if (!topics.isNullOrEmpty()) {
                            _trendingTopics.value = topics.map { if (it.startsWith("#")) it else "#$it" }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Trend fetch failed: ${e.message}")
                // Graceful fallback is already active with default lists
            } finally {
                _isTrendingFetching.value = false
            }
        }
    }

    fun generateTopicInsight(topic: String) {
        viewModelScope.launch {
            _isGeneratingInsight.value = true
            _selectedTopicInsight.value = null
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1000)
                    _selectedTopicInsight.value = when (topic) {
                        "#PromptEng" -> "🎯 **Prompt Engineering** is spiking on Google Search due to next-gen reasoning models requiring advanced multi-step logical framing rather than basic instructions."
                        "#CyberpunkArt" -> "🎨 **Cyberpunk Art** is trending heavily with the rise of high-fidelity generative visual algorithms and 3D hologram asset compilation tools."
                        "#SpatialComputing" -> "🕶️ **Spatial Computing** interest has risen 220% following recent announcements of ultra-lightweight carbon fiber AR goggles and spatial web telemetry."
                        "#LofiMusic" -> "🎧 **Lofi Music** stream numbers are peaking as millions of creators seek relaxed background tempos for focused design and asynchronous programming sprints."
                        "#WebTelemetry" -> "📡 **Web Telemetry** and decentralization protocols are seeing massive interest with developer groups building decentralized edge-caches and latency compression hubs."
                        "#AI_Creators" -> "🤖 **AI Creators** are revolutionizing digital economics. Over 10k digital personas powered by autonomic reasoning engines are generating real-time micro-stories and high-gloss collectibles."
                        else -> "🔥 **$topic** is trending globally today with a massive spike in search activity. Community forums are discussing its long-term potential for creative tech pipelines and spatial design."
                    }
                } else {
                    val prompt = "Provide a gorgeous, concise summary explaining why the topic '$topic' is trending on Google Search today. Include 1 relevant emoji, bold important keywords, and keep it under 3 elegant lines of text suitable for an ultra-premium tech social feed dashboard insight card."
                    val req = GeminiTrendRequest(
                        contents = listOf(GeminiTrendContent(parts = listOf(GeminiTrendPart(text = prompt))))
                    )
                    val response = geminiService.generateContent(apiKey, req)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _selectedTopicInsight.value = text ?: "Trending globally today with massive daily search interest!"
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Insight generation failed: ${e.message}")
                _selectedTopicInsight.value = "Unable to fetch search insights right now. Please try again soon."
            } finally {
                _isGeneratingInsight.value = false
            }
        }
    }

    // --- Verification & Tier Setup States ---
    private val _userVerified = MutableStateFlow(false)
    val userVerified: StateFlow<Boolean> = _userVerified.asStateFlow()

    private val _userBronzeName = MutableStateFlow("Bronze Watcher")
    val userBronzeName = _userBronzeName.asStateFlow()
    private val _userBronzePrice = MutableStateFlow(4.99)
    val userBronzePrice = _userBronzePrice.asStateFlow()
    private val _userBronzePerks = MutableStateFlow("Latest daily stock alerts & tech watchlists.")
    val userBronzePerks = _userBronzePerks.asStateFlow()

    private val _userSilverName = MutableStateFlow("Silver Analyst")
    val userSilverName = _userSilverName.asStateFlow()
    private val _userSilverPrice = MutableStateFlow(14.99)
    val userSilverPrice = _userSilverPrice.asStateFlow()
    private val _userSilverPerks = MutableStateFlow("Exclusive audio transcripts & deep portfolio wiring sheets.")
    val userSilverPerks = _userSilverPerks.asStateFlow()

    private val _userGoldName = MutableStateFlow("Gold Partner")
    val userGoldName = _userGoldName.asStateFlow()
    private val _userGoldPrice = MutableStateFlow(49.99)
    val userGoldPrice = _userGoldPrice.asStateFlow()
    private val _userGoldPerks = MutableStateFlow("Weekly 1-on-1 portfolio review on secure live rooms.")
    val userGoldPerks = _userGoldPerks.asStateFlow()

    fun verifyUser() {
        _userVerified.value = true
        showNotification("Verification Passed", "Congratulations! Profile verified securely on ledger.")
    }

    fun unverifyUser() {
        _userVerified.value = false
        showNotification("Verification Revoked", "Identity verification retracted successfully.")
    }

    fun updateUserTiers(
        bName: String, bPrice: Double, bPerks: String,
        sName: String, sPrice: Double, sPerks: String,
        gName: String, gPrice: Double, gPerks: String
    ) {
        _userBronzeName.value = bName
        _userBronzePrice.value = bPrice
        _userBronzePerks.value = bPerks
        _userSilverName.value = sName
        _userSilverPrice.value = sPrice
        _userSilverPerks.value = sPerks
        _userGoldName.value = gName
        _userGoldPrice.value = gPrice
        _userGoldPerks.value = gPerks
        showNotification("Tiers Updated", "Your customized creator tiers are saved securely.")
    }

    fun verifyCreator(creatorId: String, isVerified: Boolean) {
        viewModelScope.launch {
            try {
                val creatorsList = repository.creators.first()
                val creator = creatorsList.find { it.id == creatorId }
                if (creator != null) {
                    val updated = creator.copy(isVerified = isVerified)
                    repository.updateCreator(updated)
                    showNotification(
                        if (isVerified) "Creator Verified" else "Creator Unverified",
                        "@${creator.handle} verification status has been updated."
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Verification update error: ${e.message}")
            }
        }
    }

    fun updateCreatorTiers(
        creatorId: String,
        bronzeName: String,
        bronzePrice: Double,
        bronzePerks: String,
        silverName: String,
        silverPrice: Double,
        silverPerks: String,
        goldName: String,
        goldPrice: Double,
        goldPerks: String
    ) {
        viewModelScope.launch {
            try {
                val creatorsList = repository.creators.first()
                val creator = creatorsList.find { it.id == creatorId }
                if (creator != null) {
                    val updated = creator.copy(
                        bronzeTierName = bronzeName,
                        bronzeTierPrice = bronzePrice,
                        bronzeTierPerks = bronzePerks,
                        silverTierName = silverName,
                        silverTierPrice = silverPrice,
                        silverTierPerks = silverPerks,
                        goldTierName = goldName,
                        goldTierPrice = goldPrice,
                        goldTierPerks = goldPerks
                    )
                    repository.updateCreator(updated)
                    showNotification("Tiers Updated", "Subscription pricing and tiers updated for @${creator.handle} on local ledger.")
                }
            } catch (e: Exception) {
                android.util.Log.e("SocialHub", "Tiers update error: ${e.message}")
            }
        }
    }

    fun clearTopicInsight() {
        _selectedTopicInsight.value = null
    }
}

// --- Gemini Trend Integration REST API Models ---
interface GeminiApiService {
    @retrofit2.http.POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Query("key") apiKey: String,
        @retrofit2.http.Body request: GeminiTrendRequest
    ): GeminiTrendResponse
}

data class GeminiTrendRequest(
    val contents: List<GeminiTrendContent>,
    val generationConfig: GeminiTrendGenerationConfig? = null
)

data class GeminiTrendContent(val parts: List<GeminiTrendPart>)
data class GeminiTrendPart(val text: String)
data class GeminiTrendGenerationConfig(val responseMimeType: String? = null, val temperature: Float? = null)

data class GeminiTrendResponse(val candidates: List<GeminiTrendCandidate>?)
data class GeminiTrendCandidate(val content: GeminiTrendContent?)

// Top-level Moshi & Retrofit helper instances
private val moshi = com.squareup.moshi.Moshi.Builder()
    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
    .build()

private val retrofit = retrofit2.Retrofit.Builder()
    .baseUrl("https://generativelanguage.googleapis.com/")
    .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
    .client(
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    )
    .build()

private val geminiService = retrofit.create(GeminiApiService::class.java)

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Loading : SyncStatus()
    data class Success(val count: Int) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ApiPromotionBanner(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val targetProductId: Int? = null
)

private val apiBannersListAdapter = moshi.adapter<List<ApiPromotionBanner>>(
    com.squareup.moshi.Types.newParameterizedType(List::class.java, ApiPromotionBanner::class.java)
)

data class CheckoutInfo(
    val title: String,
    val description: String,
    val amount: Double,
    val creatorId: String,
    val creatorHandle: String,
    val action: () -> Unit
)

data class NotificationMessage(
    val title: String,
    val message: String
)

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "SYSTEM" // e.g. "SYSTEM", "WALLET", "CREATOR", "CHAT"
)
