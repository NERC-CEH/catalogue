package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.time.LocalDate;
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
        LocalDateFactory factory = new LocalDateFactory();
        
        //When
        LocalDate actual = factory.parse("2007-12-01");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void parseEnglishDate() {
        //Given
        LocalDate expected = LocalDate.of(2007, Month.DECEMBER, 1);
        LocalDateFactory factory = new LocalDateFactory();
        
        //When
        LocalDate actual = factory.parse("01-12-2007");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void parseIsoDateTime() {
        //Given
        LocalDate expected = LocalDate.of(2007, Month.DECEMBER, 1);
        LocalDateFactory factory = new LocalDateFactory();
        
        //When
        LocalDate actual = factory.parse("01-12-2007T01:34:56Z");
        
        //Then
        assertThat("LocalDate 'actual' should equal 'expected'", actual, equalTo(expected));
    }
    
    @Test
    public void unrecognisedDate() {
        //Given
        LocalDateFactory factory = new LocalDateFactory();
        
        //When
        LocalDate actual = factory.parse("10121943");
        
        //Then
        assertThat("'actual' LocalDate should be null", actual, nullValue(LocalDate.class));
    }

}