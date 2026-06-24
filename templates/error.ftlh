<#import "html/skeleton.ftlh" as skeleton>
<#--
  Branded, status-aware replacement for Spring Boot's Whitelabel error page.
  Resolved as the fallback error view (name "error") once the Whitelabel
  StaticView is disabled via spring.web.error.whitelabel.enabled=false.
  Uses only the always-present error model attributes (status, path,
  timestamp); message/trace are deliberately omitted so internal details
  are never leaked to users.
-->
<@skeleton.master title="Error" catalogue=catalogues.defaultCatalogue()>
  <div id="metadata" class="metadata container text-center mt-5">
    <#assign code = (status!0)?number>
    <#if code == 404>
      <h1><i class="fa-solid fa-compass"></i> Page not found</h1>
      <p>We couldn't find the page you were looking for. It may have moved or no longer exist.</p>
    <#elseif code gte 500>
      <h1><i class="fa-solid fa-triangle-exclamation"></i> Something went wrong</h1>
      <p>An unexpected error occurred on our end. Please try again shortly.</p>
    <#else>
      <h1><i class="fa-solid fa-circle-exclamation"></i> Sorry, an error occurred</h1>
      <p>Your request could not be completed.</p>
    </#if>
    <p class="mt-4"><a href="/" class="btn btn-primary">Return to the catalogue</a></p>
    <p class="text-muted small mt-3">
      Reference: ${status!"?"}<#if (path!"")?has_content> &middot; ${path}</#if><#if timestamp??> &middot; ${timestamp?string("yyyy-MM-dd HH:mm:ss")}</#if>
    </p>
  </div>
</@skeleton.master>
