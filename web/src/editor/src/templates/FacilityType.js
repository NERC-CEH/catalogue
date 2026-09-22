import _ from 'underscore'

export default _.template(`
<select data-name="value" id="input-facilityType" class="form-select">
    <option value="catchment">Catchment (incl. sub-catchments)</option>
    <option value="laboratory">Laboratory</option>
    <option value="platform">Platform</option>
    <option value="sensor">Sensor</option>
    <option value="site">Site</option>
    <option value="station">Station</option>
    <option value="BH">Borehole</option>
    <option value="EC">Eddy covariance</option>
    <option value="FL">Flume</option>
    <option value="RG">Rain gauge</option>
    <option value="RS">Recharge station</option>
    <option value="SC">Scintillometer</option>
    <option value="SS">Soil station</option>
    <option value="ST">Soil transect</option>
    <option value="TF">Trapezoidal flume</option>
    <option value="WL">Water level sensor</option>
    <option value="WQ">Water quality</option>
    <option value="WS">Weather station</option>
    <option value="WV">Water velocity</option>
    <option value="WD">Water discharge</option>
</select>
`)

/*

*/
