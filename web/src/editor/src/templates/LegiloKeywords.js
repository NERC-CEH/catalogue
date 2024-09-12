import _ from 'underscore'

export default _.template(`
<div class="row col-sm-offset-3">
      <div class="col-sm-9">
        <div class="form-group">
          <button class="btn btn-default legilo-fetch-btn">Fetch Keywords</button>
        </div>

        <div class="form-group">
          <div class="loader text-center" style="display: none;">
            <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Loading...
          </div>
          <table class="table table-bordered table-striped keywords-table" style="display: none;">
            <thead>
              <tr>
                <th class="col-xs-1">Select</th>
                <th class="col-xs-3">Term</th>
                <th class="col-xs-5">URI</th>
                <th class="col-xs-1">Confidence</th>
              </tr>
            </thead>
            <tbody class="keywords-table-body">

            </tbody>
          </table>
        </div>

        <div class="form-group load-more-btn" style="display: none;">
      <button class="btn btn-default legilo-load-more-btn">Load More</button>
    </div>

        <div class="keywords-buttons" style="display: none;">
          <button class="btn btn-default legilo-add-btn">Add</button>
          <button class="btn btn-default legilo-cancel-btn">Cancel</button>
        </div>
      </div>
    </div>
`)
