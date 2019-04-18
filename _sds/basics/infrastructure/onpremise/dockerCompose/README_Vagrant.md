# Using the Vagrantfile to docker-compose inside VM
 
1. Place the Vagrantfile in the same folder as the docker-compose.yml.
1. Make sure you have installed Vagrant.
1. Install the vagrant-disksize plugin by running `vagran plugin install vagrant-disksize`.
1. Run `vagrant up` to start the Vagrant VM.
1. When the Vagrant VM is up, run 'vagrant ssh' to open a shell in the VM.
1. The contents of the `dockerCompose` folder are inside the `/vagrant` folder in the VM.
  - `cd` to the `/vagrant` folder. 
  - From here you can run the `docker-compose` as usual.
  - All the ports are forwarded to your maching so you can still reach for example the Zeppelin server by going to `localhost:8080` in your web browser.
1. After exiting the Vagrant VM you can shut down the VM with `vagrant halt`. This allows you to quickly start it again with `vagrant up` without having to set up the environment. 
1. If you want to completely remove the VM you can use `vagrant destroy` but keep in 
mind that it will have to pull all the docker images again.

## Some additional info: 

1. The Vagrantfile contains lines setting the disk size and ram size of the VM, 
namely the `config.disksize.size` and the `vb.memory` respectively. 
They are currently set to 50GB disk space and 8GB ram. 
If this isn't enough you can increase it by editing these lines appropriately.
1. The Vagrantfile is configured to use Virtualbox as provider. If you're using 
some other provider, like VMWare, you will have to modify the 
Vagrantfile to add a code block like the one starting with 
`config.vm.provider "virtualbox"` for that particular provider. 
