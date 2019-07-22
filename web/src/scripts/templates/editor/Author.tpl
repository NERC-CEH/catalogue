<div class="row authors">
  <div class="col-sm-3">
    <input data-name='individualName'  placeholder='Name' class='editor-input' id='authors<%= data.index %>Name' value='<%= data.individualName %>'>
  </div>

  <div class="col-sm-3">
    <input data-name='organisationName'  placeholder='Affilation' class='editor-input' id='authors<%= data.index %>Affilation' value='<%= data.organisationName %>'>
  </div>

  <div class="col-sm-3">
    <input data-name='email'  placeholder='Email' class='editor-input' id='authors<%= data.index %>Email' value='<%= data.email %>'>
  </div>

  <div class="col-sm-3">
    <input data-name='nameIdentifier' placeholder='ORCID' class='editor-input' id='authors<%= data.index %>nameIdentifier' value="<%= data.nameIdentifier %>">
  </div>
</div>