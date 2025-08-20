import _ from 'underscore'

export default _.template(`
<div class="table-responsive position-relative overflow-visible p-1">
  <table id="filetable" class="table table-bordered my-3">
    <thead>
      <tr class="table-secondary">
        <th>File</th>
        <th>Size</th>
        <th>Checksum</th>
        <th>Status</th>
        <th></th>
      </tr>
    </thead>
    <thead>
      <tr class="filters table-light">
        <th>
          <div class="position-relative">
            <input type="text" class="form-control form-control-sm column-search" placeholder="Search file ..." />
            <span class="position-absolute top-50 end-0 translate-middle-y pe-2 clear-search" style="cursor: pointer; display: none;">
              &times;
            </span>
          </div>
        </th>
        <th>
          <div class="position-relative">
            <input type="text" class="form-control form-control-sm column-search" placeholder="Search size ..." />
            <span class="position-absolute top-50 end-0 translate-middle-y pe-2 clear-search" style="cursor: pointer; display: none;">
              &times;
            </span>
          </div>
        </th>
        <th>
          <select class="form-select form-select-sm select-filter checksum-filter">
          </select>
        </th>
        <th>
          <select class="form-select form-select-sm select-filter status-filter">
          </select>
        </th>
        <th>
        </th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</div>
`)
