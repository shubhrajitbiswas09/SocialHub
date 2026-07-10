package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialHubDao {
    // === Creators ===
    @Query("SELECT * FROM creators")
    fun getAllCreators(): Flow<List<Creator>>

    @Query("SELECT * FROM creators WHERE id = :id LIMIT 1")
    fun getCreatorById(id: String): Flow<Creator?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreator(creator: Creator)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreators(creators: List<Creator>)

    @Update
    suspend fun updateCreator(creator: Creator)

    // === Posts ===
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts")
    suspend fun getPostsList(): List<Post>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Int): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Update
    suspend fun updatePost(post: Post)

    // === Subscriptions ===
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE creatorId = :creatorId")
    suspend fun deleteSubscription(creatorId: String)

    // === Transactions ===
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    // === Chat Messages ===
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getChatMessageCount(): Int

    @Query("DELETE FROM chat_messages WHERE id IN (SELECT id FROM chat_messages ORDER BY timestamp ASC LIMIT :limit)")
    suspend fun deleteOldestChatMessages(limit: Int)

    @Update
    suspend fun updateChatMessage(message: ChatMessage)

    @Delete
    suspend fun deleteChatMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET isSeen = 1 WHERE senderName = :senderName AND receiverName = 'You' AND isSeen = 0")
    suspend fun markMessagesAsSeenForSender(senderName: String)

    @Query("UPDATE chat_messages SET isSeen = 1 WHERE receiverName = :receiverName AND senderName != 'You' AND isSeen = 0")
    suspend fun markMessagesAsSeenForGroup(receiverName: String)

    // === Events ===
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    // === Marketplace Products ===
    @Query("SELECT * FROM marketplace_products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<MarketplaceProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: MarketplaceProduct)

    @Query("DELETE FROM marketplace_products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    // === Marketplace Banners ===
    @Query("SELECT * FROM marketplace_banners ORDER BY id DESC")
    fun getAllBanners(): Flow<List<MarketplaceBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: MarketplaceBanner)

    @Query("DELETE FROM marketplace_banners WHERE id = :id")
    suspend fun deleteBannerById(id: Int)

    @Query("DELETE FROM marketplace_banners")
    suspend fun deleteAllBanners()

    @Query("DELETE FROM creators")
    suspend fun deleteAllCreators()

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("DELETE FROM marketplace_products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllChatMessages()

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAllSubscriptions()
}
