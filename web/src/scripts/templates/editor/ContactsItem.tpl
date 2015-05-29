<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Person">Person</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='individualName' class="editor-input" id="contacts<%= index %>Person" value="<%= individualName %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Email">Email</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='email' class="editor-input" id="contacts<%= index %>Email" value="<%= email %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Organisation">Organisation</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='organisationName' class="editor-input" id="contacts<%= index %>Organisation" value="<%= organisationName %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Role">Role</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <select data-name="role" class="editor-input" id="contacts<%= index %>Role">
      <option value="" <% if (role === '') { %> selected <% } %> >- Select Role -</option>
      <option value="author" <% if (role === 'author') { %> selected <% } %> >Author</option>
      <option value="custodian" <% if (role === 'custodian') { %> selected <% } %> >Custodian</option>
      <option value="distributor" <% if (role === 'distributor') { %> selected <% } %> >Distributor</option>
      <option value="originator" <% if (role === 'originator') { %> selected <% } %> >Originator</option>
      <option value="owner" <% if (role === 'owner') { %> selected <% } %> >Owner</option>
      <option value="pointOfContact" <% if (role === 'pointOfContact') { %> selected <% } %> >Point Of Contact</option>
      <option value="principalInvestigator" <% if (role === 'principalInvestigator') { %> selected <% } %> >Principal Investigator</option>
      <option value="processor" <% if (role === 'processor') { %> selected <% } %> >Processor</option>
      <option value="publisher" <% if (role === 'publisher') { %> selected <% } %> >Publisher</option>
      <option value="resourceProvider" <% if (role === 'resourceProvider') { %> selected <% } %> >Resource Provider</option>
      <option value="user" <% if (role === 'user') { %> selected <% } %> >User</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Address">Address</label>
  </div>
  <div class="col-sm-11 col-lg-11">
    <input data-name="deliveryPoint" class="editor-input" id="contacts<%= index %>Address" value="<%= address.deliveryPoint %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>City">City/Town</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="city" class="editor-input" id="contacts<%= index %>City" value="<%= address.city %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>County">County</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="administrativeArea" class="editor-input" id="contacts<%= index %>County" value="<%= address.administrativeArea %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Country">Country</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="country" class="editor-input" id="contacts<%= index %>Country" value="<%= address.country %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Postcode">Postcode</label>
  </div>
  <div class="col-sm-4 col-lg-4">
    <input data-name="postalCode" class="editor-input" id="contacts<%= index %>Postcode" value="<%= address.postalCode %>">
  </div>
  <div class="col-sm-1 col-lg-1">
  <% if (index === 'Add') { %>
    <button class="editor-button add">Add</button>
  <% } else { %>
    <button class="editor-button remove"><i class="glyphicon glyphicon-remove"></i></button>
  <% } %>
  </div>
</div>