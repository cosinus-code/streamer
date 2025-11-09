#! /bin/bash
basedir=$(dirname "$0")
source "$basedir/echo-it.sh"
cd "$basedir" || exit

cd streamer-ui
show-info "Installing Streamer..."
if mvn clean install -Pjava-package; then
  #cp output/org.streamer.root.policy /usr/share/polkit-1/actions
  #cp output/image/streamer.png /usr/share/icons
  #show-info "Streamer was installed in ${STREAMER_HOME:-~/streamer}"
  show-info "Streamer was installed"
fi
