package model

import kotlinx.serialization.Serializable

@Serializable
data class Verse(
    val chapter: Int,
    val verse: Int,
    val text: String,
    val notes: String?
)

fun EnglishVerse(mlv: MultiLingualVerse) =
    Verse(mlv.chapter, mlv.verse, mlv.textEn, mlv.notes)

fun LatinVerse(mlv: MultiLingualVerse) =
    Verse(mlv.chapter, mlv.verse, mlv.textLa, mlv.notes)

@Serializable
data class Chapter(
    val chapter: Int,
    val verses: List<Verse>
)

@Serializable
data class Book(
    val bookNumber: Int,
    val book: String,
    val testament: String,
    val chapters: List<Chapter>
)
