import _ from 'underscore'

export default _.template(`
<div class="table-responsive position-relative overflow-visible p-1">
  <table id="filesTable" class="table table-hover w-100 my-2">
    <thead>
      <tr>
        <th class="col-3">File</th>
        <th class="col-3">Path</th>
        <th class="col-1">Size</th>
        <th class="col-1">Checksum</th>
        <th class="col-2">Status</th>
        <th class="col-2">Action</th>
      </tr>
    </thead>
    <thead>
      <tr class="filters">
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
            <input type="text" class="form-control form-control-sm column-search" placeholder="Search path ..." />
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
        <th></th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</div>
`)
