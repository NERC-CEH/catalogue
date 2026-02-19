import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="metadataStandard<%= data.index %>Title">Title</label>
    </div>
    <div class="col-sm-10">
        <input data-name="title" id="metadataStandard<%= data.index %>Title" class="editor-input" value="<%= data.title %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="metadataStandard<%= data.index %>Edition">Edition</label>
    </div>
    <div class="col-sm-10">
        <input data-name="edition" id="metadataStandard<%= data.index %>Edition" class="editor-input" value="<%= data.edition %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="metadataStandard<%= data.index %>Date">Date</label>
    </div>
    <div class="col-sm-10">
        <input data-name="date" type="date" id="metadataStandard<%= data.index %>Date" class="editor-input" value="<%= data.date %>" autocomplete="off">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="metadataStandard<%= data.index %>OnlineLink">OnlineLink</label>
    </div>
    <div class="col-sm-10">
        <input data-name="onlineLink" id="metadataStandard<%= data.index %>OnlineLink" class="editor-input" value="<%= data.onlineLink %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="metadataStandard<%= data.index %>Conformity">Conformity</label>
    </div>
    <div class="col-sm-10">
        <select data-name="conformity" id="metadataStandard<%= data.index %>Conformity" >
            <option value="" selected >- Select -</option>
            <option value="conformant">Conformant</option>
            <option value="nonconformant">Not conformant</option>
            <option value="unknown">Unknown</option>
        </select>
    </div>
</div>
`)
