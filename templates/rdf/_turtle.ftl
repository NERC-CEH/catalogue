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

  Note what this deliberately does NOT change. A double quote is replaced with
  an apostrophe rather than escaped as \", which is lossy: proper escaping
  would keep the character. That substitution is what the catalogue has emitted
  for years, and every literal already published carries it, so changing it
  here would silently rewrite existing data for no gain in validity. (void.ftl
  escapes properly, having been written later; the two are inconsistent and
  reconciling them is a data decision, not a bug fix.) Same reasoning for
  turning a line break into a space rather than \n.
-->

<#--
  @param string  the literal's text, as stored on the record
  @return        the same text, safe to place between double quotes; callers add
                 the quotes themselves, so a datatype or language tag can follow
-->
<#function escape string>
  <#-- Backslash first: it is the only rule here that emits a backslash, and
       running it after a rule that introduced one would escape that too. -->
  <#return string?replace("\\", "\\\\")?replace("\"", "'")?replace("\r", " ")?replace("\n", " ")>
</#function>
