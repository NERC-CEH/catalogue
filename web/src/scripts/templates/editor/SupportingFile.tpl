<div class="row dataFile">
  <div class="col-sm-4">
    <input data-name='filename'  placeholder='Filename. It should have no spaces and should not include non-standard characters (e.g. *@^£$)' class='editor-input' id='datafile<%= data.index %>filename' value='<%= data.filename %>'>
  </div>

  <div class="col-sm-8">
    <textarea rows="3" placeholder='Content' data-name="content" id="datafile<%= data.index %>Description" class="editor-textarea" <%= data.disabled%>><%= data.content %></textarea>
  </div>
</div>

