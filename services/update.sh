#!/bin/bash
URL="http://185.185.69.19"
LOGIC_NAME='abromLogic-0.1-beta-all.jar'
PROGRAM_DIRECTORY="/mnt/data/abromSoftware/abromLogic"
SERVICE='AbromSoft.service'
SERVICE_PATH='/etc/systemd/system/'

cd /mnt/data
if [ ! -f $SERVICE_PATH/$SERVICE ] || [ ! -f "$PROGRAM_DIRECTORY/$LOGIC_NAME" ]; then
  echo abrom logic not installed
else
  rm -f "$PROGRAM_DIRECTORY/$LOGIC_NAME"
  rm -f "/etc/systemd/system/${SERVICE}"
  wget --user abrom --password abromlogic --user abrom --password abromlogic -O "$PROGRAM_DIRECTORY/$LOGIC_NAME" -4Nq --show-progress "${URL}/$LOGIC_NAME"
  if [ $? -ne 0 ]; then
    echo error with logic installation
  fi
  wget --user abrom --password abromlogic --user abrom --password abromlogic -4Nq --show-progress "${URL}/$SERVICE"
  if [ $? -ne 0 ]; then
    echo error with service installation
  fi

  mv -f "${SERVICE}" "${SERVICE_PATH}${SERVICE}"
  if [ $? -ne 0 ]; then
    echo error with service installation
  fi
  sleep 1
  systemctl disable $SERVICE
  systemctl --system daemon-reload
  systemctl enable $SERVICE
  sleep 1
  systemctl restart "${SERVICE}"
fi
