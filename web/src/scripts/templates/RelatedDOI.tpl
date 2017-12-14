<div class="row doi">
    <div class="col-md-10 doi-value">
        <input name="relatedDatasets[<%= number =>]" class="form-control" type="text" placeholder="DOI" pattern="^(http(s|):\/\/(dx\.|)doi\.org\/|)10\.5285\/[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}$" required oninvalid="this.setCustomValidity('Not a valid DOI')" oninput="this.setCustomValidity('')"/>
    </div>
    <a class="btn btn-danger col-md-2 remove-doi">Remove DOI</a>
</div>