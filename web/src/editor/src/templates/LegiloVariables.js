import _ from 'underscore'

export default _.template(`
<div class="row justify-content-center mt-3">
  <div class="col-lg-8 col-12">
    <div class="mb-2">
      <h4 class="mb-2 variable-table-header text-center" style="display: none;"><strong>Suggested Variables</strong></h4>

      <div class="no-variables-message text-center" style="display: none;"></div>

      <div class="table-responsive">
        <table class="table table-bordered table-sm variables-table" style="display: none;">
          <thead>
            <tr>
              <th scope="col" class="col-1">Select</th>
              <th scope="col">Name</th>
              <th scope="col">Title</th>
              <th scope="col">Unit</th>
              <th scope="col">Description</th>
              <th scope="col">Confidence</th>
            </tr>
          </thead>
          <tbody class="variables-table-body table-group-divider">
          </tbody>
        </table>
      </div>
    </div>

    <div class="variables-buttons text-center" style="display: none;">
      <button class="btn btn-light border legilo-variable-add-btn">Add</button>
      <button class="btn btn-light border legilo-variable-close-btn">Close</button>
      <button class="btn btn-light border legilo-variable-load-more-btn">Load More</button>
    </div>
  </div>
</div>
`)
