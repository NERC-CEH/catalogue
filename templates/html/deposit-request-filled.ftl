<#import "skeleton.ftl" as skeleton>
<@skeleton.master title="Deposit Request">
    <div class="container">
        <h1>Deposit Request</h1>
        <div class="alert alert-info">${status}</div>
        <form class="deposit-request">
            <div class="form-group">
                <label>Dataset Title</label>
                <input type="text" class="form-control" value="${depositRequest.title}" disabled />
            </div>
            <div class="form-group">
                <label>Depositor Name</label>
                <input type="text" class="form-control" value="${depositRequest.depositorName}" disabled />
            </div>
            <div class="form-group">
                <label>Depositor Email</label>
                <input type="email" class="form-control" value="${depositRequest.depositorEmail}" disabled />
            </div>
            <div class="form-group">
                <label>Other Contact</label>
                <textarea class="form-control" disabled rows="${depositorOtherContactRows}">${depositRequest.depositorOtherContact}</textarea>
            </div>
            <div class="form-group">
                <label>Project Name</label>
                <input type="text" class="form-control" value="${depositRequest.projectName}" disabled />
            </div>
            <div class="form-group">
                <label>Planning Documents</label>
                <textarea class="form-control" disabled rows="${planningDocsRows}">${depositRequest.planningDocs}</textarea>
            </div>
            <div class="form-group">
                <label>Nerc Funded</label>
                <input type="text" class="form-control" value="${depositRequest.nercFunded?cap_first}" disabled />
            </div>
            <div class="form-group">
                <label>Public Funded</label>
                <input type="text" class="form-control" value="${depositRequest.publicFunded?cap_first}" disabled />
            </div>
            <div class="form-group">
                <label>Dataset Offered</label>
                <div class="datasets-offered">
                <#list depositRequest.getDatasetsOffered() as dataset>
                    <div class="dataset">
                        <div class="row">
                            <div class="col-md-4 dataset-value">
                                <input type="text" class="form-control" disabled value="${dataset.name}" />
                            </div>
                            <div class="col-md-4 dataset-format">
                                <input type="text" class="form-control" disabled value="${dataset.type!''}" />
                            </div>
                            <div class="col-md-4 dataset-value">
                                <input type="text" class="form-control" disabled value="${dataset.size!''}" />
                            </div>
                        </div>
                        <textarea class="form-control dataset-description" disabled rows="${dataset.getDescriptionSize()}">${dataset.value!""}</textarea>
                        <#if dataset.document??>
                            <a class='btn btn-link' href="documents/${dataset.document}">
                                <i class="far fa-copy"></i>
                                <spa>Documents</span>
                            </a>
                        </#if>
                    </div>
                </#list>
                </div>
            </div>
            <#if depositRequest.hasRelatedDatasets>
                <div class="form-group">
                    <label>Related Datasets</label>
                    <#list depositRequest.getRelatedDatasets() as dataset>
                        <input type="text" class="form-control doi" value="${dataset}" disabled>
                    </#list>
                </div>
            </#if>
            <div id="science-domain">
                <div class="form-group">
                    <label>Science Domain</label>
                    <input type="text" class="form-control" value="${depositRequest.scienceDomain}" disabled>
                </div>
            </div>
            <div class="form-group">
                <label>Unique Deposit</label>
                <#if depositRequest.uniqueDeposit>
                    <input type="text" class="form-control" value="Yes" disabled />
                <#else>
                    <input type="text" class="form-control" value="No" disabled />
                </#if>
            </div>
            <div class="form-group">
                <label>Model Output</label>
                <#if depositRequest.modelOutput>
                    <input type="text" class="form-control" value="Yes" disabled />
                <#else>
                    <input type="text" class="form-control" value="No" disabled />
                </#if>
            </div>
            <div class="form-group">
                <label>Published Paper</label>
                <#if depositRequest.publishedPaper>
                    <input type="text" class="form-control" value="Yes" disabled />
                <#else>
                    <input type="text" class="form-control" value="No" disabled />
                </#if>
            </div>
            <div class="form-group">
                <label>Reusable</label>
                <#if depositRequest.reusable>
                    <input type="text" class="form-control" value="Yes" disabled />
                <#else>
                    <input type="text" class="form-control" value="No" disabled />
                </#if>
            </div>
        </form>
    </div>
</@skeleton.master>