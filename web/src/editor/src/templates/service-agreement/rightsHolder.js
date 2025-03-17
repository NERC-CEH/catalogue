import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>Organisation">Affiliation</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>organisationIdentifier">Organisation's RoR</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationIdentifier' class="editor-input" id="contacts<%= data.index %>organisationIdentifier" value="<%= data.organisationIdentifier %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
       <label>Name</label>
    </div>
    <div class="col-lg-2">
        <label class="fst-italic" for="contacts<%= data.index %>honorificPrefix">Title</label>
        <select data-name="honorificPrefix" class="editor-input honorificPrefix" id="contacts<%= data.index %>honorificPrefix">
          <option value="Dr">Dr</option>
          <option value="Miss">Miss</option>
          <option value="Mr">Mr</option>
          <option value="Mrs">Mrs</option>
          <option value="Ms">Ms</option>
          <option value="Professor">Professor</option>
        </select>
    </div>
    <div class="col-lg-4">
        <label class="fst-italic" for="contacts<%= data.index %>givenName">Given name</label>
        <input data-name='givenName' placeholder="e.g., P.G." class="editor-input" id="contacts<%= data.index %>givenName" value="<%= data.givenName %>">
    </div>
    <div class="col-lg-4">
        <label class="fst-italic" for="contacts<%= data.index %>familyName">Family name</label>
        <input data-name='familyName' placeholder="Wodehouse" class="editor-input" id="contacts<%= data.index %>familyName" value="<%= data.familyName %>">
    </div>
  </div>
`)
