# -*- mode: ruby -*-
# vi: set ft=ruby :
Vagrant.configure(2) do |config|
  config.vm.box = "nercceh/ubuntu16.04"

  config.vm.network "forwarded_port", guest: 8080, host: 8080
  config.vm.network "forwarded_port", guest: 7000, host: 7000

  config.vm.provision :docker
  config.vm.provision "shell", inline: "cd /vagrant; make build"
end
