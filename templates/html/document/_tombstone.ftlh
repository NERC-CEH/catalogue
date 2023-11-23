<#import "../../functions.ftlh" as func>
<#assign superseded=jena.superseded(uri) >
<section>
    <div class="container">
        <#include "_admin.ftlh">
        <div class="tombstone">
            <h1><#if title?has_content><i>${title}</i><#else>This record</#if>  has been deleted</h1>

            <p>
            <#if citation.url?has_content && superseded?has_content>
                The resource with the identifier ${citation.url} has been replaced by 
                <a href="${superseded?first.href}">${superseded?first.title}</a>
            <#else>
                Please <a href="https://eidc.ac.uk/contact" target="_blank" rel="noopener noreferrer">contact the EIDC</a> if you require more information
            </#if>
            </p>

        </div>
    </div>
</section>