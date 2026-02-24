package com.gamingboost.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// ======================
// AUTH
// ======================

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val message: String, val user: MeResponse, val token: String)

data class MeResponse(
    val id: Long,
    val name: String,
    val email: String
)

// ======================
// BOOSTS
// ======================

data class Boost(
    val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val qty_total: Int? = null
)

data class BuyRequest(val qty: Int)

// ======================
// ORDERS
// ======================

data class CreateOrderItem(
    val boost_id: Long,
    val qty: Int
)

data class CreateOrderRequest(
    val provider: String, // stripe | mercadopago | paypal
    val items: List<CreateOrderItem>
)

data class CreateOrderResponse(
    val order_id: Long,
    val status: String,
    val provider: String,
    val total_amount: Int
)

data class PayOrderResponse(
    val checkout_url: String
)

data class OrderItemDto(
    val id: Long,
    val order_id: Long,
    val boost_id: Long,
    val qty: Int,
    val unit_price: Int
)

data class OrderDto(
    val id: Long,
    val user_id: Long,
    val status: String,
    val provider: String,
    val total_amount: Int,
    val currency: String,
    val provider_ref: String?,
    val created_at: String,
    val updated_at: String,
    val items: List<OrderItemDto>
)

// ======================
// API INTERFACE
// ======================

interface ApiService {

    // ---------- TEST ----------
    @GET("api/ping")
    suspend fun ping(): Response<Map<String, Any>>

    // ---------- AUTH ----------
    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/me")
    suspend fun me(
        @Header("Authorization") bearer: String
    ): Response<MeResponse>

    @POST("api/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    ): Response<Map<String, Any>>

    // ---------- BOOSTS ----------
    @GET("api/boosts")
    suspend fun boosts(
        @Header("Authorization") bearer: String
    ): Response<List<Boost>>

    @POST("api/boosts/{id}/buy")
    suspend fun buyBoost(
        @Header("Authorization") bearer: String,
        @Path("id") id: Long,
        @Body body: BuyRequest
    ): Response<Map<String, Any>>

    @GET("api/my-boosts")
    suspend fun myBoosts(
        @Header("Authorization") bearer: String
    ): Response<List<Boost>>

    // ---------- ORDERS ----------
    @POST("api/orders")
    suspend fun createOrder(
        @Header("Authorization") bearer: String,
        @Body body: CreateOrderRequest
    ): Response<CreateOrderResponse>

    @POST("api/orders/{id}/pay")
    suspend fun payOrder(
        @Header("Authorization") bearer: String,
        @Path("id") id: Long
    ): Response<PayOrderResponse>

    @GET("api/orders")
    suspend fun myOrders(
        @Header("Authorization") bearer: String
    ): Response<List<OrderDto>>

    @GET("api/orders/{id}")
    suspend fun orderDetail(
        @Header("Authorization") bearer: String,
        @Path("id") id: Long
    ): Response<OrderDto>
}