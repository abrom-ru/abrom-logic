#!/bin/bash
route = 'java'
URL='http://185.185.69.19'

PROGRAM_DIRECTORY="/mnt/data/abromSoftware/"
LOGIC_NAME="/mnt/data/root/abromLogic-0.1-beta-all.jar"
JAVA="/mnt/data/abromSoftware/"
SERVICE="AbromSoft.service"
rm -rf "$PROGRAM_DIRECTORY"
systemctl stop "${SERVICE}"
systemctl disable "${SERVICE}"
rm -f "/etc/systemd/system/${SERVICE}"
systemctl --system daemon-reload
sleep 1
