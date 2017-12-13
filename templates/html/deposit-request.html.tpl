<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title="Deposit Request">
<div class="container">
    <h1>Deposit Request</h1>
    <form id="deposit-request" class="deposit-request">
        <div class="form-group">
            <label for="datasetTitle">*Dataset Title</label>
            <input required type="text" class="form-control" id="datasetTitle" placeholder="Dataset Title">
        </div>
        <div class="form-group">
            <label for="depositorName">*Depositor Name</label>
            <input required type="text" class="form-control" id="depositorName" placeholder="Depositor Name">
        </div>
        <div class="form-group">
            <label for="depositorEmail">*Depositor Email</label>
            <input required type="email" class="form-control" id="depositorEmail" placeholder="Depositor Email">
        </div>
        <div class="form-group">
            <label for="depositorOtherContact">Other Contact</label>
            <textarea class="form-control" id="depositorOtherContact" rows="3" placeholder="Other Contact"></textarea>
        </div>
        <div class="form-group">
            <label for="projectName">Project Name</label>
            <input type="text" class="form-control" id="projectName" placeholder="Project Name">
        </div>
        <div class="form-group">
            <label for="planningDocs">*Planning Documents</label>
            <select class="form-control" required id="planningDocs">
                <option value="none">None</option>
                <option value="outline">Outline</option>
                <option value="intermediate">Intermediate</option>
                <option value="full">Full</option>
                <option value="other">Other</option>
            </select>
        </div>
        <div class="form-group planning-docs-other is-inactive" id="planningDocsOther">
            <label for="planningDocsOther">Other Planning Documents</label>
            <p class="form-comment">Describe any other pre-existing planing documentation that may be relevant</p>
            <textarea class="form-control" id="planningDocsOther" rows="3" placeholder="Other Planning Documents"></textarea>
        </div>
        <div class="form-group">
            <label for="nercFunded">*Nerc Funded</label>
            <br />
            <label class="radio-inline">
                <input type="radio" name="nercFunded" id="nercFundedNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="nercFunded" id="nercFundedYes" value="yes"> Yes
            </label>
            <label class="radio-inline">
                <input type="radio" name="nercFunded" id="nercFundedPartly" value="partly"> Partly
            </label>
        </div>
        <div class="form-group">
            <label for="publicFunded">*Public Funded</label>
            <br />
            <label class="radio-inline">
                <input type="radio" name="publicFunded" id="publicFundedNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="publicFunded" id="publicFundedYes" value="yes"> Yes
            </label>
            <label class="radio-inline">
                <input type="radio" name="publicFunded" id="publicFundedPartly" value="partly"> Partly
            </label>
        </div>
        <div class="form-group">
            <label>*Dataset Offered</label>
            <div class="datasets-offered">
                <div class="dataset" id="dataset0">
                    <p><i>Dataset</i></p>
                    <a class="btn btn-danger dataset-remove">Remove Dataset</a>
                    <div class="row">
                        <div class="col-md-4 dataset-value">
                            <input type="text" placeholder="Name" class="form-control" name="datasetOfferedName0" required />
                        </div>
                        <div class="col-md-4 dataset-format">
                            <input type="text" placeholder="Format" class="form-control" name="datasetOfferedFormat0" />
                        </div>
                        <div class="col-md-4 dataset-value">
                            <input type="text" placeholder="Estimated Size" class="form-control" name="datasetOfferedSize0" />
                        </div>
                    </div>
                    <textarea rows="3" placeholder="Description" class="form-control dataset-description" name="datasetOfferedDescription0"></textarea>
                    
                </div>
            </div>
            <a class="btn btn-success" id="dataset-add">Add Dataset</a>
        </div>
        <div class="form-group">
            <label for="relatedDatasets">*Related Datasets</label>
            <br />
            <label class="radio-inline">
                <input type="radio" name="relatedDatasets" id="relatedDatasetsNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="relatedDatasets" id="relatedDatasetsYes" value="yes"> Yes
            </label>
        </div>
        <div class="form-group">
            <label for="relatedDatasetsDetail">Related Datasets Detail</label>
            <p class="form-comment">Enter DOI(s)</p>
            <textarea class="form-control" id="relatedDatasetsDetail" rows="3" placeholder="Related Datasets Detail"></textarea>
        </div>
        <div class="form-group">
            <label for="scienceDomain">*Science Domain</label>
            <select class="form-control" required id="scienceDomain">
                <option value="terrestrial">Terrestrial</option>
                <option value="ecology">Ecology</option>
                <option value="freshwater">Freshwater</option>
                <option value="ecology">Ecology</option>
                <option value="hydrology">Hydrology</option>
                <option value="environmentalBiology">Environmental Biology</option>
                <option value="other">Other</option>
            </select>
        </div>
        <div class="form-group">
            <label for="scienceDomainOther">Science Domain Other</label>
            <input class="form-control" id="scienceDomainOther" type="text" placeholder="Other"></textarea>
        </div>
        <div class="form-group">
            <label for="uniqueDeposit">*Unique Deposit</label>
            <p class="form-comment">Are there copies of the data held in another data centre?</p>
            <label class="radio-inline">
                <input type="radio" name="uniqueDeposit" id="uniqueDepositNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="uniqueDeposit" id="uniqueDepositYes" value="yes"> Yes
            </label>
        </div>
        <div class="form-group">
            <label for="modelOutput">*Model Output</label>
            <p class="form-comment">Are the data model output data that could be reproduced at minimal cost?</p>
            <label class="radio-inline">
                <input type="radio" name="modelOutput" id="modelOutputNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="modelOutput" id="modelOutputYes" value="yes"> Yes
            </label>
        </div>
        <div class="form-group">
            <label for="publishedPaper">*Published Paper</label>
            <p class="form-comment">Have the data been used in a peer-reviewed, published journal paper?</p>
            <label class="radio-inline">
                <input type="radio" name="publishedPaper" id="publishedPaperNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="publishedPaper" id="publishedPaperYes" value="yes"> Yes
            </label>
        </div>
        <div class="form-group">
            <label for="reusable">*Reusable</label>
            <p class="form-comment">Can the data be sufficiently described to make it re-usable?</p>
            <label class="radio-inline">
                <input type="radio" name="reusable" id="reusableNo" value="no" required> No
            </label>
            <label class="radio-inline">
                <input type="radio" name="reusable" id="reusableYes" value="yes"> Yes
            </label>
        </div>
        <button type="submit" class="btn btn-default">Save</button>
        <button type="submit" class="btn btn-default">Finish</button>
    </form>
</div>
</@skeleton.master>