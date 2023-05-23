<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="serviceOperation<%= data.index %>OperationName">Operation Name</label>
  </div>
  <div class="col-sm-4 col-lg-4">
    <select data-name='operationName' class="editor-input operationName" id="serviceOperation<%= data.index %>OperationName" <%= data.disabled%>>
      <option value="">- Select Operation Name -</option>
      <option>Cancel</option>
      <option>Confirm</option>
      <option>DescribeCoverage</option>
      <option>DescribeFeatureType</option>
      <option>DescribeLayer</option>
      <option>DescribeProcess</option>
      <option>DescribeRecord</option>
      <option>DescribeResultAccess</option>
      <option>DescribeSensor</option>
      <option>DescribeTasking</option>
      <option>Execute</option>
      <option>GetCapabilities</option>
      <option>GetCoverage</option>
      <option>GetDomain</option>
      <option>GetFeasibility</option>
      <option>GetFeature</option>
      <option>GetFeatureInfo</option>
      <option>GetFeatureWithLock</option>
      <option>GetGMLObject</option>
      <option>GetLegendGraphic</option>
      <option>GetMap</option>
      <option>GetObservation</option>
      <option>GetPropertyValue</option>
      <option>GetRecordById</option>
      <option>GetRecords</option>
      <option>GetStatus</option>
      <option>GetTask</option>
      <option>GetTile</option>
      <option>Harvest</option>
      <option>LockFeature</option>
      <option>Reserve</option>
      <option>Submit</option>
      <option>Transaction</option>
      <option>Update</option>
    </select>
  </div>
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="serviceOperation<%= data.index %>Platform">Platform</label>
  </div>
  <div class="col-sm-4 col-lg-4">
    <select data-name='platform' class="editor-input platform" id="serviceOperation<%= data.index %>Platform" <%= data.disabled%>>
      <option value="">- Select Platform -</option>
      <option>COM</option>
      <option>Corba</option>
      <option>HTTPGet</option>
      <option>HTTPPost</option>
      <option>HTTPSoap</option>
      <option>Java</option>
      <option>SQL</option>
      <option>WebService</option>
      <option>XML</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="serviceOperation<%= data.index %>Url">URL</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name='url' class="editor-input" id="serviceOperation<%= data.index %>Url" value="<%= data.url %>" <%= data.disabled%>>
  </div>
</div>