<div class='container'>
    <h3 id="saved" class="alert alert-info" style="display: none;">
        <i class="fa fa-save"></i> Saved
    </h3>
    <form id="form" class="new-form" data-document="manufacturer" novalidate>
        <input name="type" type="hidden" value="dataset">
        <input name="resourceType" type="hidden" value="dataset">
        <div class='head'>
            <input name="title" type="text" class='title' placeholder="Title" required data-error-message="Title is required" data-error-name="title">
        </div>
        <div class='body'>
            <div class='value value-link' data-name="website">
                <label><a href="#">Website</a></label>
                <input name="website" placeholder="Manufacturer Website" pattern="^(https?|ftp):\\/\\/(-\\.)?([^\\s\\/?\\.#-]+\\.?)+(\\/[^\\s]*)?$" data-error-name="documentation" data-error-message="Not a valid url, needs to be http(s)://url">
            </div>
        </div>
    </form>
    <a href="/elter/documents" class="btn btn-link">
        <i class="fa fa-files-o" aria-hidden="true"></i>
        <span>eLTER Documents</span>
    </a>
</div>
