#!/bin/bash
# To use, copy all files in this directory into the home folder
# of the Raspberry Pi, then run this script as the pi user to
# set everything up

# Change these to match your team number!
radioIP="10.21.29.1"
staticIP="10.21.29.3"

jreFileName="OpenJDK11U-jre_aarch64_linux_hotspot_11.0.16_8.tar.gz"
jreDirName="jdk-11.0.16+8-jre"

echo "Extracting Java Runtime Environment"
tar -xf $jreFileName
mv $jreDirName jre

echo "Setting script execute permissions"
chmod +x Messenger/run.sh
chmod +x TaskManager/run.sh

echo "Creating TaskManager tasks directory"
mkdir TaskManager/tasks

echo "Installing systemd services"
sudo ln -s /home/pi/Messenger/messenger.service /etc/systemd/system/
sudo ln -s /home/pi/TaskManager/taskmanager.service /etc/systemd/system/
sudo systemctl enable messenger
sudo systemctl enable taskmanager

echo "Starting services"
sudo systemctl start messenger
sudo systemctl start taskmanager

echo "Setting static IP"
cp /etc/dhcpcd.conf dhcpcd.conf.bak # Make a backup of DHCP config
dhcpStaticConf="
interface eth0
static ip_address=${staticIP}/24
static routers=${radioIP}
static domain_name_servers=${radioIP}
"
echo "$dhcpStaticConf" | sudo tee -a /etc/dhcpcd.conf
sudo systemctl restart dhcpcd

echo "Setup complete!"
