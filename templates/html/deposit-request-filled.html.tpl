<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Deposit Request">
    <div class="container">
        <h1>Deposit Request</h1>
        <form class="deposit-request">
            <div class="form-group">
                <label>Dataset Title</label>
                <input type="text" class="form-control" value="Title" disabled />
            </div>
            <div class="form-group">
                <label>Depositor Name</label>
                <input type="text" class="form-control" value="Name" disabled />
            </div>
            <div class="form-group">
                <label>Depositor Email</label>
                <input type="email" class="form-control" value="Email" disabled />
            </div>
            <div class="form-group">
                <label>Other Contact</label>
                <textarea class="form-control" disabled rows="1">Other Contact</textarea>
            </div>
            <div class="form-group">
                <label>Project Name</label>
                <input type="text" class="form-control" value="Project Name" disabled />
            </div>
            <div class="form-group">
                <label>Planning Documents</label>
                <textarea class="form-control" disabled rows="1">Planning Documents</textarea>
            </div>
            <div class="form-group">
                <label>Nerc Funded</label>
                <input type="text" class="form-control" value="Yes" disabled />
            </div>
            <div class="form-group">
                <label>Public Funded</label>
                <input type="text" class="form-control" value="Yes" disabled />
            </div>
            <div class="form-group">
                <label>Dataset Offered</label>
                <div class="datasets-offered">
                    <div class="dataset">
                        <div class="row">
                            <div class="col-md-4 dataset-value">
                                <input type="text" class="form-control" disabled value="Name" />
                            </div>
                            <div class="col-md-4 dataset-format">
                                <input type="text" class="form-control" disabled value="Format" />
                            </div>
                            <div class="col-md-4 dataset-value">
                                <input type="text" class="form-control" disabled value="Size" />
                            </div>
                        </div>
                        <textarea class="form-control dataset-description" disabled rows="3">Description</textarea>
                        <#--  <a class='btn btn-link' href="the-sub-task">Upload Files</a>  -->
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label>Related Datasets</label>
                <input type="text" class="form-control doi" value="DOI 1" disabled>
                <input type="text" class="form-control doi" value="DOI 2" disabled>
            </div>
            <div id="science-domain">
                <div class="form-group">
                    <label>Science Domain</label>
                    <input type="text" class="form-control" value="Science Domain" disabled>
                </div>
            </div>
            <div class="form-group">
                <label>Unique Deposit</label>
                <input type="text" class="form-control" value="Yes" disabled />
            </div>
            <div class="form-group">
                <label>Model Output</label>
                <input type="text" class="form-control" value="Yes" disabled />
            </div>
            <div class="form-group">
                <label>Published Paper</label>
                <input type="text" class="form-control" value="Yes" disabled />
            </div>
            <div class="form-group">
                <label>Reusable</label>
                <input type="text" class="form-control" value="Yes" disabled />
            </div>
        </form>
    </div>
</@skeleton.master>