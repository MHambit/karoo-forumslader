package org.happycode.karoo.forumslader.adapters

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

class DataFieldHeadlineLengthTest : ShouldSpec({

    val maxSingleLineLength = 11

    fun readDataFieldHeadlines(resourcePath: String): Map<String, String> {
        val file = File(resourcePath)
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
        val nodes = document.getElementsByTagName("string")

        return buildMap {
            for (index in 0 until nodes.length) {
                val element = nodes.item(index) as? Element ?: continue
                val name = element.getAttribute("name")
                if (name.startsWith("datafield_") && !name.endsWith("_desc")) {
                    put(name, element.textContent.trim())
                }
            }
        }
    }

    listOf(
        "English" to "src/main/res/values/strings.xml",
        "German" to "src/main/res/values-de/strings.xml"
    ).forEach { (locale, path) ->
        should("keep all $locale datafield headlines within $maxSingleLineLength characters for single-line display") {
            // given
            val headlines = readDataFieldHeadlines(path)
            headlines.shouldNotBeEmpty()

            // when
            val overflowingHeadlines = headlines
                .filterValues { it.length > maxSingleLineLength }
                .map { (key, value) -> "$key='$value' (${value.length})" }

            // then
            overflowingHeadlines.shouldBeEmpty()
        }
    }
})
