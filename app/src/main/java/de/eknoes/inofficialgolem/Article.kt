package de.eknoes.inofficialgolem

import de.eknoes.inofficialgolem.updater.GolemItem
import java.util.Date
import java.util.Locale

/**
 * Created by soenke on 10.04.16.
 */
internal class Article : GolemItem() {
    var title: String? = null
    var subheadline: String? = null
        get() = if (field == null) {
            null
        } else field!!.uppercase(Locale.ROOT)
    var teaser: String? = null
    var isOffline = false
    var fulltext: String? = null
    private var date = Date()
    var imgUrl: String? = null
    var commentUrl: String? = null
    var commentNr: String? = null
    var alreadyRead: Boolean? = false

    fun getDate(): Date {
        return date
    }

    fun setDate(date: Long) {
        this.date.time = date
    }
}
