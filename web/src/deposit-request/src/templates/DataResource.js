import _ from 'underscore'

export default _.template(`
  <div class="border border-1 px-4 pt-2">
    <div class="item-space mt-2">
      <label class="control-label">
        Please provide a short descriptive working title for the resource <span class="text-danger">*</span>
      </label>
      <input type="text"
            data-name='title'
            class="form-control text-dark"
            placeholder="Enter working title"
            value="<%= data.title || '' %>">
      <div class="invalid-feedback"></div>
    </div>
    <div class="item-space">
      <label class="control-label">
        Please provide a brief description of what the data are and how they were created. Guidance on what to include in a description can be found here: <a href="https://eidc.ac.uk/deposit/metadata/guidance" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit/metadata/guidance</a> <span class="text-danger">*</span>
      </label>
      <textarea data-name='description'
                class="form-control text-dark"
                rows="3"
                placeholder="Enter brief description"><%= data.description || '' %></textarea>
      <div class="invalid-feedback"></div>
    </div>
    <div class="item-space">
      <label class="control-label">
        Type of resource <span class="text-danger">*</span>
      </label>
      <select data-name="resourceType" class="form-select text-dark">
        <option value="">Select resource type</option>
        <option value="Experimental data" <%= data.resourceType === 'Experimental data' ? 'selected' : '' %>>Experimental data</option>
        <option value="Images" <%= data.resourceType === 'Images' ? 'selected' : '' %>>Images</option>
        <option value="Interview/survey" <%= data.resourceType === 'Interview/survey' ? 'selected' : '' %>>Interview/survey</option>
        <option value="Model output" <%= data.resourceType === 'Model output' ? 'selected' : '' %>>Model output</option>
        <option value="Monitoring data" <%= data.resourceType === 'Monitoring data' ? 'selected' : '' %>>Monitoring data</option>
        <option value="Other" <%= data.resourceType === 'Other' ? 'selected' : '' %>>Other</option>
      </select>
      <div class="invalid-feedback"></div>
      <div class="mt-2">
        <input type="text"
              data-name="resourceTypeOther"
              class="form-control text-dark"
              value="<%= data.otherResourceType || '' %>"
              placeholder="Specify the resource(s). If listing more than one, separate them with commas."
              style="<%= data.resourceType === 'Other' ? '' : 'display: none;' %>">
        <div class="invalid-feedback"></div>
      </div>
    </div>
    <div class="item-space easilyRecreated" style="<%= data.resourceType === 'Other' || data.resourceType === 'Model output' ? '' : 'display: none;' %>">
      <label class="form-label">
        Can the data be easily recreated?  e.g. model output from a publicly available model that can be run quickly to produce the same data. If it is easily recreated we wouldn't generally consider this data of long-term value and hence it would be outside the remit of the EIDC. <span class="text-danger">*</span>
      </label>
      <div class="form-check">
        <input class="form-check-input"
               type="radio"
               name="easilyRecreated"
               data-name="easilyRecreated"
               id="easilyRecreatedYes"
               value=true
               <%= data.easilyRecreated === true ? 'checked' : '' %>>
        <label class="form-check-label" for="easilyRecreatedYes">Yes</label>
      </div>
      <div class="form-check">
        <input class="form-check-input"
               type="radio"
               name="easilyRecreated"
               data-name="easilyRecreated"
               id="easilyRecreatedNo"
               value=false
               <%= data.easilyRecreated === false ? 'checked' : '' %>>
        <label class="form-check-label" for="easilyRecreatedNo">No</label>
        <div class="invalid-feedback"></div>
      </div>
    </div>
    <div class="item-space">
      <label class="control-label">
        What's the current format of the resource? <span class="text-danger">*</span>
        <div class="fst-italic text-muted small">Data provided should normally be in a non-proprietary format. See <a href="https://eidc.ac.uk/deposit/preparingData" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit/preparingData</a> for guidance</div>
      </label>
      <select data-name="resourceFormat" class="form-select text-dark">
        <option value="">Select resource format</option>
        <option value="Comma separated values (csv)" <%= data.resourceFormat === 'Comma separated values (csv)' ? 'selected' : '' %>>Comma separated values (csv)</option>
        <option value="Excel spreadsheet" <%= data.resourceFormat === 'Excel spreadsheet' ? 'selected' : '' %>>Excel spreadsheet</option>
        <option value="NetCDF" <%= data.resourceFormat === 'NetCDF' ? 'selected' : '' %>>NetCDF</option>
        <option value="Shapefile" <%= data.resourceFormat === 'Shapefile' ? 'selected' : '' %>>Shapefile</option>
        <option value="Other" <%= data.resourceFormat === 'Other' ? 'selected' : '' %>>Other</option>
      </select>
      <div class="invalid-feedback"></div>
      <div class="mt-2">
        <input type="text"
              data-name="resourceFormatOther"
              class="form-control text-dark"
              value="<%= data.resourceFormatOther || '' %>"
              placeholder="Specify the resource format(s). If listing more than one, separate them with commas."
              style="<%= data.resourceFormat === 'Other' ? '' : 'display: none;' %>">
        <div class="invalid-feedback"></div>
      </div>
    </div>
    <div class="item-space">
      <label class="control-label">
        Size <span class="text-danger">*</span>
        <div class="fst-italic text-muted small">What is the size (Mb/Gb) and how many files are there?  (A rough estimate will do)</div>
      </label>
      <input type="text"
            data-name="size"
            class="form-control text-dark"
            value="<%= data.size || '' %>"
            placeholder="Enter size">
      <div class="invalid-feedback"></div>
    </div>
  </div>
`)
