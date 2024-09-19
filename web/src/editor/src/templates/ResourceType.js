import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input" id="input-resourceType">
    <option value="" selected>- Select Resource Type -</option>
    <optgroup label="Commonly used">
      <option class="option-eidc option-ukceh option-inms option-assist option-elter" value="dataset">Dataset</option>
      <option class="option-eidc option-inms option-elter" value="nonGeographicDataset">Dataset (non-geographic)</option>
      <option class="option-eidc option-inms option-assist option-elter" value="aggregate">Data collection (aggregation)</option>
      <option class="option-eidc option-elter" value="application">Model code (application)</option>
      <option class="option-eidc" value="nercSignpost">NERC signpost</option>
      <option class="option-elter" value="signpost">Signpost</option>
      <option class="option-inms option-assist" value="thirdPartyDataset">Third-party dataset</option>
      <option class="option-eidc option-inms option-elter" value="service">Web service</option>
    </optgroup>
    <optgroup label="Other">
      <option class="option-iso" value="aggregate">Aggregate</option>
      <option class="option-iso" value="application">Application</option>
      <option class="option-iso" value="attribute">Attribute</option>
      <option class="option-iso" value="attributeType">Attribute type</option>
      <option class="option-iso" value="collection">Collection</option>
      <option class="option-iso" value="collectionHardware">Collection hardware</option>
      <option class="option-iso" value="collectionSession">Collection session</option>
      <option class="option-iso" value="coverage">Coverage</option>
      <option class="option-iso" value="dataset">Dataset</option>
      <option class="option-iso" value="dimensionGroup">Dimension group</option>
      <option class="option-iso" value="document">Document</option>
      <option class="option-iso" value="feature">Feature</option>
      <option class="option-iso" value="featureType">Feature type</option>
      <option class="option-iso" value="fieldSession">Field session</option>
      <option class="option-iso" value="initiative">Initiative</option>
      <option class="option-iso" value="metadata">Metadata</option>
      <option class="option-iso" value="model">Model</option>
      <option class="option-iso" value="nonGeographicDataset">Non-geographic dataset</option>
      <option class="option-iso" value="product">Product</option>
      <option class="option-iso" value="propertyType">Property type</option>
      <option class="option-iso" value="repository">Repository</option>
      <option class="option-iso" value="series">Series</option>
      <option class="option-iso" value="sample">Sample</option>
      <option class="option-iso" value="software">Software</option>
      <option class="option-iso" value="tile">Tile</option>
      <option class="option-iso" value="service">Web service</option>
    </optgroup>
</select>
`)
