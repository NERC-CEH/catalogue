<div class="dataset" id="dataset<%= number %>">
    <a class="btn btn-danger dataset-remove" id="dataset-add">Remove Dataset</a>
    <p><i>Dataset</i></p>
    <div class="row">
        <div class="col-md-4 dataset-value">
            <input type="text" placeholder="Name" class="form-control" name="datasetOfferedName<%= number %>" required />
        </div>
        <div class="col-md-4 dataset-format">
            <input type="text" placeholder="Format" class="form-control" name="datasetOfferedFormat<%= number %>" />
        </div>
        <div class="col-md-4 dataset-value">
            <input type="text" placeholder="Estimated Size" class="form-control" name="datasetOfferedSize<%= number %>" />
        </div>
    </div>
    <textarea rows="3" placeholder="Description" class="form-control dataset-description" name="datasetOfferedDescription<%= number %>"></textarea>
</div>