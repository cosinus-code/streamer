#! /bin/bash
basedir=$(dirname "$0")
source "$basedir/echo-it.sh"
cd "$basedir" || exit

run_or_continue_if_fail() {
    if ! "$@"; then
        echo "❌ Failed to run $*" >&2
    fi
}

run_or_die() {
    if ! "$@"; then
        echo "❌ Failed to install ${application_display_name} while running: $*" >&2
        exit 1
    fi
}

maven_property() {
  mvn help:evaluate -Dexpression="$1" -q -DforceStdout
}

cd streamer-ui

application_name=$(maven_property application.name)
application_display_name=$(maven_property application.display.name)
application_icon_name=$(maven_property application.icon.name)
application_version=$(maven_property project.version)
application_arguments=$(maven_property application.splash.progress.arguments)

show-info "Building ${application_display_name}..."
run_or_die mvn clean install -Pjava-package

show-info "Installing ${application_display_name}..."
if [ "$(uname -s)" = "Linux" ]; then
  application_deb_file="target/${application_name}_${application_version}.deb"
  application_rpm_file="target/${application_name}_${application_version}.rpm"
  application_desktop_file="target/assets/${application_name}.desktop"
  target_desktop_file="$HOME/.local/share/applications/${application_name}.desktop"

  if [ "$(grep -Ei 'debian|ubuntu|mint' /etc/os-release)" ]; then
    run_or_die sudo dpkg -i ${application_deb_file}
  fi

  if [ "$(grep -Ei 'fedora|redhat' /etc/os-release)" ]; then
    run_or_die sudo dnf install ${application_rpm_file}
  fi

  run_or_die sed -i "s/%U/${application_arguments}/" ${application_desktop_file}
  run_or_die cp ${application_desktop_file} ${target_desktop_file}
  run_or_die chmod +x ${target_desktop_file}
  run_or_die sudo update-desktop-database

  run_or_continue_if_fail sudo cp src/main/resources/org.${application_name}.root.policy /usr/share/polkit-1/actions
#  run_or_continue_if_fail sudo cp src/main/resources/image/${application_icon_name} /usr/share/icons
fi

show-info "${application_display_name} was installed"
