package uk.ac.ceh.gateway.catalogue.datapreviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NetcdfSummariserTest {

    @TempDir
    Path tmp;

    private static Path fixture(String name) throws URISyntaxException {
        return Path.of(NetcdfSummariserTest.class.getResource("/datapreviewer/" + name).toURI());
    }

    private static NetcdfSummary summarise(Path file) throws IOException {
        return NetcdfSummariser.summarise(file, () -> false).orElseThrow();
    }

    @Test
    void summarisesNetcdf4() throws Exception {
        NetcdfSummary summary = summarise(fixture("climate4.nc"));

        assertThat(summary.format(), is("NetCDF-4 (HDF5)"));
        assertThat(summary.attributes().stream().map(NetcdfSummary.Attribute::name).toList(),
            contains("title", "institution", "Conventions", "source"));
        assertThat(summary.attributes(), hasItem(new NetcdfSummary.Attribute("title", "Daily air temperature, test grid")));
        assertThat(summary.attributes(), hasItem(new NetcdfSummary.Attribute("Conventions", "CF-1.8")));
        assertThat(summary.attributesTruncated(), is(false));
        // HDF5 keeps dimensions in its own order, not the order they were created in
        assertThat(summary.dimensions(), containsInAnyOrder(
            new NetcdfSummary.Dimension("time", 3, true),
            new NetcdfSummary.Dimension("lat", 2, false),
            new NetcdfSummary.Dimension("lon", 3, false)));
        assertThat(summary.variables(), hasItem(new NetcdfSummary.Variable(
            "tas", "Near-surface air temperature", "air_temperature", "K", "time lat lon", "float", false)));
        assertThat(summary.variables(), hasItem(new NetcdfSummary.Variable(
            "lat", null, "latitude", "degrees_north", "lat", "float", true)));
    }

    @Test
    void decodesTimeCoverageFromCfUnits() throws Exception {
        NetcdfSummary.TimeCoverage time = summarise(fixture("climate4.nc")).time();

        assertThat(time.variable(), is("time"));
        assertThat(time.start(), is("2000-01-01T00:00:00Z"));
        assertThat(time.end(), is("2000-12-31T00:00:00Z"));
        assertThat(time.calendar(), is("standard"));
    }

    @Test
    void givesGeographicExtent() throws Exception {
        assertThat(summarise(fixture("climate4.nc")).extents(), contains(
            new NetcdfSummary.Extent("lat", "latitude", "degrees_north", 50.5, 51.5),
            new NetcdfSummary.Extent("lon", "longitude", "degrees_east", -3.0, -1.0)));
    }

    @Test
    void summarisesNetcdf3WithProjectedCoordinatesAnd360DayCalendar() throws Exception {
        NetcdfSummary summary = summarise(fixture("rain3.nc"));

        assertThat(summary.format(), is("NetCDF-3 classic"));
        assertThat(summary.time().start(), is("1990-01-01T00:00:00Z"));
        // 720 hours is 30 days, a whole month in a 360-day calendar
        assertThat(summary.time().end(), is("1990-02-01T00:00:00Z"));
        assertThat(summary.time().calendar(), is("360_day"));
        assertThat(summary.extents(), contains(
            new NetcdfSummary.Extent("x", "x", "m", 200000, 201000),
            new NetcdfSummary.Extent("y", "y", "m", 100000, 101000)));
    }

    @Test
    void fileThatIsNotNetcdfIsEmpty() throws Exception {
        Path csv = Files.writeString(tmp.resolve("fake.nc"), "site,count\n");

        assertThat(NetcdfSummariser.summarise(csv, () -> false), is(Optional.empty()));
    }

    @Test
    void ncmlIsNeverParsed() throws Exception {
        // NcML can point netcdf-java at other files and URLs; without a NetCDF signature it is refused.
        Path ncml = Files.writeString(tmp.resolve("trap.nc"),
            "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\" location=\"/etc/passwd\"/>");

        assertThat(NetcdfSummariser.summarise(ncml, () -> false), is(Optional.empty()));
    }

    @Test
    void damagedFileWithASignatureIsEmpty() throws Exception {
        Path broken = tmp.resolve("broken.nc");
        byte[] head = Files.readAllBytes(fixture("climate4.nc"));
        Files.write(broken, java.util.Arrays.copyOf(head, 64));

        assertThat(NetcdfSummariser.summarise(broken, () -> false), is(Optional.empty()));
    }

    @Test
    void cancelledParseIsEmpty() throws Exception {
        assertThat(NetcdfSummariser.summarise(fixture("climate4.nc"), () -> true), is(Optional.empty()));
    }

    @Test
    void missingFileIsAReadFault() {
        assertThrows(NoSuchFileException.class, () -> NetcdfSummariser.summarise(tmp.resolve("gone.nc"), () -> false));
    }

    @Test
    void variableWithoutAttributesIsListedWithNulls() throws Exception {
        NetcdfSummary summary = summarise(fixture("rain3.nc"));

        assertThat(summary.variables(), hasItem(new NetcdfSummary.Variable(
            "rainfall", "Monthly rainfall", null, "mm", "time y x", "float", false)));
        assertThat(summary.variablesTruncated(), is(false));
    }

    @Test
    void coordinateInAnOversizedChunkIsNotRead() throws Exception {
        // Reading one value decompresses its whole chunk: here 32 MiB for a three-value time axis.
        NetcdfSummary summary = summarise(fixture("bigchunk4.nc"));

        assertThat(summary.time(), is(org.hamcrest.Matchers.nullValue()));
        assertThat(summary.extents(), contains(new NetcdfSummary.Extent("lat", "latitude", "degrees_north", 50, 51)));
        assertThat(summary.variables().stream().map(NetcdfSummary.Variable::name).toList(), hasItem("time"));
    }
}
