do_install:append() {
    if [ -f ${D}${sysconfdir}/network/interfaces ]; then
        sed -i '/^iface wlan0 inet dhcp/i auto wlan0' ${D}${sysconfdir}/network/interfaces
    fi
}
