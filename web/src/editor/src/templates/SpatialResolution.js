import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-xl-2 col-lg-3 col-md-12">
        <label for="spatialResolution<%= data.index %>Distance">Distance</label>
    </div>
    <div class="col-xl-4 col-lg-3 col-md-12">
        <input data-name='distance' type="number" step="1" min="1" class="editor-input" id="spatialResolution<%= data.index %>Distance" value="<%= data.distance %>">
    </div>
    <div class="col-xl-1 col-lg-2 col-md-12">
        <label for="spatialResolution<%= data.index %>Uom">Unit</label>
    </div>
    <div class="col-xl-5 col-lg-4 col-md-12">
        <select data-name='uom' id="spatialResolution<%= data.index %>Uom">
          <option value="urn:ogc:def:uom:EPSG::9102">Degrees</value>
          <option value="urn:ogc:def:uom:EPSG::9001">Metres</value>
          <option value="urn:ogc:def:uom:EPSG::9107">Arc minutes</value>
          <option value="urn:ogc:def:uom:EPSG::9108">Arc seconds</value>
        </select>
    </div>
</div>
`)
