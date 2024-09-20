import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-lg-9 col-md-10 col-sm-12 col-xs-12 col-lg-offset-3">
    <div class="form-group">
      <h4 class="mb-3 keyword-table-header text-center" style="display: none;"><strong>Suggested Keywords</strong></h4>

      <div class="no-keywords-message text-center" style="display: none;"></div>

      <div class="table-responsive">
        <table class="table table-bordered table-striped keywords-table" style="display: none;">
          <thead>
            <tr>
              <th class="col-xs-1">Select</th>
              <th class="col-xs-2">Term</th>
              <th class="col-xs-6">URI</th>
              <th class="col-xs-1">Confidence</th>
            </tr>
          </thead>
          <tbody class="keywords-table-body">
          </tbody>
        </table>
      </div>
    </div>

    <div class="keywords-buttons text-center" style="display: none;">
      <button class="btn btn-default legilo-add-btn">Add</button>
      <button class="btn btn-default legilo-close-btn">Close</button>
      <button class="btn btn-default legilo-load-more-btn">Load More</button>
    </div>
  </div>
</div>
`)
