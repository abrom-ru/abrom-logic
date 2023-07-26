#!/bin/bash
route = 'java'
LOGIC_NAME="/mnt/data/root/abromLogic-0.1-beta-all.jar"
SERVICE="ClimateControl.service"
rm -f ${LOGIC_NAME}
rm -f "/identifier.sqlite"
systemctl stop "${SERVICE}"
systemctl disable "${SERVICE}"
rm -f "/etc/systemd/system/${SERVICE}"
systemctl --system daemon-reload
sleep 1

