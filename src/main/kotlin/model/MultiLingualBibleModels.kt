package model

import kotlinx.serialization.Serializable

@Serializable
data class MultiLingualVerse(
    val chapter: Int,
    val verse: Int,
    val textEn: String,
    val textLa: String,
    val notes: String?
)

fun multiLingualVerse(collector: VerseCollector): MultiLingualVerse =
    MultiLingualVerse(collector.chapter, collector.verse, collector.textEn, collector.textLa, collector.notes)

data class VerseCollector(
    var chapter: Int = -1,
    var verse: Int = -1,
    var textEn: String = "",
    var textLa: String = "",
    var notes: String? = null
)

@Serializable
data class MultiLingualChapter(
    val chapter: Int,
    val verses: List<MultiLingualVerse>
)

@Serializable
data class Chapters(
    val chapters: List<MultiLingualChapter>
)

@Serializable
data class MultiLingualBook(
    val bookNumber: Int,
    val book: String,
    val testament: String,
    val chapters: List<MultiLingualChapter>
)
