<#-- The files named in the agreement, with the size from bytes when known and else as typed. -->
<#list files>
<p>Files to be deposited</p>
<div class="tbl tbl--collapse tbl--files">
    <table>
        <thead>
            <tr>
                <th>Path</th>
                <th>Format</th>
                <th>Size</th>
            </tr>
        </thead>
        <tbody>
        <#items as file>
            <tr>
                <td><#if file.name?has_content>${file.name}<#else><span class="text-danger">missing</span></#if></td>
                <td><#if file.format?has_content>${file.format}<#if file.formatType?has_content> <span class="text-muted">${file.formatType}</span></#if><#else><span class="text-danger">missing</span></#if></td>
                <td><#if file.bytes??>${fileSizes.format(file.bytes)}<#elseif file.size?has_content>${file.size}<#else><span class="text-danger">missing</span></#if></td>
            </tr>
        </#items>
        </tbody>
    </table>
</div>
</#list>
