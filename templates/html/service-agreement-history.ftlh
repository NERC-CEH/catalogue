<@skeleton.master "History">
    <h2>Service Agreement History</h2>
<#list revisions as revision>
    <form action="${revision.href}" method="get">
        <input type="submit" value="${revision.title}">
    </form>
</#list>

</@skeleton.master>