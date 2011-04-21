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

# some Java parameters
if [ "$JAVA_HOME" != "" ]; then
  #echo "run java in $JAVA_HOME"
  export JAVA_HOME=$JAVA_HOME
fi
  
if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  #exit 1
fi

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

# restore ordinary behaviour
unset IFS

java -classpath "$CLASSPATH" org.apache.pivot.wtk.DesktopApplicationContext nova.ui.NovaUI


