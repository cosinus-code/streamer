#! /bin/bash
basedir=$(dirname "$0")
source "$basedir/echo-it.sh"
cd "$basedir" || exit

show-info "Packaging Streamer..."
cd streamer-ui
if mvn clean package spring-boot:repackage; then
  show-info "Deploying Streamer..."
  cp -a output/.  ${STREAMER_HOME:-~/streamer}
  #cp output/org.streamer.root.policy /usr/share/polkit-1/actions
  #cp output/image/streamer.png /usr/share/icons
  show-info "Streamer was deployed in ${STREAMER_HOME:-~/streamer}"
fi
