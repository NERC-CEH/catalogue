<div class="row">
   <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Type">Type</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <select data-name="type" class="editor-input type" id="qa<%= data.index %>Type">
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
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Notes">Notes</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name="notes" id="qa<%= data.index %>Notes" class="editor-input" value="<%= data.notes %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name="date" type="date" id="qa<%= data.index %>Date" class="editor-input" value="<%= data.date %>">
  </div>
</div>
