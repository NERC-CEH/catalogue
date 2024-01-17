import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1 col-lg-1">
        <label class="control-label" for="descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Value">Value</label>
    </div>
    <div class="col-sm-11 col-lg-5">
        <input data-name='value' class="editor-input" id="descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Value" value="<%= data.value %>">
    </div>
    <div class="col-sm-1 col-lg-1">
        <label class="control-label" for="descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Uri">Uri</label>
    </div>
    <div class="col-sm-11 col-lg-5">
        <input data-name='uri' class="editor-input" id="descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Uri" value="<%= data.uri %>">
    </div>
</div>
`)
