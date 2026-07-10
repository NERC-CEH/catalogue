import _ from 'underscore'

export default _.template(`
<select data-name="value" id="input-accessLimitation" <%= data.disabled%>>
    <option value="unknown"> -- Choose an option -- </option>
    <optgroup label="Available">
        <option value="noLimitations">
          No limitations
          <span>FREELY AVAILABLE and there is no need to login to access it</span>
        </option>
        <option value="registrationRequired">
          Login required
          <span>FREELY AVAILABLE, but users must login to access it</span>
        </option>
    </optgroup>
    <optgroup label="Controlled">
        <option value="controlled">
          CONTROLLED
          <span>To access this data, a bespoke licence needs to be negotiated and there may be a cost</span>
        </option>
    </optgroup>
    <optgroup label="Unavailable">
        <option value="embargoed">
          Embargoed
          <span>This resource is not yet available but a date has been set for its release</span>
        </option>
        <option value="superseded">
          Superseded
          <span>This resource has been withdrawn and has been replaced by an updated version</span>
        </option>
        <option value="withdrawn">
          Withdrawn
          <span>This resource has been withdrawn but has not been replaced</span>
        </option>
        <option value="deleted">
          Deleted
          <span>This resource has been permanently deleted</span>
        </option>
    </optgroup>
    <optgroup label="Restricted">
        <option value="public access limited according to Article 13(1)(h) of the INSPIRE Directive">
          ACCESS RESTRICTED as release would adversely affect the protection of the environment (e.g. the location of rare species)
        </option>
        <option value="public access limited according to Article 13(1)(f) of the INSPIRE Directive">
          ACCESS RESTRICTED as it contains personal information
        </option>
        <option value="public access limited according to Article 13(1)(d) of the INSPIRE Directive">
          ACCESS RESTRICTED for reasons of commercial confidentiality
        </option>
        <option value="public access limited according to Article 13(1)(e) of the INSPIRE Directive">
          ACCESS RESTRICTED as release would adversely affect intellectual property rights
        </option>
    </optgroup>
</select>
`)
