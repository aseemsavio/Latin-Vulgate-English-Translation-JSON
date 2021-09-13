package scrape

import model.Verse
import model.VerseCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * This class scrapes the Latin Vulgate and stores the data as JSON files.
 */
private class Scraper {

    fun scrape(url: String) {

        // read page content as a String
        val doc = connect(url).bodyAsString()
        // Get only the useful parts (English, Latin and notes) as a List
        val split = doc.split("<br>")
            .map { it.trim() }
            .filter { it.startsWith("{") || it.startsWith("~") }
            .toMutableList()

        var prevIdStr = ""
        val verses = mutableSetOf<Verse>()
        var verseCollector = VerseCollector()

        for (i in 0 until split.size) {
            val it = split[i]
            if (it.trim().startsWith("{")) {
                val endOfId = it.indexOf("}")
                val idStr = it.substring(0, endOfId + 1)
                if (idStr == prevIdStr) {
                    val english = it.substring(endOfId + 1).trim()
                    verseCollector.textEn = english
                    if (verseCollector.chapter != 0 && verseCollector.verse != 0)
                        verses += Verse(verseCollector)
                } else {
                    verseCollector = VerseCollector()
                    val (chapter, verse) = chapterVerse(idStr)
                    val latin = it.substring(endOfId + 1).trim()
                    verseCollector.textLa = latin
                    verseCollector.chapter = chapter
                    verseCollector.verse = verse
                    prevIdStr = idStr
                }
            } else if (it.trim().startsWith("~")) {
                val notes: Boolean = it.trim().startsWith("~")
                if (notes) {
                    var notes = it.trim().replace("~", "")
                    /*while (i < split.size && split[i + 1].startsWith("~")) {
                        notes += "\n${split[i + 1].replace("~", "")}"
                    }*/
                   /* while (iterator.hasNext()) {
                        val next = iterator.next().replace("~", "")
                        if (next.startsWith("~")) {
                            notes += "\n${next}"
                        } else break
                    }*/
                    verseCollector.notes = notes
                    if (verseCollector.chapter != 0 && verseCollector.verse != 0)
                        verses += Verse(verseCollector)
                    //verseCollector = VerseCollector()
                }
            }
        }

        //val iterator = split.iterator()

/*
        while (iterator.hasNext()) {
            val it = iterator.next()
            if (it.trim().startsWith("{")) {
                val endOfId = it.indexOf("}")
                val idStr = it.substring(0, endOfId + 1)
                if (idStr == prevIdStr) {
                    val english = it.substring(endOfId + 1).trim()
                    verseCollector.textEn = english
                    if (verseCollector.chapter != 0 && verseCollector.verse != 0)
                        verses += Verse(verseCollector)
                } else {
                    verseCollector = VerseCollector()
                    val (chapter, verse) = chapterVerse(idStr)
                    val latin = it.substring(endOfId + 1).trim()
                    verseCollector.textLa = latin
                    verseCollector.chapter = chapter
                    verseCollector.verse = verse
                    prevIdStr = idStr
                }
            } else if (it.trim().startsWith("~")) {
                val notes: Boolean = it.trim().startsWith("~")
                if (notes) {
                    var notes = it.trim().replace("~", "")
                    while (iterator.hasNext()) {
                        val next = iterator.next().replace("~", "")
                        if (next.startsWith("~")) {
                            notes += "\n${next}"
                        } else break
                    }
                    verseCollector.notes = notes
                    if (verseCollector.chapter != 0 && verseCollector.verse != 0)
                        verses += Verse(verseCollector)
                    //verseCollector = VerseCollector()
                }
            }
        }
*/
        verses.forEach { println(it) }
    }

    private fun connect(url: String): Document = Jsoup.connect(url).get()

}

fun scrape() = urls.forEach { Scraper().scrape(it) }

fun Document.bodyAsString() = this.select("body").toString()

fun chapterVerse(idString: String): Pair<Int, Int> {
    val numbers = idString.replace("{", "").replace("}", "").split(":")
    return Pair(numbers[0].trim().toInt(), numbers[1].trim().toInt())
}

private const val baseUrl = "http://www.sacredbible.org/studybible/"
private val books = setOf(
    "OT-01_Genesis"
)
private val urls = books.map { "${scrape.baseUrl}$it.htm" }
