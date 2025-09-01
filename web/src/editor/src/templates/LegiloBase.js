import _ from 'underscore'

export default (title, tableTemplate) => {
  return _.template(`
    <div class="row justify-content-center mt-3 mb-3">
      <div class="suggestions-table-result col-12">
        <div class="mb-2">
          <h4 class="mb-2 suggestions-table-header text-center" style="display: none;"><strong>${title}</strong></h4>

          <div class="no-suggestions-message text-center" style="display: none;"></div>

          <div class="table-responsive">
            <table class="table table-bordered table-sm suggestions-table" style="display: none;">
              ${tableTemplate}
            </table>
          </div>
        </div>

        <div class="suggestions-buttons text-center" style="display: none;">
          <button class="btn btn-outline-secondary suggestions-add-btn">Add</button>
          <button class="btn btn-outline-secondary suggestions-close-btn">Close</button>
          <button class="btn btn-outline-secondary suggestions-load-more-btn">Load More</button>
        </div>
      </div>
    </div>
  `)
}
