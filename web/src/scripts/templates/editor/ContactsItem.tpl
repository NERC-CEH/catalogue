<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Person">Person</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='individualName' class="form-control input-sm" id="contacts<%= index %>Person" value="<%= individualName %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Email">Email</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='email' class="form-control input-sm" id="contacts<%= index %>Email" value="<%= email %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Organisation">Organisation</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='organisationName' class="form-control input-sm" id="contacts<%= index %>Organisation" value="<%= organisationName %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Role">Role</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <select data-name="role" class="form-control input-sm" id="contacts<%= index %>Role">
      <option value="" selected>- Select Role -</option>
      <option value="author">Author</option>
      <option value="custodian">Custodian</option>
      <option value="distributor">Distributor</option>
      <option value="originator">Originator</option>
      <option value="owner">Owner</option>
      <option value="pointOfContact">Point Of Contact</option>
      <option value="principalInvestigator">Principal Investigator</option>
      <option value="processor">Processor</option>
      <option value="publisher">Publisher</option>
      <option value="resourceProvider">Resource Provider</option>
      <option value="user">User</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Address">Address</label>
  </div>
  <div class="col-sm-11 col-lg-11">
    <input data-name="deliveryPoint" class="form-control input-sm" id="contacts<%= index %>Address" value="<%= address.deliveryPoint %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>City">City/Town</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="city" class="form-control input-sm" id="contacts<%= index %>City" value="<%= address.city %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>County">County</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="administrativeArea" class="form-control input-sm" id="contacts<%= index %>County" value="<%= address.administrativeArea %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Country">Country</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="country" class="form-control input-sm" id="contacts<%= index %>Country" value="<%= address.country %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="contacts<%= index %>Postcode">Postcode</label>
  </div>
  <div class="col-sm-4 col-lg-4">
    <input data-name="postalCode" class="form-control input-sm" id="contacts<%= index %>Postcode" value="<%= address.postalCode %>">
  </div>
  <div class="col-sm-1 col-lg-1">
  <% if (index === 'Add') { %>
    <button class="btn btn-default btn-sm pull-right" id="contactsAdd">Add</button>
  <% } else { %>
    <button class="btn btn-default btn-sm pull-right remove"><i class="glyphicon glyphicon-remove"></i></button>
  <% } %>
  </div>
</div>