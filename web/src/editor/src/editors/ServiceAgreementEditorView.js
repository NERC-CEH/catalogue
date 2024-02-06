import _ from 'underscore'
import Backbone from 'backbone'
import {
  AuthorView, CategoryView, DescriptiveKeywordView, EndUserLicenceView,
  FileView, FundingView,
  ParentView,
  PredefinedParentView, RightsHolderView,
  SingleObjectView, SupportingDocView,
  TextareaView,
  TextOnlyView
} from '../views'
import { EditorView, InputView } from '../index'
import { Author, DescriptiveKeyword, Funding, RightsHolder, SupportingDoc } from '../models'
import { BoundingBox, BoundingBoxView } from '../geometryMap'
import Swal from 'sweetalert2'

export default EditorView.extend({

  initialize () {
    this.delegate({ 'click #exitWithoutSaving': 'exit' })
    this.delegate({ 'click #editorExit': 'attemptExit' })

    this.sections = [{
      label: 'General',
      title: '',
      views: [
        new TextOnlyView({
          model: this.model,
          text: "<h1>EIDC service agreement</h1><p>For more information/guidance about this document see <a href='https://eidc.ac.uk/support/agreement' target='_blank' rel='noopener noreferrer'>https://eidc.ac.uk/deposit/agreement</a></p>"
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'depositReference',
          label: 'Deposit Reference'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'depositorName',
          label: 'Depositor Name'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'depositorContactDetails',
          label: "Depositor's contact details"
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'eidcName',
          label: 'EIDC contact name'
        }),

        new TextOnlyView({
          model: this.model,
          text: `\
<h3>Data identification and citation</h3>
<p><strong>PLEASE NOTE: once the dataset is published, the title and authors cannot be changed</strong><p>\
`
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Title',
          text: `<p>Provide a brief title that best describes the data resource, <strong>not</strong> the project or activity from which the data were derived. Include references to the subject, spatial and temporal aspects of the data resource. <a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer' >Further guidance is available on our website</a>.</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'title'
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Authors',
          text: `<p>List authors below in the order in which they will appear in the citation.</p>
<p>Author's names must be in the format <code>Surname &laquo;comma&raquo; Initial(s)</code>. For example, <code>Smith, K.P.</code> <strong>not</strong> <code>Kim P. Smith</code></p>
<p>Authors' details will be published in a public data catalogue and held in EIDC systems.  UK law requires us to inform all individuals listed that they are being proposed as an author.  We therefore require a current, valid email address (or phone number) for all living authors.  Those without valid contact details are not eligible for authorship.  Please see our <a href='http://eidc.ceh.ac.uk/policies/privacy' target='_blank' rel='noopener noreferrer'>Privacy Notice</a> for further information</p>\
`
        }),

        new ParentView({
          model: this.model,
          ModelType: Author,
          modelAttribute: 'authors',
          ObjectInputView: AuthorView,
          multiline: true
        })
      ]
    },
    {
      label: 'The Data',
      title: 'The Data',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Data retention',
          text: `<p>The EIDC's policy is to assign a DOI to all deposited data; such data will be kept in perpetuity.  If a DOI is not required or there are any other exceptions to this policy, please state them below.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'policyExceptions',
          rows: 3
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'fileNumber',
          label: 'Number of files to be deposited'
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Files',
          text: `\
<p>List the files to be deposited below - filenames must not include any spaces or special characters</p>
<p>If there are a too many files to list separately, you can specify a naming convention below <strong>instead</strong>. (If doing so, please also indicate the total size of the deposit (e.g. 500Gb).)</p>\
`
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'files',
          ObjectInputView: FileView,
          predefined: {
            CSV: {
              format: 'csv'
            },
            NetCDF: {
              format: 'NetCDF'
            }
          },
          multiline: true
        }),

        new TextareaView({
          model: this.model,
          label: 'Naming convention',
          modelAttribute: 'fileNamingConvention',
          rows: 5,
          helpText: `
<p>Specify a naming convention <strong>only</strong> if there are too many files to list individually.  Please also indicate the total size of the deposit (e.g. 500Gb).</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'transferMethod',
          label: 'Transfer Method',
          listAttribute: `
<option value='Upload via EIDC catalogue' />
`
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Data Category',
          text: `<p>If the data are wholly or partly funded by NERC, the data must be categorised as either <strong>Environmental Data</strong> or <strong>Information Product</strong>.</p><p>Environmental data are '<i>individual items or records ... obtained by measurement, observation or modelling of the natural world... including all necessary calibration and quality control. This includes data generated through complex systems, such as ... models, including the model code used to produce the data.</i>' </p><p>Information Products are '<i>created by adding a level of intellectual input that refines or adds value to data through interpretation and/or combination with other data</i>'.</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'dataCategory',
          ObjectInputView: CategoryView
        })
      ]
    },
    {
      label: 'Supporting documentation',
      title: 'Supporting documentation',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Document(s) to be provided',
          text: `<p>Please provide the title and file extension of document(s) you will provide to enable re-use of the data (see <a href="https://eidc.ac.uk/deposit/supportingDocumentation">https://eidc.ac.uk/deposit/supportingDocumentation</a>).</a>
<p>Describe the content of the documentation to be supplied. Mandatory elements are:</p>
<ul><li>Collection/generation methods</li><li>Nature and Units of recorded values</li><li>Quality control</li><li>Details of data structure</li></ul>
<p>Required elements (if appropriate) include:</p>
<ul><li>Experimental design/Sampling regime</li><li>Fieldwork and laboratory instrumentation</li><li>Calibration steps and values</li><li>Analytical methods</li><li>Any other information useful to the interpretation of the data</li></ul>\
`
        }),

        new ParentView({
          model: this.model,
          ModelType: SupportingDoc,
          label: 'Supporting documents',
          modelAttribute: 'supportingDocs',
          ObjectInputView: SupportingDocView,
          multiline: true
        })

      ]
    },
    {
      label: 'Availability, access and licensing',
      title: 'Availability, access and licensing',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'End user licence',
          text: `
<p>Please state under which licence the data will be made available. the vast majority of NERC-funded data are provided under the Open Government Licence. We recommend that you seek guidance from your institution and/or funding agency as to the appropriate licence.</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'endUserLicence',
          ObjectInputView: EndUserLicenceView
        }),

        new ParentView({
          model: this.model,
          ModelType: RightsHolder,
          modelAttribute: 'ownersOfIpr',
          label: 'Owner of IPR',
          ObjectInputView: RightsHolderView,
          multiline: true
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Availability',
          text: `\
<p>Depositors may request that access to the data be restricted for an agreed period (embargoed).  Embargoes and embargo periods may be subject to funder requirements. For NERC-funded research, a reasonable embargo period is considered to be a maximum of two years <strong>from the end of data collection.</strong></p>
<p>If an embargo is required, please specify below.</p>\
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'availability',
          typeAttribute: 'date'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'useConstraints',
          label: 'Additional Use Constraints',
          rows: 3
        })
      ]
    },
    {
      label: 'Legislation & funding',
      title: 'Legislation & funding',
      views: [
        new TextOnlyView({
          model: this.model,
          label: 'Other policies/legislation',
          text: `<p>All environmental data deposited into the EIDC are subject to the requirements of the <a href='https://nerc.ukri.org/research/sites/environmental-data-service-eds/policy/' target='_blank' rel='noopener noreferrer'>NERC Data Policy.</a></p>
<p>By depositing data, you confirm that the data is compliant with the provisions of UK data protection laws.</p>
<p>Data and supporting documentation should not contain names, addresses or other personal information relating to 'identifiable natural persons'.  Discovery metadata (the catalogue record) may contain names and contact details of the authors of this data (<a href='https://eidc.ac.uk/policies/retentionPersonalData' target='_blank' rel='noopener noreferrer'>see our policy on retention and use of personal data</a>).</p>
<p>If other policies/legislation applies, please specify below.</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'otherPoliciesOrLegislation',
          rows: 5
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'funding',
          ModelType: Funding,
          multiline: true,
          label: 'Grants/awards used to generate this resource',
          ObjectInputView: FundingView,
          predefined: {
            NERC: {
              funderName: 'Natural Environment Research Council',
              funderIdentifier: 'https://ror.org/02b5d8509'
            },
            BBSRC: {
              funderName: 'Biotechnology and Biological Sciences Research Council',
              funderIdentifier: 'https://ror.org/00cwqg982'
            },
            Defra: {
              funderName: 'Department for Environment Food and Rural Affairs',
              funderIdentifier: 'https://ror.org/00tnppw48'
            },
            EPSRC: {
              funderName: 'Engineering and Physical Sciences Research Council',
              funderIdentifier: 'https://ror.org/0439y7842'
            },
            ESRC: {
              funderName: 'Economic and Social Research Council',
              funderIdentifier: 'https://ror.org/03n0ht308'
            },
            'Innovate UK': {
              funderName: 'Innovate UK',
              funderIdentifier: 'https://ror.org/05ar5fy68'
            },
            MRC: {
              funderName: 'Medical Research Council',
              funderIdentifier: 'https://ror.org/03x94j517'
            },
            STFC: {
              funderName: 'Science and Technology Facilities Council',
              funderIdentifier: 'https://ror.org/057g20z61'
            }
          }
        })
      ]
    },
    {
      label: 'Miscellaneous',
      title: 'Miscellaneous',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Superseding existing data',
          text: `
<p>If the data is superseding an existing dataset held by the EIDC, please specify and explain why it is to be replaced. Include details of any errors found.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'supersededData',
          rows: 5
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'relatedDataHoldings',
          label: 'Related Data Holdings',
          rows: 5
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Other info',
          text: `
<p>If there is any other information you wish to provide, please include it below.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'otherInfo',
          rows: 7
        })
      ]
    },
    {
      label: 'Discovery metadata',
      title: 'Discovery metadata',
      views: [

        new TextOnlyView({
          model: this.model,
          text: `\
<p>Data resources deposited with the EIDC have an entry in the EIDC data catalogue, enabling users to find and access them. Please provide the following information to help complete the catalogue record. <a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer' >Further guidance is available on our website</a>.</p>
<p><em>Please note, this information is not fixed and may be subject to change and improvement over time</em></p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description (min 100 characters)',
          rows: 12,
          helpText: `\
<p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>
<p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>
<p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the "What, Where, When, How, Why, Who" structure.</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'lineage',
          label: 'Lineage',
          rows: 10,
          helpText: `\
<p>Information about the source data used in the construction of this data resource.</p>
<p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>\
`
        }),

        new PredefinedParentView({
          model: this.model,
          ModelType: DescriptiveKeyword,
          modelAttribute: 'descriptiveKeywords',
          label: 'Keywords',
          ObjectInputView: DescriptiveKeywordView,
          multiline: true,
          predefined: {
            'Catalogue topic': {
              type: 'Catalogue topic'
            }
          },
          helpText: `\
<p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>
<p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>\
`
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'areaOfStudy',
          ModelType: BoundingBox,
          label: 'Area of Study',
          ObjectInputView: BoundingBoxView,
          multiline: true,
          predefined: {
            England: {
              northBoundLatitude: 55.812,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -6.452,
              extentName: 'England',
              extentUri: 'http://sws.geonames.org/6269131'
            },
            'Great Britain': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648,
              extentName: 'Great Britain'
            },
            'Northern Ireland': {
              northBoundLatitude: 55.313,
              eastBoundLongitude: -5.432,
              southBoundLatitude: 54.022,
              westBoundLongitude: -8.178,
              extentName: 'Northern Ireland',
              extentUri: 'http://sws.geonames.org/2641364'
            },
            Scotland: {
              northBoundLatitude: 60.861,
              eastBoundLongitude: -0.728,
              southBoundLatitude: 54.634,
              westBoundLongitude: -8.648,
              extentName: 'Scotland',
              extentUri: 'http://sws.geonames.org/2638360'
            },
            'United Kingdom': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648,
              extentName: 'United Kingdom',
              extentUri: 'http://sws.geonames.org/2635167'
            },
            Wales: {
              northBoundLatitude: 53.434,
              eastBoundLongitude: -2.654,
              southBoundLatitude: 51.375,
              westBoundLongitude: -5.473,
              extentName: 'Wales',
              extentUri: 'http://sws.geonames.org/2634895'
            },
            World: {
              northBoundLatitude: 90.00,
              eastBoundLongitude: 180.00,
              southBoundLatitude: -90.00,
              westBoundLongitude: -180.00
            }
          }
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  },

  attemptExit () {
    Swal.fire({
      title: 'Do you want to exit without saving?',
      confirmButtonText: 'Yes?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.exit()
      }
    })
  },

  exit () {
    _.invoke(this.sections, 'remove')
    this.remove()

    if (Backbone.history.location.pathname === `/documents/${this.model.get('id')}`) {
      return Backbone.history.location.replace(`/service-agreement/${this.model.get('id')}`)
    } else {
      return Backbone.history.location.reload()
    }
  }
})
