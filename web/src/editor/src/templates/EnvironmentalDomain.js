import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input environmentalDomain">
  <option value="atmosphere">Atmosphere</option>
  <option value="biosphere">Biosphere</option>
  <option value="builtEnvironment">Built environment</option>
  <option value="cryosphere">Cryosphere</option>
  <option value="freshwater">Freshwater</option>
  <option value="geosphere">Geosphere</option>
  <option value="groundwater">Groundwater</option>
  <option value="lithosphere">Lithosphere</option>
  <option value="marine">Marine</option>
</select>
`)
