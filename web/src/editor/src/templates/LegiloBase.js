import _ from 'underscore'

// The suggestions panel around a table of suggestions. The table comes compiled and is rendered
// between the panel's two halves, so that every template is a literal the build can compile; it used
// to be pasted into this one as text (NERC-CEH/dri-one#464).
export default (title, table) => data => `
    <div class="row justify-content-center mt-3 mb-3">
      <div class="suggestions-table-result col-12">
        <div class="mb-2">
          <h4 class="mb-2 suggestions-table-header text-center" style="display: none;"><strong>${_.escape(title)}</strong></h4>

          <div class="no-suggestions-message text-center" style="display: none;"></div>

          <div class="table-responsive">
            <table class="table table-bordered table-sm suggestions-table" style="display: none;">
              ` + table(data) + `
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
  `
