<#if licences?has_content>
    <#list licences as licence>
        <#if licence.code == 'license'>
            <p class="licenceText">
                <#assign licenceText = licence.value >
                <#if licence.uri?has_content><a rel="license" href="${licence.uri}"></#if>
                    <#if licenceText?contains("Open Government Licence")>
                        <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
                    </#if>
                    <#if licence.uri?has_content></a></#if>
            </p>
            <div class="divider"></div>
        </#if>
    </#list>
</#if>
