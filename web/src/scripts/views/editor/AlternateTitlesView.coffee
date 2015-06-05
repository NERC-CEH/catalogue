define [
  'cs!models/editor/String'
  'cs!views/editor/ParentView'
  'tpl!templates/editor/AlternateTitles.tpl'
  'cs!views/editor/AlternateTitlesItemView'
], (String, ParentView, template, AlternateTitlesItemView) -> ParentView.extend
  template:       template
  ModelType:      String
  modelAttribute: 'alternateTitles'
  ChildView:      AlternateTitlesItemView
  helpText: """
            <p>Alternative titles allow for entries of multiple titles, translations of titles (e.g. Welsh), and those with acronyms.</p>
            <p>The leading letter and proper nouns of the title only should be capitalised.</p>
            <p>In the event that the alternative title includes acronyms in the formal title of a data resource, then include
            both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>

            """
