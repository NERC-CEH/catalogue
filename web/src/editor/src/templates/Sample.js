import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-lg-2">
        <label for="sample<%= data.index %>SampleID">Sample ID</label>
    </div>
    <div class="col-lg-10">
        <input autocomplete="off" aria-autocomplete="none"
               data-name="sampleID"
               class="editor-input"
               id="sample<%= data.index %>SampleID"
               value="<%= data.sampleID %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="sample<%= data.index %>SampleDescription">Description</label>
    </div>
    <div class="col-lg-10">
        <input autocomplete="off" aria-autocomplete="none"
               data-name="sampleDescription"
               class="editor-input"
               id="sample<%= data.index %>SampleDescription"
               value="<%= data.sampleDescription %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="sample<%= data.index %>SampleDate">Date</label>
    </div>
    <div class="col-lg-10">
        <input autocomplete="off" aria-autocomplete="none"
               data-name="sampleDate"
               class="editor-input"
               type="Date"
               id="sample<%= data.index %>SampleDate"
               value="<%= data.sampleDate %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label>Storage</label>
    </div>
    <div class="col-lg-10 sampleStorage-container"><!--insert template here --></div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label>Geometry</label>
    </div>
    <div class="col-lg-10 geometry-container"></div>
</div>
`)