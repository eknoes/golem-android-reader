package de.eknoes.inofficialgolem.updater

import android.os.Build
import android.text.Html
import android.util.Xml
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

    open fun parse(stream: String): List<GolemItem> {
        return readFeed(getParser(stream))
    }

    private fun readFeed(parser: XmlPullParser): List<GolemItem> {
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

    private fun readChannel(parser: XmlPullParser): List<GolemItem> {
        val entries = mutableListOf<GolemItem>()

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

    private fun readItem(parser: XmlPullParser): GolemItem {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var title: String? = null
        var link: String? = null
        var commentUrl: String? = null
        var commentNumber: String? = null
        var pubDate: String? = null
        var desc: String? = null
        var content: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "link" -> link = readLink(parser)
                "description" -> desc = readDesc(parser)
                "comments" -> commentUrl = readCommentUrl(parser)
                "slash:comments" -> commentNumber = readCommentNumber(parser)
                "pubDate" -> pubDate = readPubDate(parser)
                "content:encoded" -> content = readContent(parser)
                else -> skip(parser)
            }
        }

        val item = GolemItem()
        item.setProp(GolemItem.ItemProperties.TITLE, decodeHTML(title))
        item.setProp(GolemItem.ItemProperties.COMMENT_URL, commentUrl)
        item.setProp(GolemItem.ItemProperties.COMMENT_NR, commentNumber)
        item.setProp(GolemItem.ItemProperties.TEASER, decodeHTML(desc))

        //Extract IMG Url from content
        val matcher = urlPattern.matcher(content)
        if(matcher.find())
            item.setProp(GolemItem.ItemProperties.IMG_URL, content?.substring(matcher.start(0), matcher.end(0)))
        item.setProp(GolemItem.ItemProperties.DATE, parseDate(pubDate,"EEE, d MMM yyyy HH:mm:ss Z"))
        item.url = link
        return item
    }

    internal fun parseDate(datestring: String?, fmtstring: String): String? {
        // Thu, 07 Feb 2019 19:16:00 +0100
        val df = SimpleDateFormat(fmtstring, Locale.ENGLISH)
        try {
            val result = df.parse(datestring)
            return result.time.toString()
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