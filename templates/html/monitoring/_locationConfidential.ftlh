<#-- Parenthesised so the guard covers a missing `geometry`, not just a missing
     flag: FreeMarker's ?? and ! only ever cover the last step of an expression. -->
<#if (geometry.locationConfidential)!false>
  <div class="locationConfidential">The location of this facility is confidential, so the position shown on the map is just illustrative</div>
</#if>
