#!/bin/bash

URL="http://185.185.69.19:9090"
LOGIC_NAME="abromLogic-0.1-beta-all.jar"
SERVICE='AbromSoft.service'
SERVICE_PATH='/etc/systemd/system/'
JAVA_PATH='/mnt/data/abromSoftware/java/bin/java'
JAVA_DIRECTORY="/mnt/data/abromSoftware/java"
PROGRAM_DIRECTORY="/mnt/data/abromSoftware/abromLogic"
JAVA='jdk-11.0.17.tar.gz'
cd /mnt/data
ABROM_LOGIC_SIZE=$(curl -s $URL -u abrom:abromlogic | grep $LOGIC_NAME | awk '{print $3}')
JDK_ARCHIVED_SIZE=$(curl -s $URL -u abrom:abromlogic | grep $JAVA | awk '{print $3}')
JDK_SIZE=$((3 * JDK_ARCHIVED_SIZE))
NEED_MEMORY=$((ABROM_LOGIC_SIZE + JDK_SIZE))
FREE_MEMORY_KILO=$(df | grep /mnt/data | awk '{print $4}')
FREE_MEMORY=$((FREE_MEMORY_KILO * 1024))
echo "need memory is $NEED_MEMORY free memory is $FREE_MEMORY"
if [[ $NEED_MEMORY -gt $FREE_MEMORY ]]; then
  echo "not enough memory"
  exit
fi

mkdir -p $PROGRAM_DIRECTORY
if [ -f $SERVICE_PATH ] || [ -f "$PROGRAM_DIRECTORY/$LOGIC_NAME" ]; then
  echo abrom logic allready installed
else
  wget --user abrom --password abromlogic -4Nq -O "$PROGRAM_DIRECTORY/$LOGIC_NAME" --show-progress "${URL}/$LOGIC_NAME"
  if [ $? -ne 0 ]; then
    echo error with logic installation
  fi
  wget --user abrom --password abromlogic -4Nq --show-progress "${URL}/$SERVICE"
  if [ $? -ne 0 ]; then
    echo error with service installation
  fi

  mv -f "${SERVICE}" "${SERVICE_PATH}${SERVICE}"
  if [ $? -ne 0 ]; then
    echo error with service installation
  fi
  if [ ! -f $JAVA_PATH ]; then
    mkdir -p ${JAVA_DIRECTORY}
    wget --user abrom --password abromlogic -4Nq --show-progress "${URL}/$JAVA"
    tar --strip-components=1 -xxzf $JAVA -C ${JAVA_DIRECTORY}
        rm $JAVA
  fi

  sleep 1
  systemctl daemon-reload
  systemctl enable "${SERVICE}"
  systemctl enable avahi-daemon --quiet
  systemctl restart "${SERVICE}"
fi
