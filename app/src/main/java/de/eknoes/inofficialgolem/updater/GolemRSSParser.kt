package de.eknoes.inofficialgolem.updater

import android.os.Build
import android.text.Html
import android.util.Xml
import de.eknoes.inofficialgolem.entities.Article
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern



private val ns: String? = null

open class GolemRSSParser {

    internal fun getParser(stream: String) : XmlPullParser {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(stream))
        parser.nextTag()
        return parser
    }

    open fun parse(stream: String): List<Article> {
        return readFeed(getParser(stream))
    }

    private fun readFeed(parser: XmlPullParser): List<Article> {
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "channel") {
                return readChannel(parser)
            } else {
                skip(parser)
            }
        }

        return emptyList()
    }

    private fun readChannel(parser: XmlPullParser): List<Article> {
        val entries = mutableListOf<Article>()

        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "item") {
                entries.add(readItem(parser))
            } else {
                skip(parser)
            }
        }

        return entries
    }

    private val urlPattern = Pattern.compile(
            "((https):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)

    private fun readItem(parser: XmlPullParser): Article {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var title: String? = null
        var link: String? = null
        var commentUrl: String? = null
        var commentNumber: String? = null
        var pubDate: String? = null
        var desc: String? = null
        var content: String? = null
        var guid: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "link" -> link = readLink(parser)
                "description" -> desc = readDesc(parser)
                "comments" -> commentUrl = readCommentUrl(parser)
                "guid" -> guid = readGuid(parser)
                "content:encoded" -> content = readContent(parser)
                "slash:comments" -> commentNumber = readCommentNumber(parser)
                "pubDate" -> pubDate = readPubDate(parser)
                else -> skip(parser)
            }
        }

        val article = Article()
        if (guid != null) {
            article.id = guid.toInt()
        }
        article.title = decodeHTML(title)
        article.commentUrl = commentUrl
        article.commentNr = commentNumber
        article.teaser = decodeHTML(desc)

        //Extract IMG Url from content
        if (content != null) {
            val matcher = urlPattern.matcher(content)
            if(matcher.find())
                article.imgUrl = content.substring(matcher.start(0), matcher.end(0))
        }
        article.date = parseDate(pubDate,"EEE, d MMM yyyy HH:mm:ss Z")
        article.articleUrl = link
        return article
    }

    internal fun parseDate(datestring: String?, fmtstring: String): String? {
        if (datestring == null) {
            return "0"
        }

        val df = SimpleDateFormat(fmtstring, Locale.ENGLISH)
        try {
            val result = df.parse(datestring)
            return result?.time.toString()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return "0"
    }

    private fun readContent(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "content:encoded")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "content:encoded")
        return content
    }

    private fun readCommentUrl(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "comments")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "comments")
        return content
    }

    private fun readCommentNumber(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "slash:comments")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "slash:comments")
        return content
    }

    private fun readDesc(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "description")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "description")

        return cleanTeaser(content)
    }

    private val teaserPattern = Pattern.compile("\\(<a href=\".*\\) <img src=\".*/>")
    private fun cleanTeaser(teaser: String?): String {
        val matcher = teaserPattern.matcher(teaser!!)
        if (matcher.find())
            return teaser.substring(0, matcher.start(0))
        return teaser
    }


    private fun readPubDate(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "pubDate")
        return content
    }


    internal open fun readLink(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "link")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "link")
        return content
    }

    internal fun readTitle(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return content
    }

    internal fun readGuid(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, ns, "guid")
        val content = readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"guid")
        return content.split("/").get(4).replace(".html","")
    }


    internal fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }

        return result
    }

    private fun decodeHTML(str: String?): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(str).toString()
        }
    }

    internal fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}