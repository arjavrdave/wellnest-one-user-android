package com.wellnest.one.model

data class SetupMessageInfo(
    val messages: List<String> = listOf(
        "Message 1",
        "Message 2",
        "Message 3",
        "Message 4",
        "Message 5"
    ),
    val msgTime: Int,// Seconds
    val successTime: Int,
    val totalTime: Int,
    val errorMsg: String,
    val threshold : Double
)
