import _ from 'underscore'

export default _.template(`
  <div class="row">
    <div class="col-lg-2">
       <label>Name</label>
    </div>
    <div class="col-lg-2">
        <label class="fst-italic" for="contacts<%= data.index %>honorificPrefix">Title</label>
        <select data-name="honorificPrefix" class="editor-input honorificPrefix" id="contacts<%= data.index %>honorificPrefix">
          <option value="">--none--</option>
          <option value="Dr">Dr</option>
          <option value="Miss">Miss</option>
          <option value="Mr">Mr</option>
          <option value="Mrs">Mrs</option>
          <option value="Ms">Ms</option>
          <option value="Professor">Professor</option>
        </select>
    </div>
    <div class="col-lg-4 required">
        <label class="fst-italic" for="contacts<%= data.index %>givenName">Initial(s)</label>
        <input data-name='givenName' placeholder="e.g., C."  class="editor-input" id="contacts<%= data.index %>givenName" value="<%= data.givenName %>">
    </div>
    <div class="col-lg-4 required">
        <label class="fst-italic" for="contacts<%= data.index %>familyName">Family name</label>
        <input data-name='familyName' placeholder="e.g., Darwin" class="editor-input" id="contacts<%= data.index %>familyName" value="<%= data.familyName %>">
    </div>
  </div>
  <div class="row">
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>DisplayName">Display name</label>
    </div>
    <div class="col-sm-10">
       <input data-name='displayName' class="editor-input form-control-sm" id="contacts<%= data.index %>DisplayName" placeholder="Only include a display name if it is different to given/family name" value="<%= data.displayName %>">
    </div>
  </div>
  <div class="row">
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>nameIdentifier">ORCID</label>
    </div>
    <div class="col-sm-4">
        <input data-name='nameIdentifier' placeholder='https://orcid.org/0000-0000-0000-0000' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
    </div>
    <div class="col-sm-1 required">
        <label for="contacts<%= data.index %>Email">Email</label>
    </div>
    <div class="col-sm-5 required">
        <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2 required">
        <label for="contacts<%= data.index %>Organisation">Affiliation</label>
    </div>
    <div class="col-lg-10">
        <input placeholder="Search for organisation" data-name='organisationName' class="editor-input orgAutocomplete" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
    <div class="visually-hidden">
        <input data-name="organisationIdentifier" class="editor-input" value="<%= data.organisationIdentifier %>">
    </div>
</div>
`)
