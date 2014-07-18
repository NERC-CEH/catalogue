package uk.ac.ceh.gateway.catalogue.gemini.elements;

import java.time.LocalDate;
import java.time.Month;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TimePeriodTest {
    
    @Test
    public void parseIsoLocalDate() {
        //Given
        LocalDate begin = LocalDate.of(2007, Month.DECEMBER, 1);
        LocalDate end = LocalDate.of(2008, Month.FEBRUARY, 15);
        
        //When
        TimePeriod actual = new TimePeriod("2007-12-01", "2008-02-15");
        
        //Then
        assertThat("TimePeriod 'begin' should equal '2007-12-01", actual.getBegin(), equalTo(begin));
        assertThat("TimePeriod 'end' should equal '2008-02-15", actual.getEnd(), equalTo(end));
    }
    
    @Test
    public void parseEnglishDate() {
        //Given
        LocalDate begin = LocalDate.of(2007, Month.DECEMBER, 1);
        LocalDate end = LocalDate.of(2008, Month.FEBRUARY, 15);
        
        //When
        TimePeriod actual = new TimePeriod("01-12-2007", "15-02-2008");
        
        //Then
        assertThat("TimePeriod 'begin' should equal '2007-12-01", actual.getBegin(), equalTo(begin));
        assertThat("TimePeriod 'end' should equal '2008-02-15", actual.getEnd(), equalTo(end));
    }
    
    @Test
    public void parseIsoDateTime() {
        //Given
        LocalDate begin = LocalDate.of(2007, Month.DECEMBER, 1);
        LocalDate end = LocalDate.of(2008, Month.FEBRUARY, 15);
        
        //When
        TimePeriod actual = new TimePeriod("01-12-2007T01:34:56Z", "15-02-2008T13:22:08Z");
        
        //Then
        assertThat("TimePeriod 'begin' should equal '2007-12-01", actual.getBegin(), equalTo(begin));
        assertThat("TimePeriod 'end' should equal '2008-02-15", actual.getEnd(), equalTo(end));
    }
    
    @Test
    public void unrecognisedDate() {
        //Given
        //When
        TimePeriod actual = new TimePeriod("10121943", "15-02-2008T00:00:00Z");
        
        //Then
        assertThat("Begin date should be null", actual.getBegin(), nullValue(LocalDate.class));
    }

}