package scrape

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Book
import model.Chapter
import model.Verse
import model.VerseCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

/**
 * This class scrapes the Latin Vulgate and stores the data as JSON files.
 */
private class LatinVulgateEnglishStudyBible {

    suspend fun scrapeAndBuildJson(url: String, bible: MutableList<Book>) {

        // read page content as a String
        val doc = connect(url).bodyAsString()

        // Get only the useful bits (English text, Latin text, and notes - if any) as a List
        val usefulBits = doc.split("<br>")
            .map { it.trim() }
            .filter { it.startsWith("{") || it.startsWith("~") }
            .toMutableList()

        var prevIdStr = ""
        val verses = mutableSetOf<Verse>()
        var verseCollector = VerseCollector()
        var previousVerse = Verse(VerseCollector())

        var current = 0
        while (current < usefulBits.size) {
            val it = usefulBits[current]
            if (it.trim().startsWith("{")) { // extracting English text
                val endOfId = it.indexOf("}")
                val idStr = it.substring(0, endOfId + 1)
                if (idStr == prevIdStr) {
                    val english = it.substring(endOfId + 1).trim()
                    verseCollector.textEn = english
                    if (verseCollector.chapter != -1 && verseCollector.verse != -1) {
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
                    var notes = it.replace("~", "").trim()
                    while (current < usefulBits.size - 1 && usefulBits[current + 1].startsWith("~")) {
                        notes += "\n${usefulBits[current + 1].replace("~", "")}"
                        ++current
                    }

                    verseCollector.notes = notes
                    if (verseCollector.chapter != -1 && verseCollector.verse != -1) {
                        val verse = Verse(verseCollector)
                        verses += verse
                        if (previousVerse.chapter == verse.chapter && previousVerse.verse == verse.verse) {
                            verses -= previousVerse
                        }
                    }
                }
            }
            ++current
        }
        val list = verses.toList()
        val chapters = list.distinctBy { it.chapter }
        val result: List<Chapter> =
            chapters.map { it.chapter }.map { chapNum -> Chapter(chapNum, list.filter { it.chapter == chapNum }) }

        val format = Json { prettyPrint = true }
        val json = format.encodeToString(result)

        val folder = File("Generated-JSON")
        if (!folder.exists()) folder.mkdir()

        val subFolder = File("Generated-JSON/Latin-Vulgate-English-Translation-Study-Bible")
        if (!subFolder.exists()) subFolder.mkdir()

        val bookName = url.substring(url.lastIndexOf("/"), url.length - 4)
        val newFileName = "Generated-JSON/Latin-Vulgate-English-Translation-Study-Bible/$bookName.json"
        File(newFileName).createNewFile()
        File(newFileName).writeText(json)

        bible.add(
            Book(
                bookNumber = bookName.substring(4, 6).toInt(),
                book = bookName.substring(7),
                testament = bookName.substring(1, 3),
                chapters = result
            )
        )
        println("Scrapped $bookName successfully!")
    }

    private suspend fun connect(url: String): Document = withContext(Dispatchers.IO) { Jsoup.connect(url).get() }

}

suspend fun latinVulgateEnglishStudyBible() {
    val books: MutableList<Book> = mutableListOf()
    urls.forEach {
        LatinVulgateEnglishStudyBible().scrapeAndBuildJson(it, books)
    }

    val format = Json { prettyPrint = true }
    val json = format.encodeToString(books)

    val newFileName = "Generated-JSON/Latin-Vulgate-English-Translation-Study-Bible/bible.json"
    File(newFileName).createNewFile()
    File(newFileName).writeText(json)

    println("Parsing the Bible completed successfully!")
}

fun Document.bodyAsString() = this.select("body").toString()

fun chapterVerse(idString: String): Pair<Int, Int> {
    val numbers = idString.replace("{", "").replace("}", "").split(":")
    val chapter = if (numbers[0].trim()[0].isDigit()) numbers[0].trim().toInt() else 0
    return Pair(chapter, numbers[1].trim().toInt())
}

private const val baseUrl = "http://www.sacredbible.org/studybible/"
private val books = setOf(
    "OT-01_Genesis",
    "OT-02_Exodus",
    "OT-03_Leviticus",
    "OT-04_Numbers",
    "OT-05_Deuteronomy",
    "OT-06_Joshua",
    "OT-07_Judges",
    "OT-08_Ruth",
    "OT-09_1-Samuel",
    "OT-10_2-Samuel",
    "OT-11_1-Kings",
    "OT-12_2-Kings",
    "OT-13_1-Chronicles",
    "OT-14_2-Chronicles",
    "OT-15_Ezra",
    "OT-16_Nehemiah",
    "OT-17_Tobit",
    "OT-18_Judith",
    "OT-19_Esther",
    "OT-20_Job",
    "OT-21_Psalms",
    "OT-22_Proverbs",
    "OT-23_Ecclesiastes",
    "OT-24_Song",
    "OT-25_Wisdom",
    "OT-26_Sirach",
    "OT-27_Isaiah",
    "OT-28_Jeremiah",
    "OT-29_Lamentations",
    "OT-30_Baruch",
    "OT-31_Ezekiel",
    "OT-32_Daniel",
    "OT-33_Hosea",
    "OT-34_Joel",
    "OT-35_Amos",
    "OT-36_Obadiah",
    "OT-37_Jonah",
    "OT-38_Micah",
    "OT-39_Nahum",
    "OT-40_Habakkuk",
    "OT-41_Zephaniah",
    "OT-42_Haggai",
    "OT-43_Zechariah",
    "OT-44_Malachi",
    "OT-45_1-Maccabees",
    "OT-46_2-Maccabees",
    "NT-01_Matthew",   //   <------ The New Testament begins here!
    "NT-02_Mark",
    "NT-03_Luke",
    "NT-04_John",
    "NT-05_Acts",
    "NT-06_Romans",
    "NT-07_1-Corinthians",
    "NT-08_2-Corinthians",
    "NT-09_Galatians",
    "NT-10_Ephesians",
    "NT-11_Philippians",
    "NT-12_Colossians",
    "NT-13_1-Thessalonians",
    "NT-14_2-Thessalonians",
    "NT-15_1-Timothy",
    "NT-16_2-Timothy",
    "NT-17_Titus",
    "NT-18_Philemon",
    "NT-19_Hebrews",
    "NT-20_James",
    "NT-21_1-Peter",
    "NT-22_2-Peter",
    "NT-23_1-John",
    "NT-24_2-John",
    "NT-25_3-John",
    "NT-26_Jude",
    "NT-27_Revelation",
)
private val urls = books.map { "$baseUrl$it.htm" }
