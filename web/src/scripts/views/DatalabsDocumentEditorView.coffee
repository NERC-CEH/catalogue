define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
 	'cs!views/editor/SelectView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/PredefinedParentView'
	'cs!views/editor/KeywordView'
  'cs!views/editor/ContactView'
  'cs!models/editor/Contact'
  

], (
  EditorView
  InputView
  TextareaView
  SelectView
  SingleObjectView
  ParentView
  ParentStringView
  PredefinedParentView
  KeywordView
  ContactView
  Contact
) -> EditorView.extend

  initialize: ->
    @model.set 'type', 'dataset' unless @model.has 'type'

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Title'
          
          new TextareaView
            model: @model
            modelAttribute: 'description'
            label: 'Description'
            rows: 10

          new SelectView
            model: @model
            modelAttribute: 'assetType'
            label: 'Type'
            options: [
              {value: '', label: ''},
              {value: 'Jupyter notebook', label: 'Jupyter notebook'},
              {value: 'Zeppelin notebook', label: 'Zeppelin notebook'},
              {value: 'RStudio project', label: 'RStudio project'},
              {value: 'RShiny app', label: 'RShiny app'}
            ]

          new InputView
            model: @model
            modelAttribute: 'version'
            label: 'Version'
          
          new InputView
            model: @model
            modelAttribute: 'masterUrl'
            label: 'Master URL'

          new InputView
            model: @model
            modelAttribute: 'primaryLanguage'
            label: 'Primary language'

          new InputView
            model: @model
            modelAttribute: 'secondaryLanguage'
            label: 'Secondary language'

          new ParentView
            model: @model
            modelAttribute: 'keywords'
            label: 'Keywords'
            ObjectInputView: KeywordView
          
          new ParentStringView
            model: @model
            modelAttribute: 'inputs'
            label: 'inputs'

          new ParentStringView
            model: @model
            modelAttribute: 'outputs'
            label: 'outputs'

          new ParentStringView
            model: @model
            modelAttribute: 'packages'
            label: 'packages'

          new PredefinedParentView
            model: @model
            ModelType: Contact
            modelAttribute: 'owners'
            label: 'Owners'
            ObjectInputView: ContactView
            multiline: true
            predefined:
              'UKCEH':
                organisationName: 'UK Centre for Ecology & Hydrology'
                role: 'owner'
                email: 'enquiries@ceh.ac.uk'
                organisationIdentifier: 'https://ror.org/00pggkr55'
              'Oher owner':
                role: 'owner'
      ]
    ]

    EditorView.prototype.initialize.apply @