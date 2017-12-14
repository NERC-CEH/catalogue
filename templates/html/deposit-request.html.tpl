<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Deposit Request">
    <div class="container">
        <h1>Deposit Request</h1>
        <form id="deposit-request" class="deposit-request">
            <div class="form-group">
                <label>*Dataset Title</label>
                <input required type="text" class="form-control" name="datasetTitle" id="datasetTitle" placeholder="Dataset Title">
            </div>
            <div class="form-group">
                <label>*Depositor Name</label>
                <input required type="text" class="form-control" name="depositorName" id="depositorName" placeholder="Depositor Name">
            </div>
            <div class="form-group">
                <label>*Depositor Email</label>
                <input required type="email" class="form-control" name="depositorEmail" id="depositorEmail" placeholder="Depositor Email">
            </div>
            <div class="form-group">
                <label>Other Contact</label>
                <textarea class="form-control" name="depositorOtherContact" id="depositorOtherContact" rows="3" placeholder="Other Contact"></textarea>
            </div>
            <div class="form-group">
                <label>Project Name</label>
                <input type="text" class="form-control" name="projectName" id="projectName" placeholder="Project Name">
            </div>
            <div class='planning-documents'>
                <div class="form-group">
                    <label>*Planning Documents</label>
                    <select class="form-control" required name="planningDocs" id="planningDocs">
                        <option value=""></option>
                        <option value="none">None</option>
                        <option value="outline">Outline</option>
                        <option value="intermediate">Intermediate</option>
                        <option value="full">Full</option>
                        <option value="other">Other</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <div>
                    <label>*Nerc Funded</label>
                </div>
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
                <div>
                    <label>*Public Funded</label>
                </div>
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
                <div>
                    <a class="btn btn-success dataset-add" id="dataset-add">Add Dataset</a>
                </div>
                <div class="datasets-offered">
                    <div class="dataset" id="dataset0">
                        <div class="row">
                            <div class="col-md-4 dataset-value">
                                <input type="text" placeholder="Name" class="form-control" name="datasetOffered[0][name]" required />
                            </div>
                            <div class="col-md-4 dataset-format">
                                <input type="text" placeholder="Format" class="form-control" name="datasetOffered[0][format]" />
                            </div>
                            <div class="col-md-4 dataset-value">
                                <input type="text" placeholder="Estimated Size" class="form-control" name="datasetOffered[0][size]" />
                            </div>
                        </div>
                        <textarea rows="3" placeholder="Description" class="form-control dataset-description" name="datasetOffered[0][description]"></textarea>
                        <a class="btn btn-danger dataset-remove">Remove Dataset</a>
                    </div>
                </div>
            </div>
            <div id="relatedDatasets">
                <div class="form-group">
                    <div>
                        <label>*Related Datasets</label>
                    </div>
                    <label class="radio-inline">
                        <input type="radio" name="hasRelatedDatasets" id="relatedDatasetsNo" value="false" required> No
                    </label>
                    <label class="radio-inline">
                        <input type="radio" name="hasRelatedDatasets" id="relatedDatasetsYes" value="true"> Yes
                    </label>
                </div>
            </div>
            <div id="science-domain">
                <div class="form-group">
                    <label>*Science Domain</label>
                    <select class="form-control" required name="scienceDomain" id="scienceDomain">
                        <option value=""></option>
                        <option value="terrestrial">Terrestrial</option>
                        <option value="ecology">Ecology</option>
                        <option value="freshwater">Freshwater</option>
                        <option value="ecology">Ecology</option>
                        <option value="hydrology">Hydrology</option>
                        <option value="environmentalBiology">Environmental Biology</option>
                        <option value="other">Other</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>*Unique Deposit</label>
                <p class="form-comment">Are there copies of the data held in another data centre?</p>
                <label class="radio-inline">
                    <input type="radio" name="uniqueDeposit" id="uniqueDepositNo" value="false" required> No
                </label>
                <label class="radio-inline">
                    <input type="radio" name="uniqueDeposit" id="uniqueDepositYes" value="true"> Yes
                </label>
            </div>
            <div class="form-group">
                <label>*Model Output</label>
                <p class="form-comment">Are the data model output data that could be reproduced at minimal cost?</p>
                <label class="radio-inline">
                    <input type="radio" name="modelOutput" id="modelOutputNo" value="false" required> No
                </label>
                <label class="radio-inline">
                    <input type="radio" name="modelOutput" id="modelOutputYes" value="true"> Yes
                </label>
            </div>
            <div class="form-group">
                <label>*Published Paper</label>
                <p class="form-comment">Have the data been used in a peer-reviewed, published journal paper?</p>
                <label class="radio-inline">
                    <input type="radio" name="publishedPaper" id="publishedPaperNo" value="false" required> No
                </label>
                <label class="radio-inline">
                    <input type="radio" name="publishedPaper" id="publishedPaperYes" value="true"> Yes
                </label>
            </div>
            <div class="form-group">
                <label>*Reusable</label>
                <p class="form-comment">Can the data be sufficiently described to make it re-usable?</p>
                <label class="radio-inline">
                    <input type="radio" name="reusable" id="reusableNo" value="false" required> No
                </label>
                <label class="radio-inline">
                    <input type="radio" name="reusable" id="reusableYes" value="true"> Yes
                </label>
            </div>
            <button type="submit" class="btn btn-primary">Submit</button>
        </form>
    </div>
</@skeleton.master>