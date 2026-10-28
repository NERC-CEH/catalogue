import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-xl-4 col-xxl-3">
        <label for="resourceMaintenance<%- data.index %>Frequency">Frequency of updates</label>
    </div>
    <div class="col-xl-8 col-xxl-9">
        <select data-name="frequencyOfUpdate" class="frequencyOfUpdate" id="resourceMaintenance<%- data.index %>Frequency">
            <option value="" selected>- Select frequency of updates -</option>
            <option value="notPlanned">Not Planned</option>
            <option value="daily">Daily</option>
            <option value="weekly">Weekly</option>
            <option value="fortnightly">Fortnightly</option>
            <option value="monthly">Monthly</option>
            <option value="quarterly">Quarterly</option>
            <option value="annually">Annually</option>
            <option value="biannually">Biannually</option>
            <option value="asNeeded">As Needed</option>
            <option value="continual">Continual</option>
            <option value="irregular">Irregular</option>
            <option value="unknown">Unknown</option>
        </select>
    </div>
</div>
<div class="row lastUpdateRow">
    <div class="col-xl-4 col-xxl-3">
        <label for="resourceMaintenance<%- data.index %>_lastUpdate">Last updated</label>
    </div>
    <div class="col-xl-8 col-xxl-9">
        <input data-name="lastUpdate" id="resourceMaintenance<%- data.index %>_lastUpdate" value="<%- data.lastUpdate || '' %>" type="date" class="editor-input" autocomplete="off" />
    </div>
</div>
<% const scope = data.scope || {}; %>
<div class="row scopeRow d-none">
    <div class="col-xl-4 col-xxl-3">
        <label>Scope of updates</label>
    </div>
    <div class="col-xl-7 col-xxl-9">
        <div class="form-check">
            <input class="scopeAttribute form-check-input" <% if(scope.attribute) { %>checked<% } %> type="checkbox" id="resourceMaintenance<%- data.index %>_scopeAttribute">
            <label class="form-check-label" for="resourceMaintenance<%- data.index %>_scopeAttribute">
                Attributes
                <span class="text-body-tertiary">(e.g., parameters added/removed)</span>
            </label>
        </div>
        <div class="form-check">
            <input class="scopeFeature form-check-input" <% if(scope.feature) { %>checked<% } %> type="checkbox" id="resourceMaintenance<%- data.index %>_scopeFeature">
            <label class="form-check-label" for="resourceMaintenance<%- data.index %>_scopeFeature">
                Features
                <span class="text-body-tertiary">(e.g., monitoring sites added/removed)</span>
            </label>
        </div>
        <div class="form-check">
            <input class="scopeAttributeInstance form-check-input" <% if(scope.attributeInstance) { %>checked<% } %> type="checkbox" id="resourceMaintenance<%- data.index %>_scopeAttributeInstance">
            <label class="form-check-label" for="resourceMaintenance<%- data.index %>_scopeAttributeInstance">
                Attribute instances
                <span class="text-body-tertiary">(e.g., data values added/removed)</span>
            </label>
        </div>
        <div class="form-check">
            <input class="scopeFeatureInstance form-check-input" <% if(scope.featureInstance) { %>checked<% } %> type="checkbox" id="resourceMaintenance<%- data.index %>_scopeFeatureInstance">
            <label class="form-check-label" for="resourceMaintenance<%- data.index %>_scopeFeatureInstance">
                Feature instances
                <span class="text-body-tertiary">(e.g., data for monitoring sites added/removed)</span>
            </label>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-xl-4 col-xxl-3">
        <label for="resourceMaintenance<%- data.index %>Note">Notes (optional)</label>
    </div>
    <div class="col-xl-8 col-xxl-9">
        <textarea data-name="note" id="resourceMaintenance<%- data.index %>Note" class="editor-textarea"><%- data.note %></textarea>
    </div>
</div>
`)
