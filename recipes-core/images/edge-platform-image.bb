SUMMARY = "Edge platform Linux image"

IMAGE_INSTALL = "packagegroup-core-boot \
                 kernel-modules \
                 linux-firmware-rpidistro-bcm43455 \
                 linux-firmware-rpidistro-bcm43456 \
                 wpa-supplicant \
                 iw \
                 ${CORE_IMAGE_EXTRA_INSTALL}"
IMAGE_FEATURES += "ssh-server-openssh allow-root-login"

IMAGE_LINGUAS = " "

LICENSE = "MIT"

inherit core-image

#set rootfs to 200 MiB by default
IMAGE_OVERHEAD_FACTOR ?= "1.0"
IMAGE_ROOTFS_SIZE ?= "204800"

ROOT_PASSWORD_HASH ?= ""

# Set ROOT_PASSWORD_HASH in build-rpi/conf/local.conf to avoid committing secrets
# in this layer. Example:
# ROOT_PASSWORD_HASH = "$6$...generated-with-openssl-passwd-6..."
python set_root_password_hash () {
    import os
    import re

    root_password_hash = d.getVar("ROOT_PASSWORD_HASH") or ""
    if not root_password_hash:
        return

    shadow_path = os.path.join(d.getVar("IMAGE_ROOTFS"), d.getVar("sysconfdir").lstrip("/"), "shadow")
    if not os.path.exists(shadow_path):
        return

    with open(shadow_path, "r", encoding="utf-8") as f:
        shadow_data = f.read()

    shadow_data = re.sub(r"^root:[^:]*:", "root:%s:" % root_password_hash, shadow_data, count=1, flags=re.MULTILINE)

    with open(shadow_path, "w", encoding="utf-8") as f:
        f.write(shadow_data)
}

ROOTFS_POSTPROCESS_COMMAND += " set_root_password_hash;"

IMAGE_ROOTFS_EXTRA_SPACE:append = "${@bb.utils.contains("DISTRO_FEATURES", "systemd", " + 4096", "", d)}"
