define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/ContactView'
  'cs!models/editor/Contact'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/ReferenceView'
  'cs!models/editor/Reference'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/DataInfoView'
  'cs!views/editor/NercModelInfoView'
  'cs!views/editor/FundingView'
  'cs!models/editor/Funding'
  'cs!views/editor/SupplementalView'
  'cs!models/editor/Supplemental'
  

], (
  EditorView, InputView, TextareaView, ParentView, PredefinedParentView, ContactView, Contact, ParentStringView, KeywordView, ReferenceView, Reference, SingleObjectView, DataInfoView, NercModelInfoView, FundingView, Funding, SupplementalView, Supplemental
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'nercModelUse') unless @model.has('type')

    @sections = [
      label: 'General'
      title: 'General'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

        new TextareaView
          model: @model
          modelAttribute: 'objectives'
          label: 'Objectives'
          rows: 17
          helpText: """
                    <p>Brief description of the main objectives</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17
          helpText: """
                    <p>Longer description of project incl. why models were used to answer the science question, assumptions made, key outputs</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView

        new InputView
          model: @model
          modelAttribute: 'completionDate'
          label: 'Completion date'

        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'responsibleParties'
          label: 'Contacts'
          ObjectInputView: ContactView
          multiline: true

        new PredefinedParentView
          model: @model
          modelAttribute: 'funding'
          ModelType: Funding
          multiline: true
          label: 'Funding'
          ObjectInputView: FundingView
          predefined:
            'BBSRC':
              funderName: 'Biotechnology and Biological Sciences Research Council'
              funderIdentifier: 'https://ror.org/00cwqg982'
            'Defra':
              funderName: 'Department for Environment Food and Rural Affairs'
              funderIdentifier: 'https://ror.org/00tnppw48'
            'EPSRC':
              funderName: 'Engineering and Physical Sciences Research Council'
              funderIdentifier: 'https://ror.org/0439y7842'
            'ESRC':
              funderName: 'Economic and Social Research Council'
              funderIdentifier: 'https://ror.org/03n0ht308'
            'Innovate UK':
              funderName: 'Innovate UK'
              funderIdentifier: 'https://ror.org/05ar5fy68'
            'MRC':
              funderName: 'Medical Research Council'
              funderIdentifier: 'https://ror.org/03x94j517'
            'NERC':
              funderName: 'Natural Environment Research Council'
              funderIdentifier: 'https://ror.org/02b5d8509'
            'STFC':
              funderName: 'Science and Technology Facilities Council'
              funderIdentifier: 'https://ror.org/057g20z61'
      ]
    ,
      label: 'References'
      title: 'References'
      views: [
        new ParentView
          model: @model
          ModelType: Reference
          modelAttribute: 'references'
          label: 'References'
          ObjectInputView: ReferenceView
          multiline: true
          helpText: """
                    <p>Citation - Add publication citations here</p>
                    <p>DOI - DOI link for the citation e.g. https://doi.org/10.1111/journal-id.1882</p>
                    <p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'rooferences'
          ModelType: Supplemental
          multiline: true
          label: 'rooferences'
          ObjectInputView: SupplementalView
          helpText: """
                    <p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>
                    <ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>
                    <p>When linking to published articles, please use DOIs whenever possible.</p>
                    <p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>
                    """
                    
      ]
    ,
      label: 'Model Info'
      title: 'Model Info'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'modelInfos'
          label: 'Model info'
          ObjectInputView: NercModelInfoView
          multiline: true
          helpText: """
                    <p>Models used in the project</p>
                    <p>Name - Name of model (searches catalogue for matching models)
                    <p>Version - Version of the model used for the application (not necessarily the current release version) e.g. v1.5.2 (if applicable)</p>
                    <p>Rationale - Why was this model chosen for use in this project?</p>
                    <p>Spatial extent of application - What spatial extent best describes the application?</p>
                    <p>Available spatial data - Can the application be described by either a shapefile/polygon or bounding box coordinates?</p>
                    <p>Spatial resolution of application - Spatial resolution at which model outputs were generated e.g. 1km²; 5m² (if applicable)</p>
                    <p>Temporal extent of application (start date) - Start date of application (if applicable)</p>
                    <p>Temporal extent of application (end date) - End date of application (if applicable)</p>
                    <p>Temporal resolution of application - Time step used in the model application e.g. 1s; annual (if applicable)</p>
                    <p>Calibration conditions - How was the model calibrated (if applicable)?</p>
                    """
      ]
    ,
      label: 'Data Info'
      title: 'Data Info'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'inputData'
          label: 'Input Data'
          ObjectInputView: DataInfoView
          multiline: true
          helpText: """
                    <p>Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'outputData'
          label: 'Output Data'
          ObjectInputView: DataInfoView
          multiline: true
          helpText: """
                    <p>Detailed description of model outputs including: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @