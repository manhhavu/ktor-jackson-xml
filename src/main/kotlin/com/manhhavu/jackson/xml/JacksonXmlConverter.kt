package com.manhhavu.jackson.xml

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream

// We need an explicit name for xml tag value
// due to this bug https://github.com/FasterXML/jackson-module-kotlin/issues/138
const val XML_TEXT_ELEMENT_NAME: String = "innerText"

/**
 *    install(ContentNegotiation) {
 *       register(ContentType.Application.xml, JacksonXmlConverter())
 *    }
 *
 *    to be able to modify the xmlMapper (eg. using specific modules and/or serializers and/or
 *    configuration options, you could use the following (as seen in the ktor-samples):
 *
 *    install(ContentNegotiation) {
 *        xml {
 *            configure(SerializationFeature.INDENT_OUTPUT, true)
 *            registerModule(JavaTimeModule())
 *        }
 *    }
 */
class JacksonXmlConverter(private val xmlMapper: XmlMapper = xmlMapper()) : ContentConverter {
    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val type = request.type
        val value = request.value as? ByteReadChannel ?: return null
        val reader = value.toInputStream().reader(context.call.request.contentCharset() ?: Charsets.UTF_8)
        return xmlMapper.readValue(reader, type.javaObjectType)
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return TextContent(
            xmlMapper.writeValueAsString(value),
            contentType.withCharset(context.call.suitableCharset())
        )
    }
}

/**
 * Register Jackson XML converter into [ContentNegotiation] feature
 */
fun ContentNegotiation.Configuration.xml(
    contentType: ContentType = ContentType.Application.Xml,
    xmlTextElementName: String = XML_TEXT_ELEMENT_NAME,
    block: XmlMapper.() -> Unit = {}
) {
    val mapper = xmlMapper(xmlTextElementName)
    mapper.apply(block)
    val converter = JacksonXmlConverter(mapper)
    register(contentType, converter)
}

private fun xmlMapper(xmlTextElementName: String = XML_TEXT_ELEMENT_NAME): XmlMapper {
    val module = JacksonXmlModule().apply {
        setXMLTextElementName(xmlTextElementName)
    }
    return XmlMapper(module).apply {
        registerModule(KotlinModule())
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })
    }
}