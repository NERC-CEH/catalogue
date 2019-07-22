define [
	'cs!views/EditorView'
	'cs!views/editor/InputView'
	'cs!views/editor/InputDateView'
	'cs!views/editor/TextareaView'
	'cs!views/editor/KeywordView'
	'cs!views/editor/CheckboxView'
	'cs!views/editor/ParentView'
	'cs!views/editor/ParentStringView'
	'cs!views/editor/ParentStringTextboxView'
	'cs!views/editor/PredefinedParentView'
	'cs!views/editor/ResourceConstraintView'
	'cs!views/editor/DataFileView'
	'cs!views/editor/SupportingFileView'
	'cs!views/editor/AuthorView'
	'cs!models/editor/Author'
	'cs!views/editor/PointOfContactView'
	'cs!models/editor/PointOfContact'
	'cs!views/editor/SelectView'
	'cs!views/editor/LinkView'
	'cs!views/editor/ReadOnlyView'

], (EditorView, InputView, InputDateView, TextareaView, KeywordView, CheckboxView, ParentView, ParentStringView, ParentStringTextboxView, PredefinedParentView,  ResourceConstraintView, DataFileView, SupportingFileView, AuthorView, Author, PointOfContactView, PointOfContact, SelectView, LinkView,  ReadOnlyView) -> EditorView.extend

	initialize: ->
		@model.set('type', 'depositAgreement') unless @model.has('type')

		@sections = [
			label: 'General'
			title: 'General'
			views: [
				new InputView
					model: @model
					modelAttribute: 'jiraReference'
					label: 'Jira reference'

				new PredefinedParentView
					model: @model
					ModelType: PointOfContact
					modelAttribute: 'depositor'
					label: 'Depositor'
					ObjectInputView: PointOfContactView
					multiline: true
					predefined:
						'CEH':
							organisationName: 'Centre for Ecology & Hydrology'

				new PredefinedParentView
					model: @model
					ModelType: PointOfContact
					modelAttribute: 'dco'
					label: 'DCO'
					ObjectInputView: PointOfContactView
					multiline: true
					predefined:
						'EIDC':
							organisationName: 'NERC Environmental Information Data Centre'
							email: 'eidc@ceh.ac.uk'
			]
		,	
			label: 'Citation'
			title: 'Citation'
			views: [
				new InputView
					model: @model
					modelAttribute: 'title'
					label: 'Title of data resource'
				
				new InputView
					model: @model
					modelAttribute: 'datasetMetadataID'
					label: 'Metadata file ID'
				
				new ParentView
					model: @model
					ModelType: Author
					modelAttribute: 'authors'
					label: 'Authors'
					ObjectInputView: AuthorView
					helpText: """
						<p>Authors should be listed in the order in which they will appear in the citation</p><p>Author's names must be in the format <code>Surname comma Initial(s)</code>. For example, <code>Smith, K.P.</code> not <code>Kim P. Smith</code></p>
						"""

			]
		,	
			label: 'The data'
			title: 'The data'
			views: [
				new PredefinedParentView
					model: @model
					modelAttribute: 'dataFiles'
					label: 'Data file(s) to be deposited'
					ObjectInputView: DataFileView
					predefined:
						'csv':
							format: 'csv'
					helpText: """
						<p>The filename(s) should not include spaces or special characters</p><p>If a large number of files are to be deposited, do not enter them individually here, complete the field below.</p>
						"""

				new TextareaView
					model: @model
					modelAttribute: 'dataFilesOther'
					label: 'Data files (other)'
					rows: 3
					helpText: """
						<p>If a large number of files are to be deposited, describe the number of files, the total (uncompressed) size and their naming convention.</p>
						"""

				new ParentView
					model: @model
					modelAttribute: 'supportingFiles'
					label: 'Supporting document(s) to be provided '
					ObjectInputView: SupportingFileView
					helpText: """
						<ul><li>Experimental design/Sampling regime</li><li>Collection methods</li><li>Fieldwork and laboratory instrumentation</li><li>Calibration steps and values</li><li>Analytical methods</li><li>Nature and Units of recorded values</li><li>Quality control</li><li>Details of data structure</li><li>Any other information useful to the interpretation of the data</li></ul>
					   """

				new TextareaView
					model: @model
					modelAttribute: 'dataRetention'
					label: 'dataRetention'
					rows: 3
					helpText: """
						<p>Blah</p>
						"""
			]     
		,	
			label: 'Availability & access'
			title: 'Availability and access'
			views: [
				new SelectView
					model: @model
					modelAttribute: 'availability'
					label: 'Data availability'
					options: [
						{value: 'noEmbargo', label: 'Make available asap'},
						{value: 'embargo', label: 'Embargo the data'},
					]
					helpText: """
						<p>Depositors may request that access to the data be restricted for an agreed period (embargoed). A key reason for  an embargo period is to protect the research process by allowing researchers a reasonable amount of time to publish their findings.</p>
						"""

				new InputDateView
					model: @model
					modelAttribute: 'embargoDate'
					label: 'Release date (if embargoed)'
					helpText: """
						<p>NERC considers that, in most cases, a reasonable embargo period is a maximum of two years from the end of data collection.</p>
						"""

				new TextareaView
					model: @model
					modelAttribute: 'onlineAccessRequirements'
					label: 'Specific requirements for online access to dataset '
					rows: 3
					helpText: """
						<p>Blah</p>
						"""

				new TextareaView
					model: @model
					modelAttribute: 'otherServices'
					label: 'Other services required'
					rows: 3
					helpText: """
						<p>Blah</p>
						"""
			]     
		,	
			label: 'Licensing and IPR'
			title: 'Licensing and IPR'
			views: [
				new SelectView
					model: @model
					modelAttribute: 'dataCategory'
					label: 'Data category'
					options: [
						{value: '', label: ''},
						{value: 'data', label: 'Environmental data'},
						{value: 'informationProduct', label: 'Information product'},
					]
					helpText: """
						<p>applies only to NERC-funded data</p>
						"""

				new PredefinedParentView
					model: @model
					modelAttribute: 'licence'
					label: 'Licence'
					ObjectInputView: ResourceConstraintView
					multiline: true
					predefined:
						'OGL':
							value: 'OGL'
							code: 'license'

				new PredefinedParentView
					model: @model
					modelAttribute: 'useConstraints'
					label: 'Additional use constraints'
					ObjectInputView: ResourceConstraintView
					multiline: true
					predefined:
						'Copyright CEH':
							value: '© Centre for Ecology & Hydrology (Natural Environment Research Council)'
							code: 'copyright'
						'Copyright NERC':
							value: '© Natural Environment Research Council: '
							code: 'copyright'
					helpText: """
						<p>Document here any specific text required in the metadata 'Use constraints' section. Include any copyright required</p>
						"""

				new TextareaView
					model: @model
					modelAttribute: 'policies'
					label: 'Policies & legislation'
					rows: 3
					helpText: """
						<p>You confirm that the data you deposit is compliant with the provisions of the Data Protection Act 2018 and the General Data Protection Regulations (GDPR).  Data and supporting documentation should not contain names and addresses relating to 'identifiable natural persons'.  However, discovery metadata (the catalogue record) may contain names and contact details of the authors/contributors of this data (<a href='http://eidc.ceh.ac.uk/policies/retentionPersonalData' target='_blank'  rel='noopener noreferrer'>see our policy on retention and use of personal data</a>).</p><p>All environmental data deposited into the EIDC is also subject to the requirements of the <a href='https://nerc.ukri.org/research/sites/data/policy/' target='_blank'  rel='noopener noreferrer'>NERC Data Policy</a>.</p><p>If other policies/legislation applies (e.g. INSPIRE), please specify here.</p>
</p>
						"""
			]
		,	
			label: 'Superseding data'
			title: 'Superseding existing data'
			views: [						
				new TextareaView
					model: @model
					modelAttribute: 'supersededReason'
					label: 'supersededReason'
					rows: 3
					helpText: """
						<p>If the data is intended to supersede an existing dataset held by the EIDC, you should explain why it is to be replaced, including details of any errors found</p>
						"""

				new InputView
					model: @model
					modelAttribute: 'supersededData'
					label: 'Metadata ID of the dataset to be replaced'
				
			]
		,			
			label: 'Miscellaneous'
			title: 'Miscellaneous'
			views: [
				new TextareaView
					model: @model
					modelAttribute: 'miscellaneous'
					label: 'Any other information you wish to provide'
					rows: 8
				]
		,			
			label: 'Document info'
			title: 'Metadata'
			views: [
				new ReadOnlyView
					model: @model
					modelAttribute: 'id'
					label: 'Identifier'
			
				new ReadOnlyView
					model: @model
					modelAttribute: 'uri'
					label: 'URL'
				
				new ReadOnlyView
					model: @model
					modelAttribute: 'metadataDate'
					label: 'Last updated'
				]
		]

		EditorView.prototype.initialize.apply @
