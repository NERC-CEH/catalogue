import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label class="control-label" for="coupledResource<%= data.index %>OperationName">Operation Name</label>
    </div>
    <div class="col-sm-4 col-lg-4">
        <select data-name='operationName' class="editor-input" id="coupledResource<%= data.index %>OperationName" <%= data.disabled%>>
            <option value="">- Select Operation -</option>
            <option>COM</option>
            <option>Corba</option>
            <option>GetMap</option>
            <option>HTTPGet</option>
            <option>HTTPPost</option>
            <option>HTTPSoap</option>
            <option>Java</option>
            <option>SQL</option>
            <option>WebService</option>
            <option>XML</option>
        </select>
    </div>
    <div class="col-sm-2 col-lg-2">
        <label class="control-label" for="coupledResource<%= data.index %>LayerName">Layer Name</label>
    </div>
    <div class="col-sm-4 col-lg-4">
        <input data-name='layerName' class="editor-input" id="coupledResource<%= data.index %>LayerName" value="<%= data.layerName %>" <%= data.disabled%>>
    </div>
</div>
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label class="control-label" for="coupledResource<%= data.index %>Identifier">Identifier</label>
    </div>
    <div class="col-sm-10 col-lg-10">
        <input data-name='identifier' class="editor-input" id="coupledResource<%= data.index %>Identifier" value="<%= data.identifier %>" <%= data.disabled%>>
    </div>
</div>
`)
