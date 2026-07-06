package com.example.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task

class SocialHubRepository(private val dao: SocialHubDao, private val context: Context) {

    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
            }
        }
    }

    private fun getFirestoreSafe(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("socialhub-uxgqwl")
                    .setApplicationId("com.aistudio.socialhub.uxgqwl")
                    .setApiKey("mock-api-key-to-prevent-crash")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            android.util.Log.e("SocialHubRepo", "Firestore init failed: ${e.message}")
            null
        }
    }

    suspend fun uploadPostToFirestore(post: Post) {
        val firestore = getFirestoreSafe() ?: return
        try {
            kotlinx.coroutines.withTimeoutOrNull(1000L) {
                val docData = hashMapOf(
                    "id" to post.id,
                    "creatorId" to post.creatorId,
                    "creatorName" to post.creatorName,
                    "creatorHandle" to post.creatorHandle,
                    "creatorAvatar" to post.creatorAvatar,
                    "caption" to post.caption,
                    "contentImage" to post.contentImage,
                    "isPremium" to post.isPremium,
                    "requiredTier" to post.requiredTier,
                    "likesCount" to post.likesCount,
                    "tipsTotal" to post.tipsTotal,
                    "timestamp" to post.timestamp
                )
                val docName = "post_${post.creatorId}_${post.timestamp}"
                firestore.collection("posts")
                    .document(docName)
                    .set(docData)
                    .awaitResult()
            }
        } catch (e: Exception) {
            android.util.Log.e("SocialHubRepo", "Firestore upload failed: ${e.message}")
        }
    }

    suspend fun fetchFollowedPostsFromFirestore(followedCreatorIds: Set<String>): List<Post> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val firestore = getFirestoreSafe() ?: return@withContext emptyList<Post>()
        try {
            kotlinx.coroutines.withTimeoutOrNull(1200L) {
                val querySnapshot = firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .awaitResult()

                val fetchedPosts = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getLong("id")?.toInt() ?: 0
                        val creatorId = doc.getString("creatorId") ?: "unknown"
                        val creatorName = doc.getString("creatorName") ?: "Unknown Creator"
                        val creatorHandle = doc.getString("creatorHandle") ?: "unknown"
                        val creatorAvatar = doc.getString("creatorAvatar") ?: "avatar_default"
                        val caption = doc.getString("caption") ?: ""
                        val contentImage = doc.getString("contentImage") ?: "gradient_neon"
                        val isPremium = doc.getBoolean("isPremium") ?: false
                        val requiredTier = doc.getString("requiredTier") ?: "FREE"
                        val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                        val tipsTotal = doc.getDouble("tipsTotal") ?: 0.0
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val isLiked = doc.getBoolean("isLiked") ?: false

                        Post(
                            id = id,
                            creatorId = creatorId,
                            creatorName = creatorName,
                            creatorHandle = creatorHandle,
                            creatorAvatar = creatorAvatar,
                            caption = caption,
                            contentImage = contentImage,
                            isPremium = isPremium,
                            requiredTier = requiredTier,
                            likesCount = likesCount,
                            tipsTotal = tipsTotal,
                            timestamp = timestamp,
                            isLiked = isLiked
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                if (fetchedPosts.isNotEmpty()) {
                    val localPostsMap = dao.getPostsList().associateBy { it.id }
                    val mergedPosts = fetchedPosts.map { fetched ->
                        val local = localPostsMap[fetched.id]
                        if (local != null) {
                            fetched.copy(isLiked = local.isLiked)
                        } else {
                            fetched
                        }
                    }
                    dao.insertPosts(mergedPosts)
                }

                fetchedPosts.filter { it.creatorId in followedCreatorIds }
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("SocialHubRepo", "Fetch followed posts failed: ${e.message}")
            emptyList<Post>()
        }
    }

    val creators: Flow<List<Creator>> = dao.getAllCreators()
    val posts: Flow<List<Post>> = dao.getAllPosts()
    val subscriptions: Flow<List<Subscription>> = dao.getAllSubscriptions()
    val transactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val chatMessages: Flow<List<ChatMessage>> = dao.getAllChatMessages()
    val events: Flow<List<Event>> = dao.getAllEvents()

    fun getCreatorById(id: String): Flow<Creator?> = dao.getCreatorById(id)

    suspend fun insertCreator(creator: Creator) = dao.insertCreator(creator)
    suspend fun updateCreator(creator: Creator) = dao.updateCreator(creator)
    
    suspend fun insertPost(post: Post) {
        dao.insertPost(post)
        uploadPostToFirestore(post)
    }

    suspend fun getPostById(id: Int): Post? = dao.getPostById(id)
    
    suspend fun updatePost(post: Post) {
        dao.updatePost(post)
        uploadPostToFirestore(post)
    }
    
    suspend fun insertSubscription(subscription: Subscription) {
        dao.insertSubscription(subscription)
    }
    
    suspend fun removeSubscription(creatorId: String) {
        dao.deleteSubscription(creatorId)
    }

    suspend fun insertTransaction(transaction: Transaction) = dao.insertTransaction(transaction)

    suspend fun sendChatMessage(message: ChatMessage) = dao.insertChatMessage(message)
    suspend fun updateChatMessage(message: ChatMessage) = dao.updateChatMessage(message)
    suspend fun markMessagesAsSeenForSender(senderName: String) = dao.markMessagesAsSeenForSender(senderName)
    suspend fun markMessagesAsSeenForGroup(receiverName: String) = dao.markMessagesAsSeenForGroup(receiverName)

    suspend fun buyTicket(eventId: Int, event: Event) {
        dao.updateEvent(event.copy(ticketsBought = event.ticketsBought + 1))
    }

    val marketplaceProducts: Flow<List<MarketplaceProduct>> = dao.getAllProducts()
    val marketplaceBanners: Flow<List<MarketplaceBanner>> = dao.getAllBanners()

    suspend fun insertProduct(product: MarketplaceProduct) = dao.insertProduct(product)
    suspend fun deleteProductById(id: Int) = dao.deleteProductById(id)

    suspend fun insertBanner(banner: MarketplaceBanner) = dao.insertBanner(banner)
    suspend fun deleteBannerById(id: Int) = dao.deleteBannerById(id)
    suspend fun clearAllBanners() = dao.deleteAllBanners()

    suspend fun seedInitialData() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val listCreators = listOf(
            Creator(
                id = "alex_johnson",
                name = "Alex Johnson",
                handle = "alex_johnson",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                description = "Professional Barista and Coffee Roaster based in San Francisco. Sharing daily aesthetic latte art & gourmet reviews ☕️🍃",
                followersCount = 84100,
                bronzeTierPrice = 1.99,
                silverTierPrice = 5.00,
                goldTierPrice = 10.00,
                isVerified = true,
                isFollowed = true
            ),
            Creator(
                id = "alex_future",
                name = "Alex Chen",
                handle = "ALX_FUTURE",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                description = "Pro Investor | Visionary Trader specializing in high-yield tech assets. Author of ALX daily digest.",
                followersCount = 14200,
                bronzeTierPrice = 5.00,
                silverTierPrice = 25.00,
                goldTierPrice = 99.00,
                isVerified = false,
                isFollowed = true
            ),
            Creator(
                id = "aura_music",
                name = "Aura Rhythm",
                handle = "music_guru",
                avatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
                description = "Synthwave producer & lo-fi beatmaker. Crafting late night deep-space waves for global frequencies. 🌌🎧",
                followersCount = 42800,
                bronzeTierPrice = 1.99,
                silverTierPrice = 5.99,
                goldTierPrice = 12.99,
                isVerified = true,
                isFollowed = true
            ),
            Creator(
                id = "pixel_queen",
                name = "Pixel Queen",
                handle = "PixelQueen",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                description = "Crypto artist and 3D animator. Drop manager for Neon Genesis Collections and virtual vibes.",
                followersCount = 94200,
                bronzeTierPrice = 2.99,
                silverTierPrice = 9.99,
                goldTierPrice = 24.99,
                isVerified = false,
                isFollowed = true
            )
        )
        dao.insertCreators(listCreators)

        val listPosts = listOf(
            Post(
                id = 100,
                creatorId = "alex_johnson",
                creatorName = "Alex Johnson",
                creatorHandle = "alex_johnson",
                creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                caption = "Just found this amazing spot downtown! The latte art is incredible. ☕️✨ #CoffeeLover",
                contentImage = "latte_art",
                isPremium = false,
                likesCount = 1200,
                tipsTotal = 0.0
            ),
            Post(
                id = 1,
                creatorId = "aura_music",
                creatorName = "Aura Rhythm",
                creatorHandle = "music_guru",
                creatorAvatar = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150",
                caption = "Chasing neon sunsets in the studio tonight. What vibe are we feeling? Synthesizer oscillators on point. 🎹✨ #synthwave #lofi #creators",
                contentImage = "music_studio",
                isPremium = false,
                likesCount = 1420,
                tipsTotal = 45.0
            ),
            Post(
                id = 2,
                creatorId = "alex_future",
                creatorName = "Alex Chen",
                creatorHandle = "ALX_FUTURE",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                caption = "[🔓 BRONZE TIER EXCLUSIVE] Deep dive technical report on next-generation silicon stock targets. High growth projections inside 📈",
                contentImage = "keyboard_schema",
                isPremium = true,
                requiredTier = "BRONZE",
                likesCount = 312,
                tipsTotal = 150.0
            ),
            Post(
                id = 3,
                creatorId = "alex_johnson",
                creatorName = "Alex Johnson",
                creatorHandle = "alex_johnson",
                creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                caption = "[🔓 SILVER EXCLUSIVE] Sourcing raw coffee beans list of 8 secret roasters directly from Ethiopia, Costa Rica and Colombia. Essential knowledge for cafe owners! 🥐☕️",
                contentImage = "croissants",
                isPremium = true,
                requiredTier = "SILVER",
                likesCount = 980,
                tipsTotal = 450.0
            )
        )
        dao.insertPosts(listPosts)
        try {
            listPosts.forEach { uploadPostToFirestore(it) }
        } catch (e: Exception) {
            android.util.Log.e("SocialHubRepo", "Populate and upload initial posts failed: ${e.message}")
        }

        val listEvents = listOf(
            Event(
                id = 1,
                creatorId = "pixel_queen",
                creatorName = "Pixel Queen",
                creatorHandle = "PixelQueen",
                title = "Neon Genesis Collection: Private Drop Visual Live stream",
                description = "Unlock the premier 3D hologram asset drops. Private livestream event with special early auction access lists.",
                dateString = "June 30, 2026 at 10:00 PM UTC",
                ticketPrice = 50.00,
                originalAvailable = 100,
                ticketsBought = 12,
                location = "Cyber World virtual drop stream Room 4"
            ),
            Event(
                id = 2,
                creatorId = "aura_music",
                creatorName = "Aura Rhythm",
                creatorHandle = "music_guru",
                title = "Celestial Solstice: Live Ambient Set",
                description = "Join an exclusive private digital live concert broadcasting live from a mountain studio at sunset. Custom visuals, spatial synthesizer streams, and real-time interactive chats.",
                dateString = "June 25, 2026 at 09:00 PM UTC",
                ticketPrice = 15.00,
                originalAvailable = 150,
                ticketsBought = 42,
                location = "SocialHub Live Stream Room 1"
            )
        )
        dao.insertEvents(listEvents)

        // Seed some conversations
        val seedChats = listOf(
            ChatMessage(
                id = 1,
                senderName = "Alex Rivera",
                receiverName = "You",
                encryptedContent = "R290IHRoZSB0cmFuc2ZlciwgdGhhbmtzIGZvciBzZW5kaW5nIGl0IHNvIGZhc3Qh", // Base64 for "Got the transfer, thanks..."
                isEncrypted = true,
                timestamp = System.currentTimeMillis() - 7200000,
                isSeen = true
            ),
            ChatMessage(
                id = 2,
                senderName = "Sarah Chen",
                receiverName = "You",
                encryptedContent = "Are we still meeting at 5 for coffee downtown? Let me know, I'm heading out soon! ☕️",
                isEncrypted = false,
                timestamp = System.currentTimeMillis() - 3600000,
                isSeen = false
            ),
            ChatMessage(
                id = 3,
                senderName = "Crypto Degens",
                receiverName = "You",
                encryptedContent = "QG1paGU6IHNlY3VyZSBrZXkgdXBkYXRlZCBmb3IgdGhlIEVUSEVyZWXVbSBtYWlubmV0IHZhdWx0Lg==", // Base64 for "@mihe: secure key updated..."
                isEncrypted = true,
                timestamp = System.currentTimeMillis() - 120000,
                isSeen = true
            ),
            ChatMessage(
                id = 4,
                senderName = "Alex Rivera",
                receiverName = "You",
                encryptedContent = "🔒 [PENDING PAYMENT REQUEST - $45.00 FOR DINNER]",
                isEncrypted = false,
                isFinancialRequest = true,
                amountRequested = 45.00,
                payRefId = "pay_dinner_ref_001",
                paymentStatus = "NONE",
                timestamp = System.currentTimeMillis() - 1700000,
                isSeen = true
            )
        )
        for (chat in seedChats) {
            dao.insertChatMessage(chat)
        }

        val initialProducts = listOf(
            MarketplaceProduct(
                id = 1,
                name = "Apex Audio Processor",
                description = "An advanced, studio-grade virtual synthesizer engine and dynamic audio compressor. Supports spatial mixes, low-latency processing, and custom preset filters for your live studio broadcasts.",
                price = 39.99,
                imageUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800",
                logoUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=150",
                affiliateLink = "https://synthuniverse.com/apex-audio-processor",
                creatorName = "Aura Rhythm"
            ),
            MarketplaceProduct(
                id = 2,
                name = "LedgerPro Crypto Wallet",
                description = "Next-generation biometric physical crypto wallet with Bluetooth, dynamic color screen, secure offline cold storage, and dual-chip multi-sig encryption. Keep your assets absolutely secure.",
                price = 149.00,
                imageUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?w=800",
                logoUrl = "https://images.unsplash.com/photo-1621761191319-c6fb62004040?w=150",
                affiliateLink = "https://hardwarewallet.com/ledgerpro",
                creatorName = "ALX_FUTURE"
            ),
            MarketplaceProduct(
                id = 3,
                name = "Creative Glow Lightroom Pack",
                description = "Unlocks 42 premium vintage and vaporwave LUT presets. Elevate your storytelling aesthetic with high-fidelity color schemes specifically balanced for night scenes and neon low-light portrait photography.",
                price = 14.50,
                imageUrl = "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=800",
                logoUrl = "https://images.unsplash.com/photo-1554080353-a576cf803bda?w=150",
                affiliateLink = "https://filtermasters.com/creative-glow",
                creatorName = "Pixel Queen"
            ),
            MarketplaceProduct(
                id = 4,
                name = "Aesthetic Latte Art & Brewing (E-book)",
                description = "The ultimate guide to home brewing, bean selection, and pouring perfect latte art. Includes step-by-step illustrations, temperature charts, and milk frothing parameters.",
                price = 4.99,
                imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800",
                logoUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=150",
                affiliateLink = "https://coffeeguru.com/latte-art-guide",
                creatorName = "Alex Johnson"
            ),
            MarketplaceProduct(
                id = 5,
                name = "High-Yield Portfolio Wiring (E-book)",
                description = "Master the art of risk-allocated portfolio positioning. Dive into real-time ledger mechanics, asset-backed networks, and secure trading cycles designed to optimize yield and leverage safely.",
                price = 19.99,
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800",
                logoUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=150",
                affiliateLink = "https://futureinvest.com/wiring-guide",
                creatorName = "Alex Chen"
            ),
            MarketplaceProduct(
                id = 6,
                name = "Late Night Celestial Synth Loops (Presets)",
                description = "An extensive pack of 65 retro-futuristic synthwave loops, drum samples, and custom MIDI templates calibrated specifically for chillwave, lofi, and late night celestial ambient tracks.",
                price = 9.99,
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                logoUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=150",
                affiliateLink = "https://musicguru.com/celestial-loops",
                creatorName = "Aura Rhythm"
            ),
            MarketplaceProduct(
                id = 7,
                name = "3D Neon Blender Cycles Shaders (Presets)",
                description = "Add instant cyber aesthetic to your 3D models with these pre-calibrated neon shaders, holographic glass textures, and volume emissions optimized for Blender Cycles engine.",
                price = 12.50,
                imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800",
                logoUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=150",
                affiliateLink = "https://artstation.com/neon-shaders",
                creatorName = "Pixel Queen"
            )
        )
        for (prod in initialProducts) {
            dao.insertProduct(prod)
        }

        val initialBanners = listOf(
            MarketplaceBanner(
                id = 1,
                title = "Vaporwave Retro Beats Drop",
                description = "Get the newly updated Synthwaves synthesizer packs and celestial audio filters. Curated by Aura Rhythm.",
                imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=1200",
                targetProductId = 1
            ),
            MarketplaceBanner(
                id = 2,
                title = "Hardware Sovereignty",
                description = "Order the LedgerPro biometric wallet and secure your decentralized digital ecosystem.",
                imageUrl = "https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=1200",
                targetProductId = 2
            )
        )
        for (banner in initialBanners) {
            dao.insertBanner(banner)
        }
    }
}
