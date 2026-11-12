<#-- The groups of files described by a pattern, and any naming convention an older agreement still carries. -->
<#if fileGroups?has_content>
    <p>File groups to be deposited</p>
    <div class="tbl tbl--collapse tbl--files">
        <table>
            <thead>
                <tr>
                    <th>Pattern</th>
                    <th>Number of files</th>
                    <th>Format</th>
                    <th>Size</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
            <#list fileGroups as group>
                <tr>
                    <td><#if group.pattern?has_content>${group.pattern}<#else><span class="text-danger">missing</span></#if></td>
                    <td><#if group.count??>${group.count?c}<#else><span class="text-danger">missing</span></#if></td>
                    <td>${group.format!""}<#if group.formatType?has_content> <span class="text-muted">${group.formatType}</span></#if></td>
                    <td>
                        <#if group.minBytes?? && group.maxBytes?? && group.minBytes != group.maxBytes>
                            ${fileSizes.format(group.minBytes)} – ${fileSizes.format(group.maxBytes)}
                        <#elseif group.minBytes?? || group.maxBytes??>
                            ${fileSizes.format(group.minBytes!group.maxBytes)}
                        <#else>
                            ${group.size!""}
                        </#if>
                        <#if group.totalBytes??><div class="text-muted">${fileSizes.format(group.totalBytes)} in total</div></#if>
                    </td>
                    <td>${group.description!""}</td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</#if>

<#if fileNamingConvention?has_content>
    <div class="row key-value">
        <div class="key">Previous naming convention</div>
        <div class="value">
            <#list fileNamingConvention?split("\n") as line>
                ${line}<#sep><br></#sep>
            </#list>
        </div>
    </div>
</#if>
