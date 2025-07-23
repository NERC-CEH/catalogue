import _ from 'underscore'

export default _.template(`
  <div>
    <label class="control-label">
      <span class="fw-bold">Please provide a short descriptive working title for the resource</span> <span class="text-danger">*</span>
    </label>
    <input type="text"
          data-name='title'
          class="form-control"
          placeholder="Enter working title"
          value="<%= data.title || '' %>">
    <div class="invalid-feedback"></div>
  </div>

  <div class="mt-5">
    <label  class="control-label">
      <span class="fw-bold">Provide a brief description of what the data are and how they were created.</span> <span class="text-danger">*</span>
      <br><span class="text-body-tertiary">Guidance on what to include in a description can be found <a class="link-secondary" href="https://eidc.ac.uk/deposit/metadata/guidance" target="_blank" rel="noopener noreferrer">in our guidance</a>.</span>
    </label>
    <textarea data-name='description'
              class="form-control"
              placeholder="Enter brief description"><%= data.description || '' %></textarea>
    <div class="invalid-feedback"></div>
  </div>

  <div class="mt-5">
    <label class="control-label">
      <span class="fw-bold">Type of resource</span> <span class="text-danger">*</span>
    </label>
    <select data-name="resourceType" class="form-select">
      <option value="">Select resource type</option>
      <option value="Experimental data" <%= data.resourceType === 'Experimental data' ? 'selected' : '' %>>Experimental data</option>
      <option value="Images" <%= data.resourceType === 'Images' ? 'selected' : '' %>>Images</option>
      <option value="Interview/survey" <%= data.resourceType === 'Interview/survey' ? 'selected' : '' %>>Interview/survey</option>
      <option value="Model output" <%= data.resourceType === 'Model output' ? 'selected' : '' %>>Model output</option>
      <option value="Monitoring data" <%= data.resourceType === 'Monitoring data' ? 'selected' : '' %>>Monitoring data</option>
      <option value="Other" <%= data.resourceType === 'Other' ? 'selected' : '' %>>Other</option>
    </select>
    <div class="invalid-feedback"></div>

    <div>
      <input type="text"
            data-name="resourceTypeOther"
            class="form-control"
            value="<%= data.otherResourceType || '' %>"
            placeholder="Specify resource type"
            style="<%= data.resourceType === 'Other' ? '' : 'display: none;' %>">
      <div class="invalid-feedback"></div>
    </div>
  </div>

  <div class="mt-2 easilyRecreated" style="<%= data.resourceType === 'Other' || data.resourceType === 'Model output' ? '' : 'display: none;' %>">
    <div class="alert alert-info">
      <b>Please note:</b> If the data is <b>model output from a publicly available model</b> that can be quickly and easily re-run to produce the same data, we may not consider it to be worth archiving and it may be outside the remit of the EIDC.

      <label class="form-label d-block mt-3">
        <span class="fw-bold">Could the data be easily regenerated?</span> <span class="text-danger">*</span>
      </label>
      <div class="form-check form-check-inline">
        <input class="form-check-input"
                type="radio"
                name="easilyRecreated-<%= data.index %>"
                data-name="easilyRecreated"
                id="easilyRecreatedYes"
                value=true
                <%= data.easilyRecreated === true ? 'checked' : '' %>>
        <label class="form-check-label" for="easilyRecreatedYes">Yes</label>
      </div>
      <div class="form-check form-check-inline">
        <input class="form-check-input"
                type="radio"
                name="easilyRecreated-<%= data.index %>"
                data-name="easilyRecreated"
                id="easilyRecreatedNo"
                value=false
                <%= data.easilyRecreated === false ? 'checked' : '' %>>
        <label class="form-check-label" for="easilyRecreatedNo">No</label>
      </div>
      <div>
        <input class="d-none"
                type="radio"
                name="easilyRecreated-<%= data.index %>"
                data-name="easilyRecreated"
                id="easilyRecreatedNone">
        <label class="d-none" >None</label>
        <div class="invalid-feedback"></div>
      </div>
    </div>
  </div>

  <div class="mt-5">
    <label class="control-label">
      <span class="fw-bold">What's the current format of the resource?</span> <span class="text-danger">*</span>
      <br><span class="text-body-tertiary">Data for deposit should normally be in a non-proprietary format. See <a class="link-secondary" href="https://eidc.ac.uk/deposit/preparingData" target="_blank" rel="noopener noreferrer">https://eidc.ac.uk/deposit/preparingData</a> for guidance.</span>
    </label>
    <select data-name="resourceFormat" class="form-select">
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
            class="form-control"
            value="<%= data.resourceFormatOther || '' %>"
            placeholder="Please specify the file format(s)"
            style="<%= data.resourceFormat === 'Other' ? '' : 'display: none;' %>">
      <div class="invalid-feedback"></div>
    </div>
  </div>

  <div class="mt-5">
    <label class="control-label">
      <span class="fw-bold">Size</span> <span class="text-danger">*</span>
      <br><span class="text-body-tertiary">What is the total size of the dataset and how many files are there?  (A rough estimate in Mb or Gb will do.)</span>
    </label>
    <input type="text"
          data-name="size"
          class="form-control"
          value="<%= data.size || '' %>"
          placeholder="Enter a rough estimate of size (Mb or Gb and approx number of files)">
    <div class="invalid-feedback"></div>
  </div>

  <div class="mt-2 largeData">
    <label class="form-label d-block mt-3">
      <span class="fw-bold">Is the total size of the data resource over 1Tb?</span> <span class="text-danger">*</span>
    </label>
    <div class="form-check form-check-inline">
      <input class="form-check-input"
              type="radio"
              name="largeData-<%= data.index %>"
              data-name="largeData"
              id="largeDataYes"
              value=true
              <%= data.largeData === true ? 'checked' : '' %>>
      <label class="form-check-label" for="largeDataYes">Yes</label>
    </div>
    <div class="form-check form-check-inline">
      <input class="form-check-input"
              type="radio"
              name="largeData-<%= data.index %>"
              data-name="largeData"
              id="largeDataNo"
              value=false
              <%= data.largeData === false ? 'checked' : '' %>>
      <label class="form-check-label" for="largeDataNo">No</label>
    </div>
    <div>
      <input class="d-none"
              type="radio"
              name="largeData-<%= data.index %>"
              data-name="largeData"
              id="largeDataNone">
      <label class="d-none" >None</label>
      <div class="invalid-feedback"></div>
    </div>
  </div>

`)
