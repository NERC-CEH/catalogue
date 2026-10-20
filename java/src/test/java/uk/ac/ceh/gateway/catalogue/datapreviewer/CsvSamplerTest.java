package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
}
