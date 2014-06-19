<#macro master title>
<!DOCTYPE html>
<html>
  <head>
    <title>${title} - CEH Catalogue</title>
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
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
              <a  class="navbar-brand" href="#"><img src="/static/img/header_logo.png" alt="CEH Logo"></a>
            </div>

          <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
              <li><a href="#"><i></i></a></li>
              <li class="dropdown"><a href="#">Help<b class="caret"></b></a></li>
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Publish<b class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="#">Blah</a></li>
                  <li><a href="#">Blah</a></li>
                </ul>
              </li>
              <li class="dropdown"><a href="#">Admin<b class="caret"></b></a></li>
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
    

      <div>
        <#nested>
      </div>

    </div>

    <script language="javascript" type="text/javascript" src="/static/vendor/jquery/dist/jquery.min.js"></script>
    <script language="javascript" type="text/javascript" src="/static/vendor/bootstrap/dist/js/bootstrap.min.js"></script>
  
  </body>
</html>
</#macro>