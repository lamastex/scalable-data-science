#/bin/bash
source Variables.sh
SplitSlaves=$(echo $SLAVE | sed 's/\\n/ /g')

ssh-keygen
ssh-copy-id -i 'xadmin@localhost'

for slave in $SplitSlaves
do
	ssh-copy-id -i 'xadmin@'$slave
	scp -r packages/ setup.sh Variables.sh 'xadmin@'$slave':~'
	ssh 'xadmin@'$slave 'bash setup.sh'
done

./setup.sh
./master_extra_setup.sh
./start_all.sh
./ZeppelinSetup.sh