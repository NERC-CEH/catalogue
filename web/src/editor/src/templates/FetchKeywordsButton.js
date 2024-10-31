import _ from 'underscore'

export default _.template(`
<div class="col-sm-3" >
    <div class="legilo">
      <button class="editor-button legilo-fetch-btn">Suggest Keywords</button>
      <div class="loader text-center">
        <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Loading...
      </div>
    </div>
</div>
`)
