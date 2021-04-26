#! /bin/bash
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  APPLICATION_DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
done
APPLICATION_DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

if [[ -z "${JAVA_HOME}" ]]; then
  JAVA_PATH="java"
else
  JAVA_PATH="$JAVA_HOME/bin/java"
fi

"$JAVA_PATH" \
-jar -Dswing.aatext=true $APPLICATION_DIR/streamer.jar \
-log-startup-progress \
-splash-progress \
-splash-progress-color=127,206,0 \
-splash-progress-y=250 \
-splash-progress-x=15
