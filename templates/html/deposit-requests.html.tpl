<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Deposit Requests">
    <div class="container">
        <h1>Deposit Requests</h1>
        <a href="/deposit-request" class="btn btn-success">Create Deposit Request</a>
        <div class="context"> 
            <h1 class="title">Your Depsoit Requests</h1> 
            <h2><span>${depositRequests?size}</span> Depsoit Requests</h2> 
            <div class="list-group">
            <#list depositRequests as depositRequest>
                <a href="/deposit-request/${depositRequest.id}" class="list-group-item">
                    <h4 class="list-group-item-heading"><b>Title</b> ${depositRequest.title}</h4>
                </a>
            </#list>

            
            <#if allDepositRequests??>
            <h1 class="title">All Depsoit Requests</h1> 
            <h2><span>${allDepositRequests?size}</span> Depsoit Requests</h2> 
            <div class="list-group">
                <#list allDepositRequests as depositRequest>
                    <a href="/deposit-request/${depositRequest.id}" class="list-group-item">
                        <h4 class="list-group-item-heading"><b>Title</b> ${depositRequest.title}</h4>
                        <b>Email</b> <span>${depositRequest.depositorEmail}</span>
                    </a>
                </#list>
                </div>
            </#if>
            
        </div>
    </div>
</@skeleton.master>