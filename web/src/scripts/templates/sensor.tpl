<div class='container'>
    <form id="form" class="form-inline new-form" data-document="sensor" method="post" action="/documents/new/elter">
        <input name="type" type="hidden" value="dataset">
        <h1>
            <div id="title" class="sensor-title-block sensor-title form-editable" data-field="title" data-value="No Title" data-type="text" data-required>
                <i class="fa fa-pencil"></i>
                <span>No Title</span>
            </div>
            <div id="shortName" class="sensor-title-block sensor-title-small form-editable" data-field="shortName" data-value="" data-type="text">
                <i class="fa fa-pencil"></i>
                <small><b class='text-muted'>No Short Name</b></small>
            </div>
        </h1>
        <p id="description" class="lead form-editable" data-field="description" data-value="" data-type="textarea">
            <i class="fa fa-pencil"></i>
            <span><b class='text-muted'>No Description</b></span>
        </p>
        <dl class="dl-horizontal sensor-rows">
            <dt class="form-editable sensor-row sensor-row-key" data-field="serialNumber" data-value="" data-type="text">
                <i class="fa fa-pencil"></i>
                <span>Serial Number</span>
            </dt>
            <dd class="sensor-row sensor-row-value" id="serialNumber">
                <span><b class='text-muted'>No Serial Number</b></span>
            </dd>
            <dt class="form-editable sensor-row sensor-row-key" data-field="documentation" data-value="" data-type="text">
                <i class="fa fa-pencil"></i>
                <span>Documentation</span>
            </dt>
            <dd class="sensor-row sensor-row-value" id="documentation">
                <b class='text-muted'>No Documentation</b>
            </dd>
            <dt class="form-editable sensor-row sensor-row-key" data-field="processType" data-value="" data-type="processType">
                <i class="fa fa-pencil"></i>
                <span>Process Type</span>
            </dt>
            <dd class="sensor-row sensor-row-value" id="processType">
                <span><b class='text-muted'>No Process Type</b></span>
            </dd>
            <dt class="form-editable sensor-row sensor-row-key" data-field="manufacturer" data-value="" data-type="manufacturer">
                <i class="fa fa-pencil"></i>
                <span>Manufacturer</span>
            </dt>
            <dd class="sensor-row sensor-row-value" id="manufacturer">
                <b class='text-muted'>No Manufacturer</b>
            </dd>
            <dt class="form-editable sensor-row sensor-row-key" data-field="defualtParameters" data-value="" data-type="defualtParameters">
                <i class="fa fa-pencil"></i>
                <span>Default Parameters</span>
            </dt>
            <dd class="sensor-row sensor-row-value" id="defualtParameters">
                <b class='text-muted'>No Default Parameters</b>
            </dd>
        </dl>
    </form>
<div class="container">
    <div class="sensor-admin col-md-8">
        <a href="/elter/documents" class="btn btn-success">
            <i class="fa fa-files-o" aria-hidden="true"></i>
            <span>eLTER Documents</span>
        </a>
    </div>
</div>