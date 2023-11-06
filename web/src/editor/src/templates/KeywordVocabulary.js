import _ from 'underscore'

export default _.template(`
<div class="row keywordPicker">
    <div class="col-sm-6 col-xs-12">
      <input class="form-control editor-input autocomplete" placeholder="Start typing to search controlled vocabularies">
    </div>
    <div class="col-sm-6 col-xs-12 vocabularyPicker"></div>
</div>
<div class="row">
    <div class="col-sm-7">
        <input data-name='value' class="editor-input value" value="<%= data.value %>" placeholder="Keyword">
    </div>
    <div class="col-sm-5">
        <input data-name='uri' class="editor-input uri" value="<%= data.uri %>" placeholder="uri of controlled term">
    </div>
</div>
`)
