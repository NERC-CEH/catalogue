import _ from 'underscore'

export default _.template(`
<select data-name="value" id="input-facilityType">
    <option value="laboratory">Laboratory</option>
    <option value="platform">Platform</option>
    <option value="sensor">Sensor</option>
    <option value="site">Site</option>
    <option value="station">Station</option>
</select>
`)
