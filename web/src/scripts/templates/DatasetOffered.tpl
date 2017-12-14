<div class="dataset">
    <div class="row">
        <div class="col-md-4 dataset-value">
            <input type="text" placeholder="Name" class="form-control" name="datasetOffered[<%= number %>][name]" required />
        </div>
        <div class="col-md-4 dataset-format">
            <input type="text" placeholder="Format" class="form-control" name="datasetOffered[<%= number %>][format]" />
        </div>
        <div class="col-md-4 dataset-value">
            <input type="text" placeholder="Estimated Size" class="form-control" name="datasetOffered[<%= number %>][size]" />
        </div>
    </div>
    <textarea rows="3" placeholder="Description" class="form-control dataset-description" name="datasetOffered[<%= number %>][description]"></textarea>
    <a class="btn btn-danger dataset-remove" id="dataset-add">Remove Dataset</a>
</div>