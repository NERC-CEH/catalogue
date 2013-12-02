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
    @XmlJavaTypeAdapter(type=LocalDate.class, value=uk.ac.ceh.ukeof.model.simple.adapters.LocalDateAdapter.class)
})
@XmlAccessorType(XmlAccessType.FIELD)
package uk.ac.ceh.ukeof.model.simple;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
