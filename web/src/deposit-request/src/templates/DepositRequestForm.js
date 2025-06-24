import baseTemplate from './DepositRequestBase'

const template = `
  <div class="mx-4 mt-5">
    <div class="text-center mb-5 alert alert-danger" role="alert" style="display: none;"></div>
    <form id="deposit-request-form" class="p-5 form-horizontal">
      <div class="row">
        <div class="col-md-2 fw-bold">
          Your details
        </div>
        <div class="col-md-10">
          <div class="form-group mb-4">
            <label class="control-label">
              Your name <span class="text-danger">*</span>
            </label>
            <input type="text"
                   class="form-control text-dark"
                   data-name="name"
                   value="<%= model.name %>"
                   placeholder="Enter your name">
            <div class="invalid-feedback"></div>
          </div>
          <div class="form-group mb-4">
            <label class="control-label">
              Your email address <span class="text-danger">*</span>
            </label>
            <input type="email"
                   class="form-control text-dark"
                   data-name="email"
                   value="<%= model.email %>"
                   placeholder="Enter your email">
            <div class="invalid-feedback"></div>
          </div>
          <div class="form-group mb-4">
            <label class="control-label">
              Your Affiliation <span class="text-danger">*</span>
            </label>
            <input type="text"
                   class="form-control text-dark"
                   data-name="affiliation"
                   value="<%= model.affiliation %>"
                   placeholder="Your university or organisation">
            <div class="invalid-feedback"></div>
          </div>
        </div>
      </div>
      <hr class="my-4">
      <div class="row mb-4">
        <div class="col-md-2 fw-bold">
          Agreement
        </div>
        <div class="col-md-10">
          <p>By proceeding, you confirm that you are the owner of the data resource(s) or have permission from the owner to publish.</p>
          <p>You also agree that any third party data used in the generation or derivation of the resource(s) were not accessed under terms and conditions which would preclude their subsequent publication.</p>
          <p>You also agree that those responsible for funding the generation of these resources, permit their publication by the EIDC. <span class="text-danger">*</span></p>
          <div class="form-check mt-4">
            <input class="form-check-input"
                   type="checkbox"
                   name="isAgreed"
                   data-name="isAgreed"
                   id="isAgreed"
                   value="Yes"
                   <%= model.isAgreed ? 'checked' : '' %>>
            <label class="form-check-label" for="isAgreed">
              I AGREE
            </label>
            <div class="invalid-feedback"></div>
          </div>
        </div>
      </div>
      <hr class="my-4">
      <div class="row mb-4">
        <div class="col-md-2 fw-bold">
          Data outline
        </div>
        <div class="col-md-10">
          <div class="form-group">
            <label class="form-label d-block">
              Who funded the research that produced this data/resource? <span class="text-danger">*</span>
              <div class="fst-italic text-muted small">If there are a number of funders, choose 'Other' and specify</div>
            </label>
            <select class="form-select text-dark" data-name="funder">
              <option value="">Select your funder</option>
              <option value="BBSRC" <%= model.funder === 'BBSRC' ? 'selected' : '' %>>BBSRC</option>
              <option value="NERC" <%= model.funder === 'NERC' ? 'selected' : '' %>>NERC</option>
              <option value="STFC" <%= model.funder === 'STFC' ? 'selected' : '' %>>STFC</option>
              <option value="Other" <%= model.funder === 'Other' ? 'selected' : '' %>>Other</option>
            </select>
            <div class="invalid-feedback"></div>
          </div>
          <div class="form-group mt-2">
            <input type="text"
                   class="form-control"
                   data-name="funderOther"
                   style="<%= model.funder === 'Other' ? '' : 'display: none;' %>"
                   placeholder="Specify funder(s)"
                   value="<%= model.funderOther || '' %>">
            <div class="invalid-feedback"></div>
          </div>
          <div class="form-group mt-4 mb-4">
            <label class="control-label">
              Please include any grant/funding reference(s), if known
            </label>
            <input type="text"
                   class="form-control text-dark"
                   data-name="fundingRef"
                   value="<%= model.fundingRef || '' %>"
                   placeholder="e.g. NE/X87234/1">
          </div>
          <div class="form-group mb-4">
            <label class="form-label d-block">
              Are the data within the remit of the EIDC i.e. environmental data from the terrestrial and freshwater sciences?
              <div class="fst-italic text-muted small">For more details on the data we take in, please consult our Acquisition Policy <a href="https://eidc.ac.uk/policies/acquisition" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/policies/acquisition</a></div>
            </label>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="eidcRemit"
                     data-name="eidcRemit"
                     id="eidcRemitYes"
                     value="Yes"
                     <%= model.eidcRemit === 'Yes' ? 'checked' : '' %>>
              <label class="form-check-label" for="eidcRemitYes">Yes</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="eidcRemit"
                     data-name="eidcRemit"
                     id="eidcRemitNo"
                     value="No"
                     <%= model.eidcRemit === 'No' ? 'checked' : '' %>>
              <label class="form-check-label" for="eidcRemitNo">No</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="eidcRemit"
                     data-name="eidcRemit"
                     id="eidcRemitSome"
                     value="Some are"
                     <%= model.eidcRemit === 'Some are' ? 'checked' : '' %>>
              <label class="form-check-label" for="eidcRemitSome">Some are</label>
            </div>
          </div>
          <div class="form-group mb-4">
            <label class="form-label d-block">
              Are the data, omic or social science data, or model code unaccompanied by output data?
              <div class="fst-italic text-muted small">We may suggest an alternative repository for this data <a href="https://eidc.ac.uk/approvedrepositories" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/approvedrepositories</a></div>
            </label>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="alternativeData"
                     data-name="alternativeData"
                     id="alternativeDataYes"
                     value="Yes"
                     <%= model.alternativeData === 'Yes' ? 'checked' : '' %>>
              <label class="form-check-label" for="alternativeDataYes">Yes</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="alternativeData"
                     data-name="alternativeData"
                     id="alternativeDataNo"
                     value="No"
                     <%= model.alternativeData === 'No' ? 'checked' : '' %>>
              <label class="form-check-label" for="alternativeDataNo">No</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="alternativeData"
                     data-name="alternativeData"
                     id="alternativeDataSome"
                     value="Some are"
                     <%= model.alternativeData === 'Some are' ? 'checked' : '' %>>
              <label class="form-check-label" for="alternativeDataSome">Some are</label>
            </div>
          </div>
          <div class="form-group mb-4">
            <label class="form-label d-block">
              We are unable to accept data resources without supporting documentation to enable their re-use. Are you able to provide document(s) to enable re-use of the resource? <span class="text-danger">*</span>
              <div class="fst-italic text-muted small">Supporting documentation helps others understand a dataset and supports its potential re-use.  The information is likely to already exist; for example in technical reports, project websites or wikis.  See our guidance at <a href="https://eidc.ac.uk/deposit/supportingDocumentation" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit/supportingDocumentation</a> for further information.</div>
            </label>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="hasSupportingDocs"
                     data-name="hasSupportingDocs"
                     id="hasSupportingDocsYes"
                     value=true
                     <%= model.hasSupportingDocs === true ? 'checked' : '' %>>
              <label class="form-check-label" for="hasSupportingDocsYes">Yes</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="hasSupportingDocs"
                     data-name="hasSupportingDocs"
                     id="hasSupportingDocsNo"
                     value=false
                     <%= model.hasSupportingDocs === false ? 'checked' : '' %>>
              <label class="form-check-label" for="hasSupportingDocsNo">No</label>
              <div class="invalid-feedback"></div>
            </div>
          </div>
          <div class="form-group mb-4">
            <label class="form-label d-block">
              Are the data and supporting documentation prepared according to our guidance: <a href="https://eidc.ac.uk/deposit" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit</a> and ready to deposit?
              <div class="fst-italic text-muted small">If data and supporting documentation are not prepared according to our guidance your deposit may take longer</div>
            </label>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="isSupportingDocsReady"
                     data-name="isSupportingDocsReady"
                     id="isSupportingDocsReadyYes"
                     value=true
                     <%= model.isSupportingDocsReady === true ? 'checked' : '' %>>
              <label class="form-check-label" for="isSupportingDocsReadyYes">Yes</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="isSupportingDocsReady"
                     data-name="isSupportingDocsReady"
                     id="isSupportingDocsReadyNo"
                     value=false
                     <%= model.isSupportingDocsReady === false ? 'checked' : '' %>>
              <label class="form-check-label" for="isSupportingDocsReadyNo">No</label>
            </div>
          </div>
          <div class="form-group mb-4">
            <label class="form-label">
              Are these resource(s) replacing those already held by the EIDC? <span class="text-danger">*</span>
            </label>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="replaceExisting"
                     data-name="replaceExisting"
                     id="replaceExistingYes"
                     value=true
                     <%= model.replaceExisting === true ? 'checked' : '' %>>
              <label class="form-check-label" for="replaceExistingYes">Yes</label>
            </div>
            <div class="form-check">
              <input class="form-check-input"
                     type="radio"
                     name="replaceExisting"
                     data-name="replaceExisting"
                     id="replaceExistingNo"
                     value=false
                     <%= model.replaceExisting === false ? 'checked' : '' %>>
              <label class="form-check-label" for="replaceExistingNo">No</label>
              <div class="invalid-feedback"></div>
            </div>
          </div>
        <div class="form-group mb-4">
          <label class="form-label">
            Are these resource(s) related to those already held by the EIDC? <span class="text-danger">*</span>
          </label>
          <div class="form-check">
            <input class="form-check-input"
                   type="radio"
                   name="relatedToExisting"
                   data-name="relatedToExisting"
                   id="relatedToExistingYes"
                   value=true
                   <%= model.relatedToExisting === true ? 'checked' : '' %>>
            <label class="form-check-label" for="relatedToExistingYes">Yes</label>
          </div>
          <div class="form-check">
            <input class="form-check-input"
                   type="radio"
                   name="relatedToExisting"
                   data-name="relatedToExisting"
                   id="relatedToExistingNo"
                   value=false
                   <%= model.relatedToExisting === false ? 'checked' : '' %>>
            <label class="form-check-label" for="relatedToExistingNo">No</label>
            <div class="invalid-feedback"></div>
          </div>
        </div>
      </div>
      <hr class="my-4">
      <div class="row mb-4">
        <div class="col-md-2 fw-bold">
          Data resource
        </div>
        <div class="col-md-10">
          <div>
            <p class="fw-bold">This deposit request can include one or more datasets</p>
            <p>A dataset is a structured collection of data organised and stored together.  Data within a dataset is typically related in some way.  It can include different types or formats of data and may be comprised of one or many files.</p>
            <p>To decide if you have one dataset or many, think about how you will describe the data and what the supporting documentation will consist of.  If the metadata is similar or identical for each dataset, it indicates that you probably have <span class="fw-bold">one</span> dataset.  If it is different, you probably have multiple datasets.</p>
            <p>In the next section, we'll ask you a bit more about each dataset you wish to deposit.</p>
            <p><span class="text-danger">If you need help in deciding whether your deposit consists of one or many resources, please get in touch by emailing </span><a href="mailto:info@eidc.ac.uk">info@eidc.ac.uk</a></p>
          </div>
          <div class="mt-4 resource-list" data-name="dataResources"></div>
          <div class="text-center invalid-feedback"></div>
          <div class="mt-4 d-flex justify-content-center">
            <button type="button" class="fs-6 add-resource editor-button">Add dataset</button>
          </div>
        </div>
      </div>
      <hr class="my-4">
      <div class="row mb-4">
        <div class="col-md-2 fw-bold">
          Additional information
        </div>
        <div class="col-md-10">
          <div class="form-group">
            <label class="form-label">
              <p>If there is any other useful information you'd like to add (e.g. details of data management plans), please do so here.</p>
              <p>If this form was not long enough to accommodate all the datasets you wanted to add, you can include details here.</p>
            </label>
            <textarea class="form-control text-dark"
                      data-name="additionalInfo"
                      rows="4"
                      placeholder="Enter any additional information here..."><%= model.additionalInfo || '' %></textarea>
          </div>
        </div>
      </div>
      <div class="form-group">
        <div class="mt-4 text-center">
          <button type="submit" class="btn btn-primary btn-submit">Submit</button>
        </div>
      </div>
    </form>
  </div>
`

export default baseTemplate(template)
