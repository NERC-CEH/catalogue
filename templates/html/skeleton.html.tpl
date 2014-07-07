<#macro master title>
<!DOCTYPE html>
<html>
  <head>
    <title>${title} - CEH Catalogue</title>
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
    <!-- HTML5 Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>    
    <script type="text/javascript" src="/static/vendor/respond/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
    <div class="container">
      <div class="row">
        <div class="col-md-12">
         <div class="navbar navbar-default navbar-static-top">
            <div class="navbar-header">
              <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
              <a  class="navbar-brand" href="#"></a>
              <a  class="navbar-mediaSml" href="#"></a>
            </div>

          <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">             
              <li><a href="#">Help</a></li>              
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin<b class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="#">BLAH</a></li>
                  <li><a href="#">BLAH</a></li>
                </ul>
              </li>
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Username<b class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="#">CHANGE PASSWORD</a></li>
                  <li><a href="#">LOGOUT</a></li>
                </ul>
              </li>
            </ul>
          </div>
          </div>
        </div>
      </div>
    <#nested>
    </div>

    <script language="javascript" type="text/javascript" src="/static/vendor/jquery/jquery.min.js"></script>
    <script language="javascript" type="text/javascript" src="/static/vendor/bootstrap/dist/js/bootstrap.min.js"></script>
  
  </body>
</html>
</#macro>