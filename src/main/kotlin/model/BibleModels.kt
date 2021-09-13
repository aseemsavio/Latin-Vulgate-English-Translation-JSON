package model

data class Verse(
    val chapter: Int,
    val verse: Int,
    val textEn: String,
    val textLa: String,
    val notes: String?
)

fun Verse(collector: VerseCollector): Verse =
    Verse(collector.chapter, collector.verse, collector.textEn, collector.textLa, collector.notes)

data class VerseCollector(
    var chapter: Int = 0,
    var verse: Int = 0,
    var textEn: String = "",
    var textLa: String = "",
    var notes: String? = null
)

data class Chapter(
    val chapter: Int,
    val verses: Set<Verse>
)

data class Chapters(
    val chapters: Set<Chapter>
)