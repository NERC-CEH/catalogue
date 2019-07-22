package uk.ac.ceh.gateway.catalogue.eidc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class DataFile {
    private final String filename, format, size;

    @Builder
    @JsonCreator
    private DataFile(
        @JsonProperty("filename") String filename,
        @JsonProperty("format") String format,
        @JsonProperty("size") String size
    ) {
        this.filename = nullToEmpty(filename);
        this.format = nullToEmpty(format);
        this.size = nullToEmpty(size);
    }
}