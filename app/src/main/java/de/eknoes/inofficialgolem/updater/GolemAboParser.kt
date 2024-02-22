package de.eknoes.inofficialgolem.updater

import de.eknoes.inofficialgolem.entities.Article
import org.xmlpull.v1.XmlPullParser


private val ns: String? = null

class GolemAboParser : GolemRSSParser() {

    override fun parse(stream: String): List<Article> {
        return readFeed(getParser(stream))
    }

    private fun readFeed(parser: XmlPullParser): List<Article> {
        val entries = mutableListOf<Article>()

        parser.require(XmlPullParser.START_TAG, ns, "feed")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "entry") {
                entries.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }

        return entries
    }

    private fun readEntry(parser: XmlPullParser): Article {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var title: String? = null
        var link: String? = null
        var pubDate: String? = null
        var fulltext: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "id" -> link = readLink(parser)
                "published" -> pubDate = readPublished(parser)
                "summary" -> fulltext = readFulltext(parser)
                else -> skip(parser)
            }
        }

        val item = Article()
        item.title = title;
        //TODO
//        item.date = parseDate(pubDate,"yyyy-MM-dd'T'HH:mm:ssz")
        item.fullText = fulltext;
        item.isOffline = true;
        item.articleUrl = link
        return item
    }

    private fun readFulltext(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "summary")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "summary")
        return content
    }
/*
    private val fulltextPattern = Pattern.compile("<script.*>.*?</script>", Pattern.DOTALL)

    private fun cleanFulltext(fulltext: String?): String {
        var fulltext = fulltext
        val matcher = fulltextPattern.matcher(fulltext!!)
        if (matcher.find()) {
            fulltext = fulltext.substring(0, matcher.start(0)) + fulltext.substring(matcher.end(0), fulltext.length)
            fulltext = cleanFulltext(fulltext)
        }
        return fulltext
    }
*/

    override fun readLink(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "id")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "id")
        return content
    }

    private fun readPublished(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "published")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "published")
        return content
    }
}