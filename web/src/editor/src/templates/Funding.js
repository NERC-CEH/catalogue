import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="funding<%= data.index %>FunderName">Funding body</label>
  </div>
  <div class="col-sm-10">
    <input placeholder='(e.g. NERC)' data-name='funderName' class="editor-input" id="funding<%= data.index %>FunderName" value="<%= data.funderName %>">
  </div>
</div>
<div class="row">  
  <div class="col-sm-2">
    <label class="control-label" for="funding<%= data.index %>FunderIdentifier">Funding body ID</label>
  </div>
  <div class="col-sm-10">
    <input placeholder='(e.g.Crossref Funder ID or GRID)' data-name='funderIdentifier' class="editor-input" id="funding<%= data.index %>FunderIdentifier" value="<%= data.funderIdentifier %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="funding<%= data.index %>AwardTitle">Award name</label>
  </div>
  <div class="col-sm-10">
    <input placeholder='(e.g. DURESS)' data-name='awardTitle' class="editor-input" id="funding<%= data.index %>AwardTitle" value="<%= data.awardTitle %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="funding<%= data.index %>AwardTitle">Award reference</label>
  </div>
  <div class="col-sm-10">
    <input placeholder='(e.g. NE/J015105/1)' data-name='awardNumber' class="editor-input" id="funding<%= data.index %>AwardNumber" value="<%= data.awardNumber %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="funding<%= data.index %>AwardTitle">Award URL</label>
  </div>
  <div class="col-sm-10">
    <input placeholder='(e.g. https://gtr.ukri.org/projects?ref=NE/J015644/1)' data-name='awardURI' class="editor-input" id="funding<%= data.index %>AwardURI" value="<%= data.awardURI %>">
  </div>
</div>
`)
