import _ from 'underscore'

export default _.template(`
<div class="row justify-content-center mt-3">
  <div class="col-lg-8 col-12">
    <div class="mb-2">
      <h4 class="mb-2 keyword-table-header text-center" style="display: none;"><strong>Suggested Keywords</strong></h4>

      <div class="no-keywords-message text-center" style="display: none;"></div>

      <div class="table-responsive">
        <table class="table table-bordered table-sm keywords-table" style="display: none;">
          <thead>
            <tr>
              <th scope="col" class="col-1">Select</th>
              <th scope="col">Term</th>
              <th scope="col">URI</th>
              <th scope="col">Confidence</th>
            </tr>
          </thead>
          <tbody class="keywords-table-body table-group-divider">
          </tbody>
        </table>
      </div>
    </div>

    <div class="keywords-buttons text-center" style="display: none;">
      <button class="btn btn-light border legilo-add-btn">Add</button>
      <button class="btn btn-light border legilo-close-btn">Close</button>
      <button class="btn btn-light border legilo-load-more-btn">Load More</button>
    </div>
  </div>
</div>
`)
