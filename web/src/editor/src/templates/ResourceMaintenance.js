import _ from 'underscore'

export default _.template(`
<div class="col-sm-2 col-lg-2">
    <label for="resourceMaintenance<%= data.index %>Frequency">Frequency of Update</label>
</div>
<div class="col-sm-3 col-lg-3">
    <select data-name="frequencyOfUpdate" id="resourceMaintenance<%= data.index %>Frequency" class="editor-input">
        <option value="" selected>- Select Frequency of Update -</option>
        <optgroup label="Time Period">
            <option value="daily">Daily</option>
            <option value="weekly">Weekly</option>
            <option value="fortnightly">Fortnightly</option>
            <option value="monthly">Monthly</option>
            <option value="quarterly">Quarterly</option>
            <option value="annually">Annually</option>
            <option value="biannually">Biannually</option>
        </optgroup>
        <optgroup label="Other">
            <option value="asNeeded">As Needed</option>
            <option value="continual">Continual</option>
            <option value="irregular">Irregular</option>
            <option value="notPlanned">Not Planned</option>
            <option value="unknown">Unknown</option>
        </optgroup>
    </select>
</div>
<div class="col-sm-1 col-lg-1">
    <label for="resourceMaintenance<%= data.index %>Note">Notes</label>
</div>
<div class="col-sm-6 col-lg-6">
    <textarea data-name="note" rows="3" id="resourceMaintenance<%= data.index %>Note" class="editor-textarea"><%= data.note %></textarea>
</div>
`)
