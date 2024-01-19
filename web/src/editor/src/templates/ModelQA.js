import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1">
            <label for="qa<%= data.index %>Category">Category</label>
    </div>
    <div class="col-sm-5">
            <select data-name="category" class="editor-input category" id="qa<%= data.index %>Category">
                <option value="" selected>-- Choose one --</option>
                <option value="developerTesting">Developer testing</option>
                <option value="governance">Governance</option>
                <option value="guidelinesAndChecklists">Guidelines and checklists</option>
                <option value="internalModelAudit">Model audit (internal)</option>
                <option value="externalModelAudit">Model audit (external)</option>
                <option value="internalPeerReview">Peer review (internal)</option>
                <option value="externalPeerReview">Peer review (external)</option>
                <option value="periodicReview">Periodic review</option>
                <option value="transparency">Transparency</option>
            </select>
    </div>

    <div class="col-sm-1">
        <label for="qa<%= data.index %>Date">Date</label>
    </div>
    <div class="col-sm-5">
        <input data-name="date" type="date" id="qa<%= data.index %>Date" class="editor-input" value="<%= data.date %>">
    </div>

    <div class="col-sm-1">
        <label for="qa<%= data.index %>Notes">Notes</label>
    </div>
    <div class="col-sm-11">
        <textarea data-name="notes" id="qa<%= data.index %>Notes" class="editor-textarea" rows="3"><%= data.notes %></textarea>
    </div>
</div>
`)
