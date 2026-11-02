package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CsvSamplerTest {

    private static final int BIG = 262144;
    private final CsvSampler sampler = new CsvSampler();

    private CsvSample sample(byte[] bytes, int maxBytes, int maxRows) {
        return sampler.sample(new ByteArrayInputStream(bytes), maxBytes, maxRows);
    }

    private CsvSample sample(String text) {
        return sample(text.getBytes(StandardCharsets.UTF_8), BIG, 50);
    }

    @Test
    void readsHeaderAndRows() {
        CsvSample s = sample("a,b\r\n1,2\r\n3,4\r\n");
        assertThat(s.header(), contains("a", "b"));
        assertThat(s.rows(), contains(List.of("1", "2"), List.of("3", "4")));
        assertThat(s.moreRows(), is(false));
        assertThat(s.previewable(), is(true));
    }

    @Test
    void stopsAtMaxRowsAndFlagsMore() {
        StringBuilder sb = new StringBuilder("a,b\r\n");
        for (int i = 0; i < 60; i++) {
            sb.append(i).append(",x\r\n");
        }
        CsvSample s = sample(sb.toString().getBytes(StandardCharsets.UTF_8), BIG, 50);
        assertThat(s.rows(), hasSize(50));
        assertThat(s.moreRows(), is(true));
    }

    @Test
    void dropsRowCutOffByByteCap() {
        byte[] bytes = "a,b\r\n1,2\r\n3,4\r\n".getBytes(StandardCharsets.UTF_8);
        CsvSample s = sample(bytes, 12, 50); // ends inside "3,4"
        assertThat(s.rows(), contains(List.of("1", "2")));
        assertThat(s.moreRows(), is(true));
        assertThat(s.previewable(), is(true));
    }

    @Test
    void exactlyFillingTheCapIsNotMore() {
        byte[] bytes = "a,b\r\n1,2\r\n".getBytes(StandardCharsets.UTF_8);
        CsvSample s = sample(bytes, bytes.length, 50);
        assertThat(s.rows(), hasSize(1));
        assertThat(s.moreRows(), is(false));
    }

    @Test
    void dropsTrailingEmptyRows() {
        CsvSample s = sample("a,b\r\n1,2\r\n,\r\n,\r\n");
        assertThat(s.rows(), hasSize(1));
    }

    @Test
    void keepsQuotedStringsAndUnquotedDates() {
        CsvSample s = sample("\"SITE\",\"SDATE\"\r\n\"T01\",06-OCT-92\r\n");
        assertThat(s.header(), contains("SITE", "SDATE"));
        assertThat(s.rows().get(0), contains("T01", "06-OCT-92"));
    }

    @Test
    void keepsMissingValueMarkersVerbatim() {
        CsvSample s = sample("a,b,c,d,e\r\n\"\",\" \",NA,-999,NANH1\r\n");
        assertThat(s.rows().get(0), contains("", " ", "NA", "-999", "NANH1"));
    }

    @Test
    void stripsUtf8Bom() {
        CsvSample s = sample("﻿easting,northing\r\n1,2\r\n");
        assertThat(s.header().get(0), is("easting"));
    }

    @Test
    void decodesLatin1WhenNotUtf8() {
        byte[] head = "a\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] cell = {0x4D, (byte) 0xB5, 0x67};
        byte[] all = new byte[head.length + cell.length];
        System.arraycopy(head, 0, all, 0, head.length);
        System.arraycopy(cell, 0, all, head.length, cell.length);
        CsvSample s = sample(all, BIG, 50);
        assertThat(s.rows().get(0), contains("Mµg"));
    }

    @Test
    void padsRaggedRowsToHeaderWidth() {
        CsvSample s = sample("a,b,c\r\n1,2\r\n");
        assertThat(s.rows().get(0), contains("1", "2", ""));
    }

    @Test
    void binaryIsNotPreviewable() {
        assertThat(sample(new byte[] {'a', ',', 'b', '\n', 0x00, 1}, BIG, 50).previewable(), is(false));
    }

    @Test
    void singleHugeLineIsNotPreviewable() {
        byte[] bytes = new byte[300 * 1024];
        Arrays.fill(bytes, (byte) 'x');
        assertThat(sample(bytes, BIG, 50).previewable(), is(false));
    }

    @Test
    void emptyFileIsNotPreviewable() {
        assertThat(sample(new byte[0], BIG, 50).previewable(), is(false));
    }

    // Delimiter detection

    private static Optional<CsvSampler.Delimiter> detect(String text) {
        return CsvSampler.detect(text);
    }

    @Test
    void detectsComma() {
        assertThat(detect("a,b,c\n1,2,3\n4,5,6\n"), is(Optional.of(CsvSampler.Delimiter.COMMA)));
    }

    @Test
    void detectsTab() {
        assertThat(detect("site\tdate\tcount\nA1\t2020-01-01\t3\n"), is(Optional.of(CsvSampler.Delimiter.TAB)));
    }

    @Test
    void detectsSemicolonOverDecimalCommas() {
        assertThat(detect("site;value\nA1;1,5\nB2;2,25\n"), is(Optional.of(CsvSampler.Delimiter.of(';'))));
    }

    @Test
    void detectsPipe() {
        assertThat(detect("a|b\n1|2\n"), is(Optional.of(CsvSampler.Delimiter.PIPE)));
    }

    @Test
    void detectsRunsOfSpacesWhenNoCharacterSplits() {
        assertThat(detect("year   month  rain\n1990   1      85.2\n1990   2      60.1\n"),
            is(Optional.of(CsvSampler.Delimiter.WHITESPACE)));
    }

    @Test
    void ignoresDelimitersInsideQuotes() {
        assertThat(detect("\"name\",\"note\"\n\"A\",\"x; y; z\"\n\"B\",\"p; q; r\"\n"),
            is(Optional.of(CsvSampler.Delimiter.COMMA)));
    }

    @Test
    void quotedLineBreakDoesNotEndARecord() {
        assertThat(detect("a,b\n\"line one\nline two\",2\n3,4\n"), is(Optional.of(CsvSampler.Delimiter.COMMA)));
    }

    @Test
    void proseIsNotATable() {
        assertThat(detect("This folder holds the survey data, collected in 2019.\n"
            + "Files are named by site.\nSee the guide, the dictionary and the licence for details.\n"), is(Optional.empty()));
    }

    @Test
    void linesOfProseWithTheSameWordCountAreNotSpaceAligned() {
        assertThat(detect("Notes on the survey.\nSecond line of notes.\nThird and final line.\n"), is(Optional.empty()));
    }

    @Test
    void twoSpaceAlignedRecordsAreNotEnough() {
        assertThat(detect("year rain\n1990 85.2\n"), is(Optional.empty()));
    }

    @Test
    void singleColumnIsNotDetected() {
        assertThat(detect("count\n1\n2\n"), is(Optional.empty()));
    }

    @Test
    void oneRecordIsNotEnoughToDetect() {
        assertThat(detect("a,b,c\n"), is(Optional.empty()));
    }

    @Test
    void samplesTabDelimitedFile() {
        CsvSample s = sample("site\tcount\nA1\t3\nB2\t5\n");
        assertThat(s.header(), contains("site", "count"));
        assertThat(s.rows(), contains(List.of("A1", "3"), List.of("B2", "5")));
    }

    @Test
    void samplesSemicolonFileWithDecimalCommas() {
        CsvSample s = sample("site;value\nA1;1,5\n");
        assertThat(s.header(), contains("site", "value"));
        assertThat(s.rows().get(0), contains("A1", "1,5"));
    }

    @Test
    void splitsOnRunsOfSpaces() {
        CsvSampler.Text text = new CsvSampler.Text("  year   rain\n  1990   85.2\n\n  1991   60.1\n", false);

        CsvSample s = sampler.table(text, CsvSampler.Delimiter.WHITESPACE, 50);

        assertThat(s.header(), contains("year", "rain"));
        assertThat(s.rows(), contains(List.of("1990", "85.2"), List.of("1991", "60.1")));
    }

    @Test
    void readGivesWholeLinesAndSaysWhenCapped() {
        byte[] bytes = "a,b\n1,2\n3,4\n".getBytes(StandardCharsets.UTF_8);

        CsvSampler.Text text = sampler.read(new ByteArrayInputStream(bytes), 10).orElseThrow();

        assertThat(text.text(), is("a,b\n1,2\n"));
        assertThat(text.capped(), is(true));
    }

    @Test
    void readOfBinaryIsEmpty() {
        assertThat(sampler.read(new ByteArrayInputStream(new byte[] {'a', 0, 'b'}), BIG), is(Optional.empty()));
    }
}
