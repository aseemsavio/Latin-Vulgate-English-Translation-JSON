package scrape

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Chapter
import model.Verse
import model.VerseCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

/**
 * This class scrapes the Latin Vulgate and stores the data as JSON files.
 */
private class Scraper {

    fun scrapeAndBuildJson(url: String) {

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
        var previousVerse = Verse(VerseCollector())

        var i = 0
        while (i < split.size) {
            val it = split[i]
            if (it.trim().startsWith("{")) { // extracting English text
                val endOfId = it.indexOf("}")
                val idStr = it.substring(0, endOfId + 1)
                if (idStr == prevIdStr) {
                    val english = it.substring(endOfId + 1).trim()
                    verseCollector.textEn = english
                    if (verseCollector.chapter != 0 && verseCollector.verse != 0) {
                        val verse = Verse(verseCollector)
                        verses += verse
                        previousVerse = verse
                    }
                } else { // extracting Latin text
                    verseCollector = VerseCollector()
                    val (chapter, verse) = chapterVerse(idStr)
                    val latin = it.substring(endOfId + 1).trim()
                    verseCollector.textLa = latin
                    verseCollector.chapter = chapter
                    verseCollector.verse = verse
                    prevIdStr = idStr
                }
            } else if (it.trim().startsWith("~")) { // extracting notes (if available)
                val notes: Boolean = it.trim().startsWith("~")
                if (notes) {
                    var notes = it.trim().replace("~", "")
                    while (i < split.size && split[i + 1].startsWith("~")) {
                        notes += "\n${split[i + 1].replace("~", "")}"
                        ++i
                    }

                    verseCollector.notes = notes
                    if (verseCollector.chapter != 0 && verseCollector.verse != 0) {
                        val verse = Verse(verseCollector)
                        verses += verse
                        if (previousVerse.chapter == verse.chapter && previousVerse.verse == verse.verse) {
                            verses -= previousVerse
                        }
                    }
                }
            }
            ++i
        }
        val list = verses.toList()
        val chapters = list.distinctBy { it.chapter }
        val result: List<Chapter> =
            chapters.map { it.chapter }.map { chapNum -> Chapter(chapNum, list.filter { it.chapter == chapNum }) }

        val format = Json { prettyPrint = true }
        val json = format.encodeToString(result)

        val folder = File("Generated-JSON")
        if (!folder.exists()) folder.mkdir()

        val newFileName = "Generated-JSON${url.substring(url.lastIndexOf("/"), url.length - 4)}.json"
        File(newFileName).createNewFile()
        File(newFileName).writeText(json)
    }

    private fun connect(url: String): Document = Jsoup.connect(url).get()

}

fun scrape() = urls.forEach { Scraper().scrapeAndBuildJson(it) }

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
