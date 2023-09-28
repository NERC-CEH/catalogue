export default `
<div class="uploading row file-row">
    <div class="col-md-7">
        <div class="row">
            <div class="col-md-6 file-name">
                <i class="fa-solid fa-spinner fa-spin-2x"></i>
                <span data-dz-name></span>
            </div>
            <div class="col-md-2 file-size">
                <span class="file-size-value"></span>
            </div>
            <div class="col-md-4 file-status">Uploading</div>
            <div class="col-md-12 file-message"></div>
        </div>
    </div>
    <div class="col-md-5 file-actions">
        <a class="btn btn-danger cancel file-action" role="button">
            <i class="fa-solid fa-times"></i>
            <span>Cancel</span>
        </a>
    </div>
</div>
`
