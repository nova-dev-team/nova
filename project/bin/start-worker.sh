#!/bin/bash

# resolve links - $0 may be a softlink

this="$0"
while [ -h "$this" ]; do
  ls=`ls -ld "$this"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    this="$link"
  else
    this=`dirname "$this"`/"$link"
  fi
done

# convert relative path to absolute path
bin=`dirname "$this"`
script=`basename "$this"`
bin=`cd "$bin"; pwd`
this="$bin/$script"

export NOVA_HOME=`dirname "$this"`/..

export NOVA_CONF_DIR="$NOVA_HOME/conf"

# some Java parameters
if [ "$JAVA_HOME" != "" ]; then
  #echo "run java in $JAVA_HOME"
  export JAVA_HOME=$JAVA_HOME
fi
  
if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  #exit 1
fi

# CLASSPATH initially contains $NOVA_CONF_DIR
export CLASSPATH="$NOVA_CONF_DIR"
export CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar

# so that filenames w/ spaces are handled correctly in loops below
IFS=

# for developers, add Nova classes to CLASSPATH
if [ -d "$NOVA_HOME/bin/build" ]; then
  export CLASSPATH=${CLASSPATH}:$NOVA_HOME/bin/build
fi
for f in $NOVA_HOME/bin/nova-*.jar; do
  export CLASSPATH=${CLASSPATH}:$f;
done

# add libs to CLASSPATH
for f in $NOVA_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

export NOVA_LOG_DIR="$HADOOP_HOME/logs"
export NOVA_LOGFILE='worker.log'

# restore ordinary behaviour
unset IFS

export NOVA_OPTS="$NOVA_OPTS -Dnova.log.dir=$NOVA_LOG_DIR"
export NOVA_OPTS="$NOVA_OPTS -Dnova.log.file=$NOVA_LOGFILE"
export NOVA_OPTS="$NOVA_OPTS -Dnova.root.logger=${NOVA_ROOT_LOGGER:-INFO,console}"

cd $NOVA_HOME
java -server $NOVA_OPTS -classpath "$CLASSPATH" nova.worker.NovaWorker "$@"
cd -

