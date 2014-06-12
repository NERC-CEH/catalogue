<html>
  <head>
    <title>Woot, the template has been loaded and processed</title>
  </head>
<body data-target="#scrollspy" data-spy="scroll">
<div class="container">
   <div class="navbar navbar-inverse navbar-static-top">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="#"><img src="http://www.ceh.ac.uk/images/logos/header_logo.png" alt="CEH Logo"></a></li>
          </ul>
          
	 <ul class="nav navbar-nav navbar-right">
      <li><a href="#"><i></i></a></li>
      <li><a href="#">Help</a></li>
      <li>
        <a href="#">Publish <b></b></a>
      </li>
      <li>
        <a href="#">Admin <b></b></a>
      </li>
      <li>
        <a href="#"><i class="glyphicon glyphicon-user"></i> Username</a>
        <ul>
          <li><a href="#">CHANGE PASSWORD</a></li>
          <li><a href="#">LOGOUT</a></li>
        </ul>
      </li>
    </ul>          
          
        </div>
          
   </div>
</div>

<div class="container">
<!-- InstanceBeginEditable name="MAIN" -->
<div about="http://data.ceh.ac.uk/id/2a742df-3772-481a-97d6-0de5133f4812">

    <div class="page-header">
		<h2>${title}</h2> 			
    </div>
</div>
</div>

<div>
<#if topicCategories?has_content>
<p>Topic category:</p>
	<#list topicCategories as topics>
		${topics}
	</#list>
</#if>

</div>

<div>
<p>Keywords</p>
<ul>
	
	<#list descriptiveKeywords as keywordsList>
  
        <#list keywordsList.keywords as keyword>
            <li>${keyword.value}</li>
        </#list>
	</#list>
	
</ul>
</div>
<div>
<p>Language:</p>
<#if datasetLanguage?has_content>
<p>${datasetLanguage.codeList}</p>
<p>${datasetLanguage.value}</p>
</#if>

<div>
<#if alternateTitles?has_content>
<p>Alternate title:
<#list alternateTitles as atitles>
	${atitles}</p>
</#list>
</#if>
</div>

<div>
<#if metadata?has_content>
<p>Metadata: ${metadata}</p>

</#if>
</div>

<h2>${id}</h2>
</div>
</body>
<!-- InstanceEnd -->

</html>