import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input">
  <option value="" selected >- Add Topic Category -</option>
  <option value="biota">Biota&nbsp;&nbsp;&nbsp;&nbsp;(Flora and/or fauna in natural environment)</option>
  <option value="boundaries">Boundaries&nbsp;&nbsp;&nbsp;&nbsp;(Legal land descriptions)</option>
  <option value="climatologyMeteorologyAtmosphere">Climatology / Meteorology / Atmosphere&nbsp;&nbsp;&nbsp;&nbsp;(Processes and phenomena of the atmosphere)</option>
  <option value="economy">Economy&nbsp;&nbsp;&nbsp;&nbsp;(Economic activities, conditions and employment)</option>
  <option value="elevation">Elevation&nbsp;&nbsp;&nbsp;&nbsp;(Height above or below sea level)</option>
  <option value="environment">Environment&nbsp;&nbsp;&nbsp;&nbsp;(Environmental resources, protection and conservation)</option>
  <option value="farming">Farming&nbsp;&nbsp;&nbsp;&nbsp;(Rearing of animals and/or cultivation of plants)</option>
  <option value="geoscientificInformation">Geoscientific Information&nbsp;&nbsp;&nbsp;&nbsp;(Information pertaining to earth sciences)</option>
  <option value="health">Health&nbsp;&nbsp;&nbsp;&nbsp;(Health, health services, human ecology, and safety)</option>
  <option value="imageryBaseMapsEarthCover">Imagery / Base Maps / Earth Cover&nbsp;&nbsp;&nbsp;&nbsp;(Base maps)</option>
  <option value="inlandWaters">Inland Waters&nbsp;&nbsp;&nbsp;&nbsp;(Inland water features, drainage systems and their characteristics)</option>
  <option value="intelligenceMilitary">Intelligence / Military&nbsp;&nbsp;&nbsp;&nbsp;(Military bases, structures, activities)</option>
  <option value="location">Location&nbsp;&nbsp;&nbsp;&nbsp;(Positional information and services)</option>
  <option value="oceans">Oceans&nbsp;&nbsp;&nbsp;&nbsp;(Features and characteristics of salt water bodies (excluding inland waters))</option>
  <option value="planningCadastre">Planning / Cadastre&nbsp;&nbsp;&nbsp;&nbsp;(Information used for appropriate actions for future use of the land)</option>
  <option value="society">Society&nbsp;&nbsp;&nbsp;&nbsp;(Characteristics of society and cultures)</option>
  <option value="structure">Structure&nbsp;&nbsp;&nbsp;&nbsp;(Man-made construction)</option>
  <option value="transportation">Transportation&nbsp;&nbsp;&nbsp;&nbsp;(Means and aids for conveying persons and/or goods)</option>
  <option value="utilitiesCommunication">Utilities / Communication&nbsp;&nbsp;&nbsp;&nbsp;(Energy, water and waste systems and communications infrastructure and services)</option>
</select>
`)
