@XmlSchema(
    elementFormDefault = XmlNsForm.QUALIFIED,
    attributeFormDefault = XmlNsForm.QUALIFIED,
    namespace = "http://www.ukeof.org.uk/schema/1",
    xmlns = {
        @XmlNs(prefix = "xlink", namespaceURI = "http://www.w3.org/1999/xlink"),
        @XmlNs(prefix = "ukeof", namespaceURI = "http://www.ukeof.org.uk/schema/1")
    }
)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(type=DateTime.class, value=uk.ac.ceh.ukeof.model.simple.adapters.DateTimeAdapter.class),
    @XmlJavaTypeAdapter(type=LocalDate.class, value=uk.ac.ceh.ukeof.model.simple.adapters.LocalDateAdapter.class),
    @XmlJavaTypeAdapter(type=UUID.class, value=uk.ac.ceh.ukeof.model.simple.adapters.UuidAdapter.class)
})
@XmlAccessorType(XmlAccessType.FIELD)
package uk.ac.ceh.ukeof.model.simple;

import java.util.UUID;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import org.joda.time.*;