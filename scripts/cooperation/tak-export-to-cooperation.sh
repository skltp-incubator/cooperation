#!/bin/bash

#=============================================================================
# Simple check to avoid running multiple instances via cron
#=============================================================================
lock=/tmp/tak-export-to-cooperation-lockdir

if mkdir ${lock}; then
  trap 'rmdir ${lock}' EXIT
else
  exit 1
fi

#=============================================================================
# tak-export-to-cooperation.sh
#
# Export TAK-data to file from TAK-database and send the file to a
# cooperation-application instance.
#=============================================================================
# break on error
set -e

cd `dirname $0`
#=============================================================================
# Configure per environment.
#=============================================================================
# setup environment
. tak-export-to-cooperation-env-setup.sh

#=============================================================================
# Export TAK data and upload to cooperation.
#=============================================================================
mkdir -p ${tmpDir}
logFile=${tmpDir}/tak-export-to-cooperation.log
# clear logfile
date > ${logFile}
echo "Begin: environment check" >> ${logFile}
# check that groovy is available
groovy -version >> ${logFile} 2>&1
# check locale
locale >> ${logFile} 2>&1
echo "Done: environment check" >> ${logFile}

#-----------------------------
# Export TAK data.
#-----------------------------
echo "Begin: TAK-export: `date`" >> ${logFile}
# TAK export file name - MUST be named like this for the cooperation-app to
# parse TAK-site and TAK-environment from filename
exportFile=${tmpDir}/takdump_${takSite}_${takEnvironment}.json
groovy -Dgroovy.grape.report.downloads=true -Dgrape.root=./grape_repo TakCooperationExport.groovy \
    --platform ${takSite} -env ${takEnvironment} -u ${takDbUser} -p ${takDbPassword} -d ${takDbName} -s ${takDbHost} \
    > ${exportFile} 2>> ${logFile}
echo "Done: TAK-export: `date`" >> ${logFile}

#-----------------------------
# Upload TAK data.
#-----------------------------
echo "Begin: TAK-upload: `date`" >> ${logFile}

echo "Will do: scp <options> ${exportFile} ${cooperationUser}@${cooperationHost}:${cooperationRemotePath}" >> ${logFile}

scp -o "PreferredAuthentications publickey" \
    -o "StrictHostKeyChecking no" \
    -o "UserKnownHostsFile /dev/null" \
    -o "IdentityFile ${sshIdentityFile}" \
    ${exportFile} ${cooperationUser}@${cooperationHost}:${cooperationRemotePath} >> ${logFile} 2>&1

echo "Done: TAK-upload: `date`" >> ${logFile}
