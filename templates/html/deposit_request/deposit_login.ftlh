<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Deposit Success" catalogue=catalogues.defaultCatalogue()>
  <div class="text-center fw-bold mt-4">
    <h1 class="fw-bold py-5">EIDC Deposit Request Form</h1>
    <p>To deposit resources such as datasets, information products and model output data to the EIDC, please complete this form.</p>
    <p>We won't ask for a lot of information and it should only take a few minutes.</p>
    <p>Once we've received your request, we'll review it and then get in touch to let you know next steps.</p>
    <p>For more information and help on preparing your resource for deposit see our guidance at <a href="https://eidc.ac.uk/deposit/preparingData" target="_blank" class="text-decoration-underline">https://eidc.ac.uk/deposit/preparingData</a></p>
  </div>
  <div class="text-center fw-bold mt-4">
    <p class="pt-5 fw-bold">If you already have an EIDC account login to get started</p>
    <a href="/sso/login"
       class="btn"
       style="background-color: var(--header-background-colour); color: white; min-width: 300px;">
      Login
    </a>
  </div>
  <div class="text-center fw-bold my-4 pb-5">
    <p class="pt-5 fw-bold">New to the Environmental Information Data Centre (EIDC)<br>register as a new user first</p>
    <a href="/sso/signup"
       class="btn"
       style="background-color: var(--header-background-colour); color: white; min-width: 300px;">
      Register to get started
    </a>
  </div>
</@skeleton.master>
