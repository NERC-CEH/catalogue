<#ftl output_format="plainText">
<#--
  Escaping for the inside of a Turtle quoted literal ("..."), shared by every
  template that emits one.

  Three characters cannot appear raw between the quotes, and until dri-one #344
  only two of them were handled:

    "   ends the literal early
    \   starts an escape sequence
    CR / LF   are not permitted in a single-quoted Turtle string at all

  The backslash was the one missed, and it is the one real data actually
  contains: a Royal Society grant reference is written ICA\R1\180100, so
  frapo:hasGrantNumber emitted "ICA\R1\180100" and Fuseki rejected the entire
  20MB upload with

    [line: 332531, col: 28] Illegal escape sequence value: R (0x52)

  because \R is not a legal Turtle escape. The PUT is all-or-nothing, so that
  one character held back every triple for a week — the whole dri-one #319-#330
  batch and #334 with it.

  All of them are escaped (\" \\ \r \n), so the literal reads back exactly
  as the record stores it. Until dri-one #359 a double quote was instead
  replaced with an apostrophe and a line break with a space. That was valid
  Turtle but lossy: the graph no longer said what the record said, and every
  multi-paragraph abstract (over half of EIDC's records) was published as one
  undifferentiated run of text. void.ftl had already escaped the quote
  properly, so the export disagreed with itself; it now uses this function too.
-->

<#--
  @param string  the literal's text, as stored on the record
  @return        the same text, safe to place between double quotes; callers add
                 the quotes themselves, so a datatype or language tag can follow
-->
<#function escape string>
  <#-- Backslash first: every rule here emits a backslash, and running this
       one after any of the others would escape that backslash a second time. -->
  <#return string?replace("\\", "\\\\")?replace("\"", "\\\"")?replace("\r", "\\r")?replace("\n", "\\n")>
</#function>
