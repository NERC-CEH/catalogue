import baseTemplate from './DepositRequestBase'

const template = `
    <div class="text-center mt-5 alert alert-danger" role="alert" style="display: none;"></div>
    <p><span class="text-danger">*</span> indicates required information</p>
    <form id="deposit-request-form" class="mt-3">

      <div class="mt-3 form-floating">
        <input type="text"
                class="form-control"
                id="your-name"
                data-name="name"
                value="<%= model.name %>">
        <label for="your-name">
          Your name <span class="text-danger">*</span>
        </label>
        <div class="invalid-feedback"></div>
      </div>
      <div class="mt-3 form-floating">
        <input type="email"
                class="form-control"
                id="your-email"
                data-name="email"
                value="<%= model.email %>">
        <label for="your-email">
          Your email address <span class="text-danger">*</span>
        </label>
        <div class="invalid-feedback"></div>
      </div>
      <div class="mt-3 form-floating">
        <input type="text"
                class="form-control"
                id="your-affiliation"
                data-name="affiliation"
                value="<%= model.affiliation %>">
        <div class="invalid-feedback"></div>
        <label for="your-affiliation">
          Your affiliation <span class="text-danger">*</span>
        </label>
      </div>

      <hr class="my-5 border border-secondary border-2">

      <div>
        <h2 class="my-3">Your data</h2>
        <div class="my-2">
          <label class="form-label d-block">
            Who funded the research that produced this data/resource? <span class="text-danger">*</span>
            <div class="text-body-tertiary">If there are a number of funders, choose 'Other' and specify</div>
          </label>
          <select class="form-select" data-name="funder">
            <option value="">Select funder</option>
            <option value="BBSRC" <%= model.funder === 'BBSRC' ? 'selected' : '' %>>BBSRC</option>
            <option value="NERC" <%= model.funder === 'NERC' ? 'selected' : '' %>>NERC</option>
            <option value="STFC" <%= model.funder === 'STFC' ? 'selected' : '' %>>STFC</option>
            <option value="Other" <%= model.funder === 'Other' ? 'selected' : '' %>>Other</option>
          </select>
          <div class="invalid-feedback"></div>
        </div>
        <div class="my-2">
          <input type="text"
                  class="form-control"
                  data-name="funderOther"
                  style="<%= model.funder === 'Other' ? '' : 'display: none;' %>"
                  placeholder="Specify funder(s)"
                  value="<%= model.funderOther || '' %>">
          <div class="invalid-feedback"></div>
        </div>
        <div class="my-5">
          <label class="control-label">
            Please include any grant/funding reference(s), if known
          </label>
          <input type="text"
                  class="form-control"
                  data-name="fundingRef"
                  value="<%= model.fundingRef || '' %>"
                  placeholder="e.g. NE/X87234/1">
        </div>
        <div class="my-5">
          <label class="form-label d-block">
            Are the data within the remit of the EIDC (i.e., environmental data from the terrestrial and freshwater sciences?)
            <div class="text-body-tertiary">For more details on the data we take in, please consult <a href="https://eidc.ac.uk/policies/acquisition" target="_blank" rel="noopener noreferrer"> our Acquisition Policy</a></div>
          </label>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="eidcRemit"
                    data-name="eidcRemit"
                    id="eidcRemitYes"
                    value="Yes"
                    <%= model.eidcRemit === 'Yes' ? 'checked' : '' %>>
            <label class="form-check-label" for="eidcRemitYes">Yes</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="eidcRemit"
                    data-name="eidcRemit"
                    id="eidcRemitNo"
                    value="No"
                    <%= model.eidcRemit === 'No' ? 'checked' : '' %>>
            <label class="form-check-label" for="eidcRemitNo">No</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="eidcRemit"
                    data-name="eidcRemit"
                    id="eidcRemitSome"
                    value="Some are"
                    <%= model.eidcRemit === 'Some are' ? 'checked' : '' %>>
            <label class="form-check-label" for="eidcRemitSome">Some are</label>
          </div>
          <div>
            <input class="d-none"
                    type="radio"
                    name="eidcRemit"
                    data-name="eidcRemit"
                    id="eidcRemitNone">
            <label class="d-none" >None</label>
            <div class="invalid-feedback"></div>
          </div>
        </div>
        <div class="my-5">
          <label class="form-label d-block">
            Does the data resource contain 'omic or social science data, or is it model code that is not accompanied by output data?
            <div class="text-body-tertiary">We may suggest an alternative repository for this data (<a href="https://eidc.ac.uk/approvedrepositories" target="_blank" rel="noopener noreferrer">see https://eidc.ac.uk/approvedrepositories</a>)</div>
          </label>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="alternativeData"
                    data-name="alternativeData"
                    id="alternativeDataYes"
                    value="Yes"
                    <%= model.alternativeData === 'Yes' ? 'checked' : '' %>>
            <label class="form-check-label" for="alternativeDataYes">Yes</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="alternativeData"
                    data-name="alternativeData"
                    id="alternativeDataNo"
                    value="No"
                    <%= model.alternativeData === 'No' ? 'checked' : '' %>>
            <label class="form-check-label" for="alternativeDataNo">No</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="alternativeData"
                    data-name="alternativeData"
                    id="alternativeDataSome"
                    value="Some are"
                    <%= model.alternativeData === 'Some are' ? 'checked' : '' %>>
            <label class="form-check-label" for="alternativeDataSome">Some are</label>
          </div>
          <div>
            <input class="d-none"
                    type="radio"
                    name="alternativeData"
                    data-name="alternativeData"
                    id="alternativeDataNone">
            <label class="d-none" >None</label>
            <div class="invalid-feedback"></div>
          </div>
        </div>
        <div class="my-5">
          <label class="form-label d-block">
            We are unable to accept data resources without supporting documentation to enable their re-use. Are you able to provide document(s) to enable re-use of the resource? <span class="text-danger">*</span>
            <div class="text-body-tertiary">Supporting documentation helps others to understand a data resource and supports its re-use.  The information is likely to already exist; for example in technical reports, project websites or wikis.  <a href="https://eidc.ac.uk/deposit/supportingDocumentation" target="_blank" rel="noopener noreferrer">See our guidance</a> for further information.</div>
          </label>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="hasSupportingDocs"
                    data-name="hasSupportingDocs"
                    id="hasSupportingDocsYes"
                    value=true
                    <%= model.hasSupportingDocs === true ? 'checked' : '' %>>
            <label class="form-check-label" for="hasSupportingDocsYes">Yes</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="hasSupportingDocs"
                    data-name="hasSupportingDocs"
                    id="hasSupportingDocsNo"
                    value=false
                    <%= model.hasSupportingDocs === false ? 'checked' : '' %>>
            <label class="form-check-label" for="hasSupportingDocsNo">No</label>
          </div>
          <div>
            <input class="d-none"
                    type="radio"
                    name="hasSupportingDocs"
                    data-name="hasSupportingDocs"
                    id="hasSupportingDocsNone">
            <label class="d-none" >None</label>
            <div class="invalid-feedback"></div>
          </div>
        </div>
        <div class="my-5">
          <label class="form-label d-block">
            Are the data and supporting documentation <a href="https://eidc.ac.uk/deposit/ready" target="_blank" rel="noopener noreferrer">ready to deposit</a> and prepared according to <a href="https://eidc.ac.uk/deposit/preparingData" target="_blank" rel="noopener noreferrer">our guidance</a>?
            <div class="text-body-tertiary">If data and supporting documentation are not prepared according to our guidance your deposit may take longer</div>
          </label>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="isSupportingDocsReady"
                    data-name="isSupportingDocsReady"
                    id="isSupportingDocsReadyYes"
                    value=true
                    <%= model.isSupportingDocsReady === true ? 'checked' : '' %>>
            <label class="form-check-label" for="isSupportingDocsReadyYes">Yes</label>
          </div>
          <div class="form-check form-check-inline">
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
        <div class="my-5">
          <label class="form-label d-block">
            Are these resource(s) replacing those already held by the EIDC? <span class="text-danger">*</span>
          </label>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="replaceExisting"
                    data-name="replaceExisting"
                    id="replaceExistingYes"
                    value=true
                    <%= model.replaceExisting === true ? 'checked' : '' %>>
            <label class="form-check-label" for="replaceExistingYes">Yes</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input"
                    type="radio"
                    name="replaceExisting"
                    data-name="replaceExisting"
                    id="replaceExistingNo"
                    value=false
                    <%= model.replaceExisting === false ? 'checked' : '' %>>
            <label class="form-check-label" for="replaceExistingNo">No</label>
          </div>
          <div>
            <input class="d-none"
                    type="radio"
                    name="replaceExisting"
                    data-name="replaceExisting"
                    id="replaceExistingNone">
            <label class="d-none">None</label>
            <div class="invalid-feedback"></div>
          </div>
        </div>
      <div class="my-5">
        <label class="form-label d-block">
          Are these resource(s) related to those already held by the EIDC? <span class="text-danger">*</span>
        </label>
        <div class="form-check form-check-inline">
          <input class="form-check-input"
                  type="radio"
                  name="relatedToExisting"
                  data-name="relatedToExisting"
                  id="relatedToExistingYes"
                  value=true
                  <%= model.relatedToExisting === true ? 'checked' : '' %>>
          <label class="form-check-label" for="relatedToExistingYes">Yes</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input"
                  type="radio"
                  name="relatedToExisting"
                  data-name="relatedToExisting"
                  id="relatedToExistingNo"
                  value=false
                  <%= model.relatedToExisting === false ? 'checked' : '' %>>
          <label class="form-check-label" for="relatedToExistingNo">No</label>
        </div>
        <div>
          <input class="d-none"
                  type="radio"
                  name="relatedToExisting"
                  data-name="relatedToExisting"
                  id="relatedToExistingNone">
          <label class="d-none">None</label>
          <div class="invalid-feedback"></div>
        </div>
      </div>

      <hr class="my-5 border border-secondary border-2">

      <div>
        <h2 class="my-3">Datasets</h2>
        <p>You can include one or more datasets in this deposit request.</p>
        <p>A dataset is a structured collection of data organised and stored together.  Data within a dataset are typically related in some way.  It can include different types or formats of data and may be comprised of one or many files.</p>
        <p>To decide if you have one dataset or many, think about how you will describe the data and what the supporting documentation will consist of.  If the metadata is similar or identical for each dataset, it indicates that you probably have <span class="fw-bold">one</span> dataset.  If it is different, you probably have multiple datasets.</p>
        <p>If you need help in deciding whether your deposit consists of one or several resources, please get in touch by emailing <a href="mailto:info@eidc.ac.uk">info@eidc.ac.uk</a></p>
        <p>Add details about each dataset you want to deposit by clicking the button below.</p>

        <div class="mt-3 resource-list" data-name="dataResources"></div>
        <div class="text-center invalid-feedback"></div>
        <div class="mt-3 d-flex">
          <button type="button" class="add-resource btn btn-primary">Add a dataset</button>
        </div>
      </div>

      <hr class="my-5 border border-secondary border-2">

      <div class="mt-3">
        <div class="form-group">
          <label class="form-label">
            <span class="fw-bold">Additional information</span><br>
            <span class="text-body-tertiary">If there is any other information you'd like to add (e.g. details of data management plans), please do so here.</span>
          </label>
          <textarea class="form-control"
                    data-name="additionalInfo"
                    placeholder="Enter any additional information here..."><%= model.additionalInfo || '' %></textarea>
        </div>
      </div>

      <div class="card fw-bold mt-5">
        <div class="card-body">
          <p>To proceed, please confirm that for each data resource offered:</p>
          <ul>
            <li>you are the owner, or have permission from the owner to publish it</li>
            <li>any third-party data used in its generation or derivation were <u>not</u> accessed under terms and conditions which would preclude its subsequent publication</li>
            <li>those responsible for funding it, permit its publication by the EIDC</li>
            <li>it was generated by research that had all relevant ethical approvals, and adhered to relevant institutional and/or funder requirements</li>
          </ul>

          <div class="form-check mt-3 mx-4">
            <input class="form-check-input"
                    type="checkbox"
                    name="isAgreed"
                    data-name="isAgreed"
                    id="isAgreed"
                    value="Yes"
                    <%= model.isAgreed ? 'checked' : '' %>>
            <label class="form-check-label" for="isAgreed">
              I CONFIRM
            </label>
            <span class="text-danger">*</span>
            <div class="invalid-feedback"></div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-center mt-3 pb-5">
        <button type="submit" class="btn btn-outline-dark btn-submit">Submit</button>
      </div>
    </form>
`

export default baseTemplate(template)
