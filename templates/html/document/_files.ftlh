<#if files?size gt 0 >
    <div id="document-files">
        <h2 id="files">Files<#if files?size gt 1>s</#if></h2>

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
                        ${file.name}
                    </div>
                    </#if>
                    <#if file.format?has_content>
                    <div>
                        ${file.format}
                    </div>
                    </#if>
                    <#if file.size?has_content>
                    <div>
                        ${file.size}
                    </div>
                    </#if>
                </div>

            </#items>

            <#if files?size gt filesMaxSize >
                <p>
                    <small>
                        <#if file.name?has_content>
                        <div>
                            ${file.name}
                        </div>
                        </#if>
                        <#if file.format?has_content>
                        <div>
                            ${file.format}
                        </div>
                        </#if>
                        <#if file.size?has_content>
                        <div>
                            ${file.size}
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
