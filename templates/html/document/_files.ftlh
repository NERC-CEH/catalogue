<#if files?has_content && files?size gt 0 >
    <div id="document-files">
        <p id="files"><b>Files</b><#if files?size gt 1>s</#if></p>

        <#list files>
            <#assign filesMaxSize = 30 >
            <#assign moreFiles = files?size-filesMaxSize >
            <#items as file>
                <#assign fileClass="file">
                <#if file?counter gt filesMaxSize>
                    <#assign fileClass="file fileCollapse collapse">
                </#if>

                <div class="${fileClass}">
                    <#if file.name?has_content>
                    <div>
                        <p>Name: ${file.name}</p>
                    </div>
                    </#if>
                    <#if file.format?has_content>
                    <div>
                        <p>Format: ${file.format}</p>
                    </div>
                    </#if>
                    <#if file.size?has_content>
                    <div>
                        <p>Size(MB): ${file.size}</p>
                    </div>
                    </#if>
                </div>

            </#items>

            <#if files?size gt filesMaxSize >
                <p>
                    <small>
                        <#if file.name?has_content>
                        <div>
                            <p>Name: ${file.name}</p>
                        </div>
                        </#if>
                        <#if file.format?has_content>
                        <div>
                            <p>Format: ${file.format}</p>
                        </div>
                        </#if>
                        <#if file.size?has_content>
                        <div>
                            <p>Size(MB): ${file.size}</p>
                        </div>
                        </#if>
                        <a href=".fileCollapse" class="moreFiles collapsed" data-toggle="collapse" aria-controls="filePartyCollapse" area-expanded="false">
                            <span>${moreFiles} more files. </span>Show
                        </a>
                    </small>
                </p>
            </#if>
        </#list>

    </div>
</#if>
