package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColourRampTest {
    @Test
    public void checkThatCanGenerateColourRamp() {
        //Given
        ColourRamp ramp = new ColourRamp(new Color(0xff,0xec,0x8b));
        ramp.addStep(1, new Color(0x8b,0x47,0x26));

        //When
        List<Color> collect = IntStream
                .range(0, 6)
                .mapToObj(i -> ramp.getColour(i, 5))
                .collect(Collectors.toList());

        //Then
        assertEquals(collect, Arrays.asList(
            new Color(0xff,0xec,0x8b),
            new Color(0xe7,0xcb,0x76),
            new Color(0xd0,0xaa,0x62),
            new Color(0xb9,0x89,0x4e),
            new Color(0xa2,0x68,0x3a),
            new Color(0x8b,0x47,0x26)
        ));
    }

    @Test
    public void checkThatCanGenerateMultiStopColourRamp() {
        //Given
        ColourRamp ramp = new ColourRamp(new Color(0xa2, 0xcd, 0x5a));
        ramp.addStep(1, new Color(0xff, 0xec, 0x8b));
        ramp.addStep(1, new Color(0x00, 0x00, 0x80));

        //When
        List<Color> collect = IntStream
                .range(0, 9)
                .mapToObj(i -> ramp.getColour(i, 8))
                .collect(Collectors.toList());

        //Then
        assertEquals(collect, Arrays.asList(
            new Color(0xA2, 0xCD, 0x5A),
            new Color(0xB9, 0xD4, 0x66),
            new Color(0xD0, 0xDC, 0x72),
            new Color(0xE7, 0xE4, 0x7E),
            new Color(0xFF, 0xEC, 0x8B),
            new Color(0xBF, 0xB1, 0x88),
            new Color(0x7F, 0x76, 0x85),
            new Color(0x3F, 0x3B, 0x82),
            new Color(0x00, 0x00, 0x80)
        ));
    }
}
