import _ from 'underscore'

export default (template) => {
  return _.template(`

    <h1 class="mt-5 mb-4 display-4">EIDC deposit request</h1>
    <p>To deposit datasets/information products with the EIDC, please fill in the form below.</p>
    <p>We don't need a lot of information at this stage and it should only take a few minutes. Once we've received your request, we'll review it and then get in touch to let you know next steps.</p>
    <p>For more information and help on preparing your resource for deposit see our guidance at <a href="https://eidc.ac.uk/deposit/preparingData" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit/preparingData</a></p>

      ${template}
  `)
}
