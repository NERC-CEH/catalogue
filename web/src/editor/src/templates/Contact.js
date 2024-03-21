import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Role">Role</label>
    </div>
    <div class="col-sm-10">
        <select data-name="role" class="editor-input role" id="contacts0Role">
            <option value="" selected="">- Select Role -</option>
            <optgroup label="Frequently used">
                <option class="option-nm option-eidc" value="author">Author</option>
                <option class="option-eidc" value="custodian">Custodian</option>
                <option class="option-eidc" value="depositor">Depositor</option>
                <option class="option-eidc" value="distributor">Distributor</option>
                <option class="option-ukeof" value="funder">Funder</option>
                <option class="option-eidc option-nm" value="pointOfContact">Point of contact</option>
                <option class="option-ukeof" value="pointOfContact">Lead organisation (point of contact)</option>
                <option class="option-nm option-eidc" value="publisher">Publisher</option>
                <option class="option-nm option-eidc" value="rightsHolder">Rights holder</option>
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

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Person">Person</label>
    </div>
    <div class="col-sm-4">
        <input data-name='individualName' class="editor-input" id="contacts<%= data.index %>Person" value="<%= data.individualName %>">
    </div>
    <div class="col-sm-1">
        <label class="control-label" for="contacts<%= data.index %>nameIdentifier">ORCID</label>
    </div>
    <div class="col-sm-5">
        <input data-name='nameIdentifier' placeholder='https://orcid.org/0000-...' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Email">Email</label>
    </div>
    <div class="col-sm-10">
        <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Organisation">Organisation</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
</div>

<div class="postalAddress">
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
</div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>organisationIdentifier">Organisation's RoR</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationIdentifier' class="editor-input" id="contacts<%= data.index %>organisationIdentifier" value="<%= data.organisationIdentifier %>">
    </div>
</div>
`)
