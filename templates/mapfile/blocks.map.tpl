[#ftl]
[#escape x as x?replace('"', '\\"')]
[#--
  Generates the correct MapServer expression value for the supplied bucket
  and the given attribute value.

  $attr which the bucket is performed on
  $bucket contains the range of the bucket values
--]
[#macro bucketExpr attr bucket]
  [#compress]
  [#if bucket.min?? && bucket.max??]
    (${bucket.min?c} < [${attr}] AND [${attr}] <= ${bucket.max?c})
  [#elseif bucket.min??]
    (${bucket.min?c} < [${attr}])
  [#elseif bucket.max??]
    ([${attr}] <= ${bucket.max?c})
  [/#if]
  [/#compress]
[/#macro]

[#--
  Generate a list of class blocks for the supplied buckets
--]
[#macro buckets attr buckets]
  [#list buckets as bucket]
    CLASS
      NAME "${bucket.label}"
      EXPRESSION [@bucketExpr attr bucket/]
      [@style bucket.style /]
    END
  [/#list]
[/#macro]

[#--
  Generate a list of class blocks for the supplied value settings
--]
[#macro values values]
  [#list values as value]
    CLASS
      NAME "${value.label}"
      EXPRESSION "${value.setting}"
      [@style value.style /]
    END
  [/#list]
[/#macro]

[#--
  Generate a style block for the current colour data
--]
[#macro style data]
  STYLE
    COLOR "${data.colour}"
  END
[/#macro]
[/#escape]
