<#if areaOfStudy??>
    <div id="section-extent">
        <dl>
            <#if areaOfStudy?has_content>
                <dt>Study area</dt>
                <dd>
                    <div id="studyarea-map">
                        <#list areaOfStudy as extent>
                            <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
                        </#list>
                    </div>
                </dd>
            </#if>
        </dl>
    </div>
</#if>