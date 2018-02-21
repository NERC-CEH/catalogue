<#import "./new-form.html.tpl" as form>

<div class="modal fade" id="new-document-modal" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">Create new <span class='new-document-title'></span> document?</h4>
      </div>
      <div class="modal-body">
        <@form.form document="" name="document" overrideReadOnly=true >
            <@form.input name="type" type="hidden" value="dataset"></@form.input>
            <@form.head>
                <@form.value name="title" errorMessage="Title is required">
                    <label>What is the title?</label>
                    <input name="title" placeholder="Title" required>
                </@form.value>
            </@form.head>
        </@form.form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-success save">Create</button>
      </div>
    </div>
  </div>
</div>