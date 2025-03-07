import _ from 'underscore'

export default _.template(`
  <div class="row">
    <div class="col-lg-2">
       <label>Name</label>0
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
  <div class="row">
    <div class="col-sm-2 required">
        <label for="contacts<%= data.index %>Name">
          Full name
        </label>
    </div>
    <div class="col-sm-10 required">
       <input data-name='individualName' disabled class="editor-input" id="contacts<%= data.index %>Name" value="<%= data.individualName %>">
    </div>
  </div>
  <div class="row">
    <div class="col-sm-2 required">
        <label for="contacts<%= data.index %>Email">
          Email
        </label>
    </div>
    <div class="col-sm-10 required">
        <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2 required">
        <label for="contacts<%= data.index %>Organisation">
          Affiliation
        </label>
    </div>
    <div class="col-sm-4 required">
        <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>

    <div class="col-sm-2">
        <label for="contacts<%= data.index %>nameIdentifier">ORCID</label>
    </div>
    <div class="col-sm-4">
        <input data-name='nameIdentifier' placeholder='https://orcid.org/0000-0000-0000-0000' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
    </div>
</div>
`)
