package com.example.data

import androidx.annotation.DrawableRes
import com.example.R

object ReciterAvatars {
    @DrawableRes
    fun resIdFor(nameEnglish: String): Int = when (nameEnglish) {
        "Abdul Basit Murattal" -> R.drawable.reciter_abdul_basit_murattal
        "Abdul Basit Mujawwad" -> R.drawable.reciter_abdul_basit_mujawwad
        "Abdullah Basfar" -> R.drawable.reciter_abdullah_basfar
        "Abdurrahmaan As-Sudais" -> R.drawable.reciter_sudais
        "AbdulSamad QuranExplorer.Com" -> R.drawable.reciter_abdulsamad
        "Abu Bakr Ash-Shaatree" -> R.drawable.reciter_shaatree
        "Ahmed ibn Ali al-Ajamy QuranExplorer.Com",
        "Ahmed ibn Ali al-Ajamy KetabAllah.Net",
        -> R.drawable.reciter_ajamy
        "Alafasy" -> R.drawable.reciter_alafasy
        "Ghamadi" -> R.drawable.reciter_ghamadi
        "Hani Rifai" -> R.drawable.reciter_hani_rifai
        "Husary" -> R.drawable.reciter_husary
        "Husary Mujawwad" -> R.drawable.reciter_husary_mujawwad
        "Hudhaify" -> R.drawable.reciter_hudhaify
        "Ibrahim Akhdar" -> R.drawable.reciter_ibrahim_akhdar
        "Maher Al Muaiqly" -> R.drawable.reciter_maher
        "Menshawi",
        "Minshawy Mujawwad",
        "Minshawy Murattal",
        -> R.drawable.reciter_menshawi
        "Mohammad al Tablaway" -> R.drawable.reciter_tablaway
        "Muhammad Ayyoub" -> R.drawable.reciter_ayyoub
        "Muhammad Jibreel" -> R.drawable.reciter_jibreel
        "Mustafa Ismail" -> R.drawable.reciter_mustafa_ismail
        "Saood bin Ibraaheem Ash-Shuraym" -> R.drawable.reciter_shuraym
        else -> R.drawable.reciter_alafasy
    }
}
