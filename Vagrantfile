# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

ENV['VAGRANT_DEFAULT_PROVIDER'] = 'vmware_workstation'

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  
  config.vm.box = "ubuntu-12.04-amd64"
  config.vm.box_url = "http://ladist.nerc-lancaster.ac.uk/vagrant/ubuntu-12.04-amd64.box"
  config.vm.network :forwarded_port, guest: 443, host: 8080, auto_correct: true
  config.vm.network :forwarded_port, guest: 7000, host: 7000, auto_correct: true

  config.vm.synced_folder "puppet_ssl", "/var/lib/puppet/ssl"
  config.vm.synced_folder "catalogue", "/opt/ceh-catalogue", create: true
  config.vm.synced_folder "catalogue-config", "/etc/ceh-catalogue", create: true

  config.vm.provider "vmware_workstation" do |v|
    v.vmx["memsize"] = "4096"
    v.vmx["remotedisplay.vnc.enabled"] = "false"
  end

  config.vm.provision "puppet_server" do |puppet|
    puppet.puppet_server = "lapuppet.nerc-lancaster.ac.uk"
    puppet.puppet_node = "gateway-developer.ceh.ac.uk"
    puppet.options = "--verbose"
  end
end
