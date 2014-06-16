<#macro master title>
<!DOCTYPE html>
<html>
  <head>
    <title>${title} - CEH Catalogue</title>
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
  </head>
  <body data-target="#scrollspy" data-spy="scroll">
    <div class="container">
      <div class="navbar navbar-static-top">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>

        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="#"><img src="/static/img/header_logo.png" alt="CEH Logo"></a></li>
          </ul>

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

    <div class="container">
      <#nested>
    </div>

    <script language="javascript" type="text/javascript" src="/static/vendor/jquery/dist/jquery.min.js"></script>
    <script language="javascript" type="text/javascript" src="/static/vendor/bootstrap/dist/js/bootstrap.min.js"></script>
  
  </body>
</html>
</#macro>