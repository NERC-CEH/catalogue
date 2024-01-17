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
                <option value="author">Author</option>
                <option value="custodian">Custodian</option>
                <option value="depositor">Depositor</option>
                <option value="distributor">Distributor</option>
                <option value="pointOfContact">Point of contact</option>
                <option value="publisher">Publisher</option>
                <option value="resourceProvider">Resource provider</option>
                <option value="rightsHolder">Rights holder</option>
                <option value="owner">Senior Responsible Officer [SRO]</option>
            </optgroup>
            <!-- OTHER USERS -->
            <optgroup label="Other">
                <option value="coAuthor">Co-author</option>
                <option value="collaborator">Collaborator</option>
                <option value="contributor">Contributor</option>
                <option value="editor">Editor</option>
                <option value="funder">Funder</option>
                <option value="mediator">Mediator</option>
                <option value="originator">Originator</option>
                <option value="principalInvestigator">Principal investigator</option>
                <option value="processor">Processor</option>
                <option value="rightsHolder">Rights holder</option>
                <option value="sponsor">Sponsor</option>
                <option value="stakeholder">Stakeholder</option>
                <option value="user">User</option>
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
