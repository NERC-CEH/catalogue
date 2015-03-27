package uk.ac.ceh.gateway.catalogue.gemini;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class LocalDateFactoryTest {
    
    @Test
    public void parseIsoLocalDate() {
        //Given
        LocalDate expected = LocalDate.of(2007, Month.DECEMBER, 1);
        
        //When
        LocalDate actual = LocalDateFactory.parse("2007-12-01");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void parseEnglishDate() {
        //Given
        LocalDate expected = LocalDate.of(2007, Month.DECEMBER, 1);
        
        //When
        LocalDate actual = LocalDateFactory.parse("01-12-2007");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void parseIsoDateTime() {
        //Given
        LocalDate expected = LocalDate.of(2007, Month.DECEMBER, 1);
        
        //When
        LocalDate actual = LocalDateFactory.parse("2007-12-01T01:34:56Z");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void parseAllNumberDate() {
        //Given
        LocalDate expected = LocalDate.of(2007, Month.DECEMBER, 1);
        
        //When
        LocalDate actual = LocalDateFactory.parse("01122007");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void unrecognisedDate() {
        //Given
        
        //When
        LocalDate actual = LocalDateFactory.parse("10/12/1943");
        
        //Then
        assertThat("'actual' LocalDate should be null", actual, nullValue(LocalDate.class));
    }
    
    @Test
    public void parseIsoDateTimeForDateTime() {
        //Given
        LocalDateTime expected = LocalDateTime.of(2007, Month.DECEMBER, 1, 01, 34, 56);
        
        //When
        LocalDateTime actual = LocalDateFactory.parseForDateTime("2007-12-01T01:34:56");
        
        //Then
        assertThat("LocalDateTime 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void parseIsoDateForDateTime() {
        //Given
        LocalDateTime expected = LocalDateTime.of(2007, Month.DECEMBER, 1, 0, 0, 1);
        
        //When
        LocalDateTime actual = LocalDateFactory.parseForDateTime("2007-12-01");
        
        //Then
        assertThat("LocalDateTime 'actual' should equal 'expected'", actual, equalTo(expected));
    }

}