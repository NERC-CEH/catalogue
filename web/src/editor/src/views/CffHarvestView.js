import _ from 'underscore'
import Swal from 'sweetalert2'
import SingleView from '../SingleView'

const template = _.template(`
  <div class="mb-2">
    <div class="d-flex align-items-center input-group">
      <input type="text" class="editor-input cff-url"
             id="input-<%= data.modelAttribute %>"
             placeholder="Enter the path to the GitHub.com CFF file" />
      <button class="btn editor-button cff-extract-btn" type="button">
        <i class="fa-brands fa-github fa-fw"></i>
        Extract
      </button>
    </div>
    <div class="text-center mt-2">
      <div class="spinner-border text-secondary cff-loader"
           role="status"
           style="display:none; width:2rem; height:2rem;">
      </div>
    </div>
  </div>
`)

const CffHarvestView = SingleView.extend({
  events: {
    'click .cff-extract-btn': 'onExtractClick'
  },

  initialize: function (options) {
    SingleView.prototype.initialize.call(this, options)
    this.render()
  },

  render: function () {
    SingleView.prototype.render.apply(this)

    this.$('.dataentry').append(template({
      data: _.extend({}, this.data)
    }))

    this.$loader = this.$('.cff-loader')

    return this
  },

  onExtractClick: async function () {
    const url = this.$('.cff-url').val()
    if (!url) {
      Swal.fire({
        title: 'Missing Input',
        text: 'Please enter a GitHub.com CFF url',
        icon: 'warning',
        confirmButtonText: 'Close'
      })
      return
    }

    try {
      this.$loader.show()
      this.$('.cff-extract-btn').prop('disabled', true)

      const catalogue = window.location.pathname.split('/')[1]
      const response = await fetch(`/documents/harvestCff?catalogue=${catalogue}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ url })
      })

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}))
        throw new Error(errData.error || `HTTP ${response.status}`)
      }

      const data = await response.json()

      Swal.fire({
        title: 'Success',
        text: 'Document created successfully!',
        icon: 'success',
        confirmButtonText: 'View Document'
      }).then(() => {
        window.location.href = `/documents/${data.id}`
      })
    } catch (err) {
      console.error('Error creating doc from CFF:', err)
      Swal.fire({
        title: 'Failed to Create Document',
        text: err.message,
        icon: 'error',
        confirmButtonText: 'Close'
      })
    } finally {
      this.$loader.hide()
      this.$('.cff-extract-btn').prop('disabled', false)
    }
  }
})

export default CffHarvestView
