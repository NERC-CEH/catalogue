# Bamboo deploy script to update Puppet environments repository with version
# number of catalogue to be released to production.
# ASSUMPTIONS:
# - Bamboo has cloned the environments repository
cd environments
git config user.email "noreply@ceh.ac.uk"
git config user.name "Bamboo"
./node-edit.rb cig-prod.nerc-lancaster.ac.uk cehinformationgateway::catalogue::version ${bamboo.deploy.version}
./node-edit.rb cig-prod.nerc-lancaster.ac.uk cehinformationgateway::catalogue::project_revision Catalogue-${bamboo.deploy.version}
git add hieradata/nodes/cig-prod.nerc-lancaster.ac.uk.yaml
git commit -m "[Bamboo deploy of cig-prod.nerc-lancaster.ac.uk ${bamboo.deploy.version}]"
git push
