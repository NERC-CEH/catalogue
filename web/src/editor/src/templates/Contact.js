import _ from 'underscore'

export default _.template(`
<div class="role row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>Role">Role</label>
    </div>
    <div class="col-lg-10">
        <select data-name='role' class="form-select role-select" id="contacts<%= data.index %>Role"></select>
    </div>
</div>
<div class="contributorRole row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>ContributorRole">Contributor role</label>
    </div>
    <div class="col-lg-10">
        <select data-name='contributorRole' class="contributorRole-select form-select" id="contacts<%= data.index %>ContributorRole">
          <option value="">Select a role</option>
          <option value="data-creator">Data creator</option>
          <option value="data-curator">Data curator</option>
          <option value="collaborator">Collaborator</option>
          <option value="researcher">Researcher</option>
          <option value="technician">Technician</option>
          <option value="project-leader">Project leader</option>
          <option value="workpackage-leader">Workpackage leader</option>
        </select>
    </div>
</div>
<div class="row organisation">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>Organisation">Organisation</label>
    </div>
    <div class="col-lg-10">
        <input autocomplete="off" aria-autocomplete="none" placeholder="Start typing to show a list of organisation. Pick from the list if possible" data-name='organisationName' class="editor-input orgAutocomplete" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>organisationIdentifier">RoR ID</label>
    </div>
    <div class="col-lg-10">
        <input autocomplete="off" aria-autocomplete="none" data-name='organisationIdentifier' placeholder="https//ror.org/..." class="ror editor-input" id="contacts<%= data.index %>organisationIdentifier" value="<%= data.organisationIdentifier %>">
    </div>
</div>
<div class="row contactName">
    <div class="col-lg-2">
       <label>Name</label>
    </div>
    <div class="col-lg-10">
        <div class="row g-0">
          <div class="col-lg-4 pe-1">
          <div class="form-floating">
            <select data-name="honorificPrefix" class="form-select honorificPrefix" id="contacts<%= data.index %>honorificPrefix">
              <option value="">--none--</option>
              <option value="Dr">Dr</option>
              <option value="Miss">Miss</option>
              <option value="Mr">Mr</option>
              <option value="Mrs">Mrs</option>
              <option value="Ms">Ms</option>
              <option value="Professor">Professor</option>
            </select>
            <label for="contacts<%= data.index %>honorificPrefix">Title</label>
          </div>
        </div>
        <div class="col-lg-4 pe-1">
          <div class="form-floating">
            <input data-name='givenName' placeholder="e.g., C."  class="editor-input" id="contacts<%= data.index %>givenName" value="<%= data.givenName %>">
            <label for="contacts<%= data.index %>givenName">Given name</label>
          </div>
        </div>
        <div class="col-lg-4">
          <div class="form-floating">
            <input data-name='familyName' placeholder="e.g., Darwin" class="editor-input" id="contacts<%= data.index %>familyName" value="<%= data.familyName %>">
            <label for="contacts<%= data.index %>familyName">Family name</label>
          </div>
        </div>
      </div>
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>Email">Email</label>
    </div>
    <div class="col-lg-10">
        <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>nameIdentifier">ORCID</label>
    </div>
    <div class="col-lg-10">
        <input data-name='nameIdentifier' placeholder='https://orcid.org/0000-...' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>DisplayName">Display name</label>
    </div>
    <div class="col-lg-10">
        <input data-name='displayName' class="editor-input" placeholder="If different to name" id="contacts<%= data.index %>DisplayName" value="<%= data.displayName %>">
    </div>
</div>
<div class="extended d-none" id="addressDetail<%= data.index %>">
    <div class="text-body-tertiary">
      <hr>
    </div>
    <div class="row">
        <div class="col-lg-2">
            <label for="contacts<%= data.index %>Address">Address</label>
        </div>
        <div class="col-lg-10">
            <input data-name="deliveryPoint" class="editor-input" id="contacts<%= data.index %>Address" value="<%= data.address.deliveryPoint %>">
        </div>
    </div>
    <div class="row">
        <div class="col-lg-2">
            <label for="contacts<%= data.index %>City">City/Town</label>
        </div>
        <div class="col-lg-10">
            <input data-name="city" class="editor-input" id="contacts<%= data.index %>City" value="<%= data.address.city %>">
        </div>
    </div>
    <div class="row">
        <div class="col-lg-2">
            <label for="contacts<%= data.index %>County">County</label>
        </div>
        <div class="col-lg-10">
            <input data-name="administrativeArea" class="editor-input" id="contacts<%= data.index %>County" value="<%= data.address.administrativeArea %>">
        </div>
    </div>
    <div class="row">
        <div class="col-sm-2">
            <label for="contacts<%= data.index %>Postcode">Postcode</label>
        </div>
        <div class="col-lg-10">
            <input data-name="postalCode" class="editor-input" id="contacts<%= data.index %>Postcode" value="<%= data.address.postalCode %>">
        </div>
    </div>
    <div class="row">
        <div class="col-lg-2">
            <label for="contacts<%= data.index %>Country">Country</label>
        </div>
        <div class="col-lg-10">
            <input data-name="country" class="editor-input" id="contacts<%= data.index %>Country" value="<%= data.address.country %>">
        </div>
    </div>
</div>

</div>

`)
