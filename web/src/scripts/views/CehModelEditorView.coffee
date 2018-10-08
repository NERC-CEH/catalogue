define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/SelectView'
  'cs!views/editor/ReferenceView'
  'cs!models/editor/Reference'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/QaView'
  'cs!views/editor/VersionHistoryView'
  'cs!views/editor/ProjectUsageView'
], (
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  ParentStringView,
  KeywordView,
  SelectView,
  ReferenceView,
  Reference,
  SingleObjectView,
  QaView,
  VersionHistoryView,
  ProjectUsageView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'model') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Name of the model</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'primaryPurpose'
          label: 'Primary purpose'
          rows: 17
          helpText: """
                    <p>Short phrase to describe primary aim of model</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'website'
          label: 'Website url'
          helpText: """
                    <p>Link to outward facing model website if one exists e.g. https://jules.jchmr.org/</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'seniorResponsibleOfficer'
          label: 'Senior responsible officer'
          helpText: """
                    <p>Senior responsible officer for the model (this should be the person who is the primary contact for the model)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'seniorResponsibleOfficerEmail'
          label: 'Senior responsible officer email'
          helpText: """
                    <p>Email address of the Senior Responsible Officer e.g. someone@ceh.ac.uk</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>5-10 keywords for model discovery e.g. rainfall; species distribution; nitrogen deposition; global circulation model</p>
                    """

        new SelectView
          model: @model
          modelAttribute: 'licenseType'
          label: 'License'
          options: [
            {value: 'unknown', label: 'Unknown'},
            {value: 'open', label: 'Open'},
            {value: 'non-open', label: 'Non-open'}
          ]
          helpText: """
                    <p>License type (open or non-open) under which the model is distributed</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'codeRepositoryUrl'
          label: 'Code repository url'
          helpText: """
                    <p>Location of code repository e.g. https://github.com/NERC-CEH/Spatial-Upscaling-model.</p>
                    <p>This does not need to be accessible by others but is to demonstrate that the model is under version control.</p>
                    <p>If your model is not currently under version control and you are unsure about how to achieve this please talk to your Informatics Liaison representative.</p>
                    <p>CEH currently supports version control primarily with git.</p>
                    """

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
      ]
    ,
      label: 'Model Description'
      title: 'Model Description'
      views: [
        new ParentStringView
          model: @model
          modelAttribute: 'keyInputVariables'
          label: 'Key input variables'
          helpText: """
                    <p>Short phrases to describe basic types of model inputs e.g. rainfall; temperature; land use; atmospheric deposition</p>
                    """

        new ParentStringView
          model: @model
          modelAttribute: 'keyOutputVariables'
          label: 'Key output variables'
          helpText: """
                    <p>Short phrases to describe basic types of model outputs e.g. nutrient runoff; deposition time series; species occurrence</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Model description'
          rows: 17
          helpText: """
                    <p>Longer description of model e.g. development history, use to answer science questions, overview of structure</p>
                    """

        new SelectView
          model: @model
          modelAttribute: 'modelType'
          label: 'Model type'
          options: [
            {value: 'unknown', label: 'Unknown'},
            {value: 'deterministic', label: 'Deterministic'},
            {value: 'stochastic', label: 'Stochastic'},
            {value: 'other', label: 'Other'}
          ]
          helpText: """
                    <p>Type which best fits model</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'currentModelVersion'
          label: 'Current model version'
          helpText: """
                    <p>Most recent release version (if applicable) e.g. v2.5.10</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'releaseDate'
          typeAttribute: 'date'
          label: 'Release date'
          helpText: """
                    <p>Date of release of current model version (if applicable) e.g. 2012-02-17</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'modelCalibration'
          label: 'Model calibration'
          rows: 17
          helpText: """
                    <p>Does the model need calibration before running? If so, what needs to be supplied to do this? (if applicable)</p>
                    """
      ]
    ,
      label: 'Spatio-Temporal Info'
      title: 'Spatio-Temporal Info'
      views: [
        new InputView
          model: @model
          modelAttribute: 'spatialDomain'
          label: 'Spatial domain'
          helpText: """
                    <p>Is the model only applicable to certain areas? E.g. Parameterised for UK only or global (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'spatialResolution'
          label: 'Spatial resolution'
          helpText: """
                    <p>Spatial resolution at which model works or at which model outputs are generated e.g. 1km&sup2;  or 5m&sup2; (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'temporalResolutionMin'
          label: 'Temporal resolution (min)'
          helpText: """
                    <p>Minimum time step supported by the model e.g. 1 second or 10 days (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'temporalResolutionMax'
          label: 'Temporal resolution (max)'
          helpText: """
                    <p>Maximum time step supported by the model e.g. annual or decadal (if applicable)</p>
                    """
      ]
    ,
      label: 'Technical Info'
      title: 'Technical Info'
      views: [
        new InputView
          model: @model
          modelAttribute: 'language'
          label: 'Language'
          helpText: """
                    <p>Computing language in which the model is written e.g. C++; R</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'compiler'
          label: 'Compiler'
          helpText: """
                    <p>Compiler required e.g. C++ compiler (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'operatingSystem'
          label: 'Operating system'
          helpText: """
                    <p>Operating system typically used to run the model</p>
                    """

         new InputView
          model: @model
          modelAttribute: 'systemMemory'
          label: 'System memory'
          helpText: """
                    <p>Memory required to run code (if known)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'documentation'
          label: 'Documentation url'
      ]
    ,
      label: 'QA Info'
      title: 'QA Info'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'developerTesting'
          label: 'Developer testing'
          ObjectInputView: QaView
          helpText: """
                    <p>Use of a range of developer tools including parallel build and analytical review or sense check</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'internalPeerReview'
          label: 'Internal peer review'
          ObjectInputView: QaView
          helpText: """
                    <p>Obtaining a critical evaluation from a third party independent of the development of the model but from within the same organisation</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'externalPeerReview'
          label: 'External peer review'
          ObjectInputView: QaView
          helpText: """
                    <p>Formal or informal engagement of a third party to conduct critical evaluation from outside the organisation in which the model is being developed</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'internalModelAudit'
          label: 'Internal model audit'
          ObjectInputView: QaView
          helpText: """
                    <p>Formal audit of a model within the organisation, perhaps involving use of internal audit functions</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'externalModelAudit'
          label: 'External model audit'
          ObjectInputView: QaView
          helpText: """
                    <p>Formal engagement of external professional to conduct a critical evaluation of the model, perhaps involving audit professionals</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'qaGuidelinesAndChecklists'
          label: 'Quality assurance guidelines & checklists'
          ObjectInputView: QaView
          helpText: """
                    <p>Model development refers to department’s guidance or other documented QA processes (e.g. third party publications)</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'governance'
          label: 'Governance'
          ObjectInputView: QaView
          helpText: """
                    <p>At least one of planning, design and/or sign-off of model for use is referred to a more senior person.  There is a clear line of accountability for the model</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'transparency'
          label: 'Transparency'
          ObjectInputView: QaView
          helpText: """
                    <p>Model is placed in the wider domain for scrutiny, and/or results are published</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'periodicReview'
          label: 'Periodic review'
          ObjectInputView: QaView
          helpText: """
                    <p>Model is reviewed at intervals to ensure it remains fit for the intended purpose, if used on an ongoing basis</p>
                    """
      ]
    ,
      label: 'Version Control History'
      title: 'Version Control History'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'versionHistories'
          label: 'Version control change notes'
          ObjectInputView: VersionHistoryView
          multiline: true
          helpText: """
                    <p>Use a unique identifier for different versions of a model</p>
                    """
      ]
    ,
      label: 'Project Usage'
      title: 'Project Usage'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'projectUsages'
          label: 'Project usage'
          ObjectInputView: ProjectUsageView
          helpText: """
                    <p>Use of model in projects</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @