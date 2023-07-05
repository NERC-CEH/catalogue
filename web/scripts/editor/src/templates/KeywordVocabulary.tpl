<div class="row keywordPicker">
    <div class="col-xs-12">
      <input class="form-control autocomplete" placeholder="Start typing to search for keywords in controlled vocabularies">
    </div>
    <div class="col-xs-12 vocabularyPicker"></div>
    <button class="btn btn-freetext">Or click here to enter a free-text keyword</button>
</div>
<div class="row">
    <div class="col-sm-7">
        <input data-name='value' class="hidden editor-input value" value="<%= data.value %>" placeholder="Keyword">
    </div>
    <div class="col-sm-5">
        <input data-name='uri' class="hidden editor-input uri" value="<%= data.uri %>" placeholder="uri of controlled term">
    </div>
</div>