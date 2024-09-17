import _ from 'underscore'

export default _.template(`
<div class="col-sm-3" style="margin-top: 10px;">
    <div class="pull-right">
      <button class="btn btn-default legilo-fetch-btn">Suggested Keywords</button>

      <div class="loader text-center" style="display: none; margin-top: 10px;">
        <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Loading...
      </div>
    </div>
</div>
`)
