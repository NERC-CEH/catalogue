<label for="resourceType" class="control-label required">
  Resource Type
  <a data-toggle="collapse" title="Click for help" href="#help-resourceType" data-parent="#editor"><i class="glyphicon glyphicon-question-sign"></i></a>
</label>
<select id="resourceType" class="form-control">
  <option value="" selected>Select Resource Type</option>
  <optgroup label="GEMINI">
    <option value="dataset">Dataset</option>
    <option value="series">Series</option>
    <option value="service">Service</option>
  </optgroup>
  <optgroup label="Other">
    <option value="aggregate">Aggregate</option>
    <option value="application">Application</option>
    <option value="attribute">Attribute</option>
    <option value="attributeType">Attribute type</option>
    <option value="collection">Collection</option>
    <option value="collectionHardware">Collection hardware</option>
    <option value="collectionSession">Collection session</option>
    <option value="coverage">Coverage</option>
    <option value="dimensionGroup">Dimension group</option>
    <option value="document">Document</option>
    <option value="feature">Feature</option>
    <option value="featureType">Feature type</option>
    <option value="fieldSession">Field session</option>
    <option value="initiative">Initiative</option>
    <option value="metadata">Metadata</option>
    <option value="model">Model</option>
    <option value="nonGeographicDataset">Non-geographic dataset</option>
    <option value="product">Product</option>
    <option value="propertyType">Property type</option>
    <option value="repository">Repository</option>
    <option value="sample">Sample</option>
    <option value="software">Software</option>
    <option value="tile">Tile</option>
  </optgroup>
</select>
<p id="help-resourceType" class="help-block hidden-print collapse">
  Resource Type help
</p>