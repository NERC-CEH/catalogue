<#-- 
                   _                                     __ _   _
   _   _ _ __   __| | ___ _ __ ___  ___ ___  _ __ ___   / _| |_| |
  | | | | '_ \ / _` |/ _ \ '__/ __|/ __/ _ \| '__/ _ \ | |_| __| |
  | |_| | | | | (_| |  __/ |  \__ \ (_| (_) | | |  __/_|  _| |_| |
   \__,_|_| |_|\__,_|\___|_|  |___/\___\___/|_|  \___(_)_|  \__|_|

  Here are a collection of generic functions for use with freemarker. These 
  functions are inspired by Underscore.js, the javascript utility belt.

  @author Christopher Johnson
-->

<#--
Looks through each value in the list, returning an array of all the values that
pass a truth test (predicate).
-->
<#function filter arr predicate>
  <#local result = []>
  <#list arr as e>
    <#if predicate(e)>
      <#local result = result + [e]>
    </#if>
  </#list>
  <#return result>
</#function>

<#--
Returns the values in list without the elements that the truth test (predicate) 
passes. The opposite of filter.
-->
<#function reject arr predicate>
  <#local result = []>
  <#list arr as e>
    <#if !predicate(e)>
      <#local result = result + [e]>
    </#if>
  </#list>
  <#return result>
</#function>