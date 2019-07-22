package uk.ac.ceh.gateway.catalogue.eidc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class File {
    private final String filename, format, size, content;

    @Builder
    @JsonCreator
    private File(
        @JsonProperty("filename") String filename,
        @JsonProperty("format") String format,
        @JsonProperty("size") String size,
        @JsonProperty("content") String content
    ) {
        this.filename = nullToEmpty(filename);
        this.format = nullToEmpty(format);
        this.size = nullToEmpty(size);
        this.content = nullToEmpty(content);
    }
}