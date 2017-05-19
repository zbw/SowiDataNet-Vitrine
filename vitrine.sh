#!/bin/bash
#
#  vitrine   Startup script for the sdn vitrine application#
#
# chkconfig: 2345 75 25
# description: vitrine
# processname: vitrine
# Source function library.


### BEGIN vars
APP=vitrine
APPNAME=vitrine
PORT=9000
BASE_DIR=/home/ottk/apps
PLAY_VERSION=
APPLICATION_PATH=${BASE_DIR}/${APP}
PROD_PATH="${APPLICATION_PATH}/target/universal/stage"
CONF_PATH="${PROD_PATH}/conf/ite20.conf"
USER=ottk
APPLICATION_SECRET=X?4WDTKsb3s:zCs0bVhFS]S@28U9:To1oOPIH6Vrxet5osACcGfX>vUyiLe4D
export APPLICATION_SECRET


# Specific project configuration environment :
export _JAVA_OPTIONS="-Xms1024m -Xmx1024m -XX:PermSize=512m -XX:MaxPermSize=512m"
### END vars

# Exit immediately if a command exits with a nonzero exit status.
set -e

update() {
    echo "Updating"

    cd $APPLICATION_PATH || exit

    unset GIT_DIR
    # Update repo
    git pull

    cd $APPLICATION_PATH
    # Creating new project (MUST BE ON THE GOOD DIR)
    $APPLICATION_PATH/activator clean compile stage
}

start() {
    echo "Starting server"
    cd $APPLICATION_PATH
    $PROD_PATH/bin/$APPNAME -Dhttp.port=$PORT -Dconfig.file=$CONF_PATH &
}

stop() {
    echo "Stopping server"
    if [ -f $PROD_PATH"/RUNNING_PID" ];then
        kill `cat $PROD_PATH"/RUNNING_PID"`
    fi
}

case "$1" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        start
    ;;
    update)
        update
    ;;
    force-restart)
        stop
        update
        start
    ;;
    *)
        echo $"Usage: $0 {start|force-restart|stop|restart|update}"
esac

exit 0
