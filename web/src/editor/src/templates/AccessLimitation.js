import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input" id="input-accessLimitation" <%= data.disabled%>>
    <option value="unknown"> -- Choose an option -- </option>
<optgroup label="Available">
  <option value="no limitations to public access">FREELY AVAILABLE and there is NO NEED TO LOG IN to access it (no limitations)</option>
  <option value="Registration is required to access this data">FREELY AVAILABLE - but USERS MUST LOG IN to access it</option>
</optgroup>
<optgroup label="Controlled">
  <option value="To access this data, a licence needs to be negotiated with the provider and there may be a cost">CONTROLLED - To access this data, a bespoke licence needs to be negotiated and there may be a cost</option>
</optgroup>
<optgroup label="Unavailable">
  <option value="embargoed">EMBARGOED - This resource is not yet available but a date has been set for its release</option>
  <option value="in-progress">IN PROGRESS - This resource is not yet available as is still being completed</option>
  <option value="superseded">SUPERSEDED - This resource has been withdrawn and has been replaced by an updated version</option>
  <option value="deleted">DELETED - This resource has been permanently removed from the data centre</option>
  <option value="withdrawn">WITHDRAWN - This resource has been withdrawn but has not been replaced</option>
</optgroup>
<optgroup label="Restricted">
  <option value="public access limited according to Article 13(1)(h) of the INSPIRE Directive">ACCESS RESTRICTED as release would adversely affect the protection of the environment (e.g. the location of rare species)</option>
  <option value="public access limited according to Article 13(1)(f) of the INSPIRE Directive">ACCESS RESTRICTED as it contains personal information</option>
  <option value="public access limited according to Article 13(1)(d) of the INSPIRE Directive">ACCESS RESTRICTED for reasons of commercial confidentiality</option>
  <option value="public access limited according to Article 13(1)(e) of the INSPIRE Directive">ACCESS RESTRICTED as release would adversely affect intellectual property rights</option>
</optgroup>
</select>
`)
