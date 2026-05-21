package com.example.shilpa_kala

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "portfolios")
data class PortfolioItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val artisanName: String,
    val material: String,
    val price: String,
    val imageUri: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "artisan_profile")
data class ArtisanProfile(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val region: String,
    val craftType: String,
    val upiId: String? = null
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val cartId: Int = 0,
    val productId: Int,
    val productName: String,
    val artisanName: String,
    val price: String,
    val imageUri: String
)

@Entity(tableName = "product_reviews")
data class ProductReview(
    @PrimaryKey(autoGenerate = true) val reviewId: Int = 0,
    val productId: Int,
    val userName: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolios ORDER BY timestamp DESC")
    fun getAllPortfolios(): Flow<List<PortfolioItem>>

    @Query("SELECT * FROM portfolios WHERE id = :id")
    suspend fun getPortfolioById(id: Int): PortfolioItem?

    @Insert
    suspend fun insert(portfolio: PortfolioItem)

    @Delete
    suspend fun delete(portfolio: PortfolioItem)

    // Artisan Profile
    @Query("SELECT * FROM artisan_profile WHERE id = 0")
    fun getProfile(): Flow<ArtisanProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ArtisanProfile)

    // Cart Operations
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItem)

    @Query("DELETE FROM cart_items WHERE cartId = :id")
    suspend fun removeFromCart(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Review Operations
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY timestamp DESC")
    fun getReviewsForProduct(productId: Int): Flow<List<ProductReview>>

    @Insert
    suspend fun addReview(review: ProductReview)
}

@Database(entities = [PortfolioItem::class, ArtisanProfile::class, CartItem::class, ProductReview::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shilpa_kala_db"
                )
                .fallbackToDestructiveMigration() // Simple for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
