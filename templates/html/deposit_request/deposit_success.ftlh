<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Thanks" catalogue=catalogues.defaultCatalogue()>
  <div class="container">
    <h1 class="mt-5 mb-4 display-4">Thank you</h1>
    <p>Your deposit reference is <b>${referenceNumber}</b></p>
    <#if componentName == "Ingestion Management">
      <p>Based on the details you've provided, we are happy to proceed with the deposit of your data. We will contact you in due course to discuss details and develop a Service Agreement.</p>
      <p>Please note, we are currently experiencing high demand for our services and therefore there may be a slight delay in progressing your deposit, for which we apologise. Please be reassured that your deposit is on our waiting list and we will be in touch as soon as possible.</p>
    <#else>
      <p>We'll review your details and someone will be in contact with you soon to discuss next steps.</p>
    </#if>
    <p>In the meantime, if you need any help, please <a href="mailto:info@eidc.ac.uk">email info@eidc.ac.uk</a>, quoting reference number ${referenceNumber}</p>
  </div>
</@skeleton.master>
