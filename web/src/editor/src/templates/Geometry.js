import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="map" style="width: 600px; height: 600px;"></div>
</div>
<div class="row">
  <input type="radio" name="type" value="create" id="createToggle"/>
  <label for="createToggle">create geometry</label>
  <input type="radio" name="type" value="modify" id="modifyToggle"/>
  <label for="modifyToggle">modify geometry</label>
  <input type="radio" name="type" value="delete" id="deleteToggle"/>
  <label for="deleteToggle">delete geometry</label>
  <input type="radio" name="type" value="navigate" checked id="navigateToggle"/>
  <label for="navigateToggle">navigate</label>
</div>
`)
