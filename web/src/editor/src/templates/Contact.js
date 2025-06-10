import _ from 'underscore'

export default _.template(`
<div class="row role">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>Role">Role</label>
    </div>
    <div class="col-lg-10">
        <select data-name="role" class="role" id="contacts<%= data.index %>Role">
            <option value="" selected="">- Select Role -</option>
            <optgroup label="Frequently used">
                <option class="option-nm option-eidc option-ukceh" value="author">Author</option>
                <option class="option-eidc" value="custodian">Custodian</option>
                <option class="option-eidc" value="depositor">Depositor</option>
                <option class="option-eidc" value="distributor">Distributor</option>
                <option class="option-ukeof" value="funder">Funder</option>
                <option class="option-eidc option-ukceh option-nm" value="pointOfContact">Point of contact</option>
                <option class="option-ukeof" value="pointOfContact">Lead organisation (point of contact)</option>
                <option class="option-nm option-eidc" value="publisher">Publisher</option>
                <option class="option-nm option-eidc option-ukceh" value="rightsHolder">Rights holder</option>
                <option class="option-nm" value="owner">Senior Responsible Officer [SRO]</option>
                <option class="option-ukeof" value="stakeholder">Stakeholder</option>
                <option class="option-ukeof" value="user">User</option>
            </optgroup>
            <!-- OTHER USERS -->
            <optgroup class="option-iso" label="Complete list">
              <option class="option-iso" value="author">Author</option>
              <option class="option-iso" value="coAuthor">Co-author</option>
              <option class="option-iso" value="collaborator">Collaborator</option>
              <option class="option-iso" value="contributor">Contributor</option>
              <option class="option-iso" value="custodian">Custodian</option>
              <option class="option-iso" value="depositor">Depositor</option>
              <option class="option-iso" value="distributor">Distributor</option>
              <option class="option-iso" value="editor">Editor</option>
              <option class="option-iso" value="funder">Funder</option>
              <option class="option-iso" value="mediator">Mediator</option>
              <option class="option-iso" value="originator">Originator</option>
              <option class="option-iso" value="pointOfContact">Point of contact</option>
              <option class="option-iso" value="principalInvestigator">Principal investigator</option>
              <option class="option-iso" value="processor">Processor</option>
              <option class="option-iso" value="publisher">Publisher</option>
              <option class="option-iso" value="resourceProvider">Resource provider</option>
              <option class="option-iso" value="rightsHolder">Rights holder</option>
              <option class="option-iso" value="sponsor">Sponsor</option>
              <option class="option-iso" value="stakeholder">Stakeholder</option>
              <option class="option-iso" value="user">User</option>
            </optgroup>
        </select>
    </div>
</div>
<div class="row organisation">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>Organisation">Organisation</label>
    </div>
    <div class="col-lg-10">
        <input placeholder="Start typing to show a list of organisation. Pick from the list if possible" data-name='organisationName' class="editor-input orgAutocomplete" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-2">
        <label for="contacts<%= data.index %>organisationIdentifier">RoR</label>
    </div>
    <div class="col-lg-10">
        <input data-name='organisationIdentifier' placeholder="https//ror.org/..." class="ror editor-input" id="contacts<%= data.index %>organisationIdentifier" value="<%= data.organisationIdentifier %>">
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
            <select data-name="honorificPrefix" class="honorificPrefix" id="contacts<%= data.index %>honorificPrefix">
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
