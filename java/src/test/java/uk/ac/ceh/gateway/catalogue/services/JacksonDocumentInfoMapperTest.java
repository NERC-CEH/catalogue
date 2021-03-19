package uk.ac.ceh.gateway.catalogue.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class JacksonDocumentInfoMapperTest {
    @Mock ObjectMapper jacksonMapper;
    
    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
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
        assertEquals("Expeceted the output from the mapper to be read", "jsonString", readInfo);
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
