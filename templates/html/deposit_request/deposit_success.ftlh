<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Deposit Success" catalogue=catalogues.defaultCatalogue()>
  <div class="text-center fw-bold mt-4">
    <h1 class="fw-bold py-5">EIDC Deposit Request Form successfully submitted</h1>
    <p>Thank you</p>
    <p>Form successfully submitted</p>
    <p>Someone will be in contact with you</p>
    <p>In the mean time here is your reference: ${referenceNumber}</p>
    <p>Contact <a href="mailto:@info.ac.uk">info@ceh.ac.uk</a></p>
  </div>
</@skeleton.master>
