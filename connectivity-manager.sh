#!/bin/bash

#!/bin/bash

source ./gradle.properties

_version=${version}

_connectivity_manager_base="/opt/nubomedia/connectivity-manager"
_connectivity_manager_config_file="/etc/nubomedia/qos.properties"
#_message_queue_base="apache-activemq-5.11.1"
#_openbaton_config_file=/etc/openbaton/openbaton.properties

function check_already_running {
        result=$(screen -ls | grep connectivity-manager | wc -l);
        if [ "${result}" -ne "0" ]; then
                echo "Connectivity-manager is already running.."
		exit;
        fi
}

function start {

    if [ ! -d build/  ]
        then
            compile
    fi

    #check_activemq
    #check_mysql
    #check_already_running
    #if [ 0 -eq $? ]
        #then
	    #screen -X eval "chdir $PWD"
	#screen -c screenrc -d -m -S ms-vnfm -t ms-vnfm java -jar "build/libs/ms-vnfm-$_version.jar"
	pushd "${_connectivity_manager_base}"
	#                                                                   build/libs/nubomedia-paas-api-0.1-SNAPSHOT.jar
	screen -d -m -S connectivity-manager -t connectivity-manager java -jar "/opt/nubomedia/connectivity-manager/build/libs/connectivity-manager-$_version.jar" --spring.config.location=file:${_connectivity_manager_config_file}
	    #screen -c screenrc -r -p 0
	popd
    #fi
}

function stop {
    if screen -list | grep "connectivity-manager"; then
	    screen -S connectivity-manager -p 0 -X stuff "exit$(printf \\r)"
    fi
}

function restart {
    kill
    start
}


function kill {
    if screen -list | grep "connectivity-manager"; then
	    screen -ls | grep connectivity-manager | cut -d. -f1 | awk '{print $1}' | xargs kill
    fi
}

function init {
    if [ ! -f $_connectivity_manager_config_file ]; then
        if [ $EUID != 0 ]; then
            echo "creating the directory and copying the file"
            sudo -E sh -c "mkdir /etc/nubomedia; cp ${_connectivity_manager_base}/src/main/resources/qos.properties ${_connectivity_manager_config_file}"
            #echo "copying the file, insert the administrator password" | sudo -kS cp ${_nubomedia_paas_base}/src/main/resources/paas.properties ${_nubomedia_config_file}
        else
            echo "creating the directory"
            mkdir /etc/nubomedia
            echo "copying the file"
            cp ${_connectivity_manager_base}/src/main/resources/paas.properties ${_connectivity_manager_config_file}
        fi
    else
        echo "Properties file already exist"
    fi
}
function compile {
    ./gradlew build -x test
}

function tests {
    ./gradlew test
}

function clean {
    ./gradlew clean
}

function end {
    exit
}
function usage {
    echo -e "Connectivity-Manager\n"
    echo -e "Usage:\n\t ./connectivity-manager.sh [compile|start|stop|test|kill|clean]"
}

##
#   MAIN
##

if [ $# -eq 0 ]
   then
        usage
        exit 1
fi

declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "clean" )
            clean ;;
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "stop" )
            stop ;;
        "init" )
            init ;;
        "restart" )
            restart ;;
        "compile" )
            compile ;;
        "kill" )
            kill ;;
        "test" )
            tests ;;
        * )
            usage
            end ;;
    esac
    if [[ $? -ne 0 ]];
    then
	    exit 1
    fi
done