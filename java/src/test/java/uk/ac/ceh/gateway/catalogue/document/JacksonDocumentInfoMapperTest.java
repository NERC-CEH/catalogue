package uk.ac.ceh.gateway.catalogue.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JacksonDocumentInfoMapperTest {
    @Mock ObjectMapper jacksonMapper;

    @Test
    public void checkCanWriteInfo() throws IOException {
        //Given
        String toWrite = "My data to write";
        OutputStream output = mock(OutputStream.class);
        JacksonDocumentInfoMapper<String> mapper = new JacksonDocumentInfoMapper<>(
                                                    jacksonMapper, String.class);

        //When
        mapper.writeInfo(toWrite, output);

        //Then
        verify(jacksonMapper).writeValue(output, toWrite);
    }

    @Test
    public void checkOutputStreamIsClosedAfterWrite() throws IOException {
        //Given
        String toWrite = "My data to write";
        OutputStream output = mock(OutputStream.class);
        JacksonDocumentInfoMapper<String> mapper = new JacksonDocumentInfoMapper<>(
                                                    jacksonMapper, String.class);
        //When
        mapper.writeInfo(toWrite, output);

        //Then
        verify(output).close();
    }

    @Test
    public void checkCanReadInfoFromInputStream() throws IOException {
        //Given
        InputStream in = mock(InputStream.class);
        JacksonDocumentInfoMapper<String> mapper = new JacksonDocumentInfoMapper<>(
                                                    jacksonMapper, String.class);

        when(jacksonMapper.readValue(in, String.class)).thenReturn("jsonString");

        //When
        String readInfo = mapper.readInfo(in);

        //Then
        assertEquals("jsonString", readInfo);
    }

    @Test
    public void checkInputStreamIsClosedAfterReading() throws IOException {
        //Given
        InputStream in = mock(InputStream.class);
        JacksonDocumentInfoMapper<String> mapper = new JacksonDocumentInfoMapper<>(
                                                    jacksonMapper, String.class);

        //When
        mapper.readInfo(in);

        //Then
        verify(in).close();
    }
}
