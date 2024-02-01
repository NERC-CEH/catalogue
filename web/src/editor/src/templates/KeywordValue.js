import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-12">
        <input data-name='value' class="editor-input" id="descriptiveKeyword<%= data.index %>Keyword<%= data.keywordIndex %>Value" value="<%= data.value %>">
    </div>
</div>
`)
