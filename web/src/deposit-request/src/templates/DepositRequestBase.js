import _ from 'underscore'

export default (template) => {
  return _.template(`
    <div class="text-center fw-bold mt-4">
      <h1 class="fw-bold py-5">EIDC deposit request</h1>
      <p>To deposit resources such as datasets, information products and model output data to the EIDC, please complete this form.</p>
      <p>We won't ask for a lot of information and it should only take a few minutes.</p>
      <p>Once we've received your request, we'll review it and then get in touch to let you know next steps.</p>
      <p>For more information and help on preparing your resource for deposit see our guidance at <a href="https://eidc.ac.uk/deposit/preparingData" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit/preparingData</a></p>
    </div>
    ${template}
  `)
}
