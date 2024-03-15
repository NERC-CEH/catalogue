import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-2 col-sm-4">
      <select class="editor-input function" data-name="function" >
        <option value="" selected class="option">-- Type of information --</option>
        <option value="objectives" class="option option-ukeof">Objectives</option>
      </select>
    </div>
    <div class="col-md-10 col-sm-8">
      <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" rows="6"><%= data.description %></textarea>
    </div>
</div>
`)

