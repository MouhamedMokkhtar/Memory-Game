package com.mokhtar.mymemorygame.models

data class MemoryCard(
    val identifier : Int,
    var isFaceUp : Boolean = false,
    var isMatch : Boolean = false
)
