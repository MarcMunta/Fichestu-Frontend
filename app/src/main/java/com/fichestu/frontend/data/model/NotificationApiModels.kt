package com.fichestu.frontend.data.model

data class NotificationListResponseDto(
    val notifications: List<NotificationDto>,
    val unreadCount: Int
)

data class NotificationDto(
    val id: Int,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String
)
