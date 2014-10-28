<#macro master title><#compress><!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} - CEH Catalogue</title>
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    
	
	<!-- HTML5 Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script type="text/javascript" src="/static/vendor/respond/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a href="#" class="navbar-brand"><img src="/static/img/cehlogo-reverse.png" alt="CEH Logo"/></a>
        </div>

        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="http://lwisportal-staging.nerc-lancaster.ac.uk/">Home</a></li>
            <li class="active"><a href="/documents">Search Data</a></li>
            <li><a href="http://lwisportal-staging.nerc-lancaster.ac.uk/about">About</a></li>
            <li><a href="#">Contact</a></li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-user"></i> Jeanette Winterson <span class="caret"></span></a>
              <ul class="dropdown-menu">
                <li><a href="#">Profile</a></li>
                <li><a href="#">Logout</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <#nested>
    <div id="message-panel"></div>
    <script data-main="/static/scripts/main-out" src="/static/vendor/requirejs/require.js"></script>
  </body>
</html></#compress></#macro>