# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

ENV['VAGRANT_DEFAULT_PROVIDER'] = 'vmware_workstation'

# Check if the environment disables sharing of directories. If booting up in isolation,
# we don't need to use shared folders
DISABLE_SHARING = ENV['VAGRANT_DISABLE_SHARING']

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "nercceh/ubuntu14.04"
  config.vm.network :forwarded_port, guest: 80, host: 8080, auto_correct: true
  config.vm.network :forwarded_port, guest: 7000, host: 7000, auto_correct: true

  config.vm.synced_folder "catalogue", "/opt/ceh-catalogue", create: true, disabled: DISABLE_SHARING

  config.vm.provider "vmware_workstation" do |v|
    v.vmx["memsize"] = "4096"
    v.vmx["remotedisplay.vnc.enabled"] = "false"
  end

  config.vm.provision :shell, :path => "vagrant_provision/copy-ssl.sh"
  config.vm.provision :shell, :path => "vagrant_provision/test-data.sh"

  config.vm.provision "puppet_server", run: "always" do |puppet|
    puppet.puppet_server = "lapuppet.nerc-lancaster.ac.uk"
    puppet.puppet_node = "cig-developer.nerc-lancaster.ac.uk"
    puppet.options = "--verbose --environment improvement_CEHCIG_603_speed_up_web_testing"
  end
end
