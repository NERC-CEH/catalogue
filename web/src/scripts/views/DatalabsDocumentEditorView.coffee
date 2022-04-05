define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/ContactView'
  'cs!models/editor/Contact'


], (
  EditorView
  InputView
  TextareaView
  ParentStringView
  PredefinedParentView
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

          new InputView
            model: @model
            modelAttribute: 'version'
            label: 'Version'
          
          new InputView
            model: @model
            modelAttribute: 'masterUrl'
            label: 'Master URL'

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