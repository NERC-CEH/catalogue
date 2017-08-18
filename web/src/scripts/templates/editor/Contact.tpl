<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>Role">Role</label>
  </div>
  <div class="col-sm-10">
    <select data-name="role" class="editor-input role" id="contacts<%= data.index %>Role">
      <option value="" selected >- Select Role -</option>
      <option value="author">Author</option>
      <option value="coAuthor">Co-author</option>
      <option value="collaborator">Collaborator</option>
      <option value="contributor">Contributor</option>
      <option value="custodian">Custodian</option>
      <option value="distributor">Distributor</option>
      <option value="editor">Editor</option>
      <option value="funder">Funder</option>
      <option value="mediator">Mediator</option>
      <option value="originator">Originator</option>
      <option value="owner">Owner</option>
      <option value="pointOfContact">Point Of Contact</option>
      <option value="principalInvestigator">Principal Investigator</option>
      <option value="processor">Processor</option>
      <option value="publisher">Publisher</option>
      <option value="resourceProvider">Resource Provider</option>
      <option value="rightsHolder">Rights Holder</option>
      <option value="sponsor">Sponsor</option>
      <option value="stakeholder">Stakeholder</option>
      <option value="user">User</option>
    </select>
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>Person">Person</label>
  </div>
  <div class="col-sm-10">
    <input data-name='individualName' class="editor-input" id="contacts<%= data.index %>Person" value="<%= data.individualName %>">
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>Organisation">Organisation</label>
  </div>
  <div class="col-sm-10">
    <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>Address">Address</label>
  </div>
  <div class="col-sm-10">
    <input data-name="deliveryPoint" class="editor-input" id="contacts<%= data.index %>Address" value="<%= data.address.deliveryPoint %>">
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>City">City/Town</label>
  </div>
  <div class="col-sm-5">
    <input data-name="city" class="editor-input" id="contacts<%= data.index %>City" value="<%= data.address.city %>">
  </div>
  <div class="col-sm-1">
    <label class="control-label" for="contacts<%= data.index %>County">County</label>
  </div>
  <div class="col-sm-4">
    <input data-name="administrativeArea" class="editor-input" id="contacts<%= data.index %>County" value="<%= data.address.administrativeArea %>">
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>Postcode">Postcode</label>
  </div>
  <div class="col-sm-5">
    <input data-name="postalCode" class="editor-input" id="contacts<%= data.index %>Postcode" value="<%= data.address.postalCode %>">
  </div>
  <div class="col-sm-1">
    <label class="control-label" for="contacts<%= data.index %>Country">Country</label>
  </div>
  <div class="col-sm-4">
    <input data-name="country" class="editor-input" id="contacts<%= data.index %>Country" value="<%= data.address.country %>">
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>Email">Email</label>
  </div>
  <div class="col-sm-10">
    <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
  </div>
</div>

<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="contacts<%= data.index %>nameIdentifier">Identifier</label>
  </div>
  <div class="col-sm-10">
    <input data-name='nameIdentifier' placeholder='(e.g. ORCiD or ISNI)' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
  </div>
</div>