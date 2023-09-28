import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-4">
    <label>Relationship</label><br>
    <select  data-name="rel" id="relationship<%= data.index %>Rel" class="editor-input rel">
        <option value='https://vocabs.ceh.ac.uk/eidc#generates'>GENERATES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a model generates a dataset)</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#memberOf'>MEMBER OF&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a dataset is a member of a data collection)</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#relatedTo'>RELATED TO</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#supersedes'>SUPERSEDES</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#uses'>USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option>
      </select>
  </div>
  <div class="col-sm-8">
    <div class="relationshipSearch">
      <label>Search</label><br>
      <input data-name="title" value="<%= data.title %>" id="relationship<%= data.index %>Name" class="form-control editor-input autocomplete" placeholder="Search the catalogue...">
    </div>
    <div class="relationshipRecord hidden">
    <label>Name</label><br>
      <input data-name="title" id="relationship<%= data.index %>Title" class="editor-input title" value="<%= data.title %>" autocomplete="off" disabled="true">
    </div>
  </div>
</div>

<div class="hidden">
  <input data-name="identifier" id="relationship<%= data.index %>Identifier" class="editor-input identifier" value="<%= data.identifier %>">
  <input data-name="associationType" id="relationship<%= data.index %>AssociationType" class="editor-input associationType " value="<%= data.associationType %>">
</div>
`)
