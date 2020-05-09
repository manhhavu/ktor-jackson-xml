# XML support for Ktor's [content negotiation](https://ktor.io/servers/features/content-negotiation.html)

Even XML format is probably less used then JSON nowadays, it does still exist. If you need to accept/return XML data 
format in your Ktor API, this library is for you. It uses the excellent [Jackson XML](https://github.com/FasterXML/jackson-dataformat-xml)
behind the scene. 

It has essentially the same implementation as of its [JSON counterpart](https://ktor.io/servers/features/content-negotiation/jackson.html), adapted to XML.

## Basic Usage

To install the feature, you can register the feature as such:

```kotlin
register(ContentType.Application.Xml, JacksonXmlConverter())
```  

or if you want to customize

```kotlin
install(ContentNegotiation) {
    xml {
        // You have access to XmlMapper here
        // XmlMapper inherits from Jackson's ObjectMapper, so you can configure the same 
        // parameters as ObjectMapper
        configure(SerializationFeature.INDENT_OUTPUT, true)
        registerModule(JavaTimeModule())
    }
}

```

## Example

* Examples can be found in the [project's tests](https://github.com/manhhavu/ktor-jackson-xml/blob/master/src/test/kotlin/com/manhhavu/jackson/xml/JacksonXmlConverterTest.kt).