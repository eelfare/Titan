# shellcheck disable=SC2164

dir=$(pwd)

# titan-manger
cd $dir
cd titanrtx-manager/titanrtx-manager-web/target
rsync titanrtx-manager-web-0.0.1-SNAPSHOT.jar txy:/root/

# titan-auto
cd $dir
cd titanrtx-manager/titanrtx-auto/target
rsync titanrtx-auto-0.0.1-SNAPSHOT.jar txy:/root/

# titan-commander
cd $dir
cd titanrtx-commander/target
rsync titanrtx-commander-0.0.1-SNAPSHOT.jar txy:/root/

#titan-agent
cd $dir
cd titanrtx-agent/target
rsync titanrtx-agent-0.0.1-SNAPSHOT.jar txy:/root/


#titan-cia-agent
cd $dir
cd titanrtx-cia-agent/target
rsync titanrtx-cia-agent-0.0.1-SNAPSHOT.jar txy:/root/
