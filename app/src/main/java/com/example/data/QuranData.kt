package com.example.data

import com.example.data.model.Reciter

object QuranData {
    val reciters: List<Reciter> = EveryAyahReciters.all
    val popularReciters: List<Reciter> = EveryAyahReciters.popular
}
