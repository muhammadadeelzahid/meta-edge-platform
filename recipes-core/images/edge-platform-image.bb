SUMMARY = "Edge platform Linux image"

IMAGE_INSTALL = "packagegroup-core-boot \
                 kernel-modules \
                 linux-firmware-rpidistro-bcm43455 \
                 linux-firmware-rpidistro-bcm43456 \
                 wpa-supplicant \
                 iw \
                 libubootenv-bin \
                 boot-mark-good \
                 ${CORE_IMAGE_EXTRA_INSTALL}"
IMAGE_FEATURES += "ssh-server-openssh allow-root-login read-only-rootfs"

# A/B dual-rootfs partition layout
WKS_FILE = "edge-platform-dual.wks.in"
IMAGE_FSTYPES:append = " ext4"

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

ROOT_SSH_AUTHORIZED_KEYS ?= ""

# Inject SSH authorized keys safely at build time
python set_ssh_authorized_keys () {
    import os

    ssh_keys = d.getVar("ROOT_SSH_AUTHORIZED_KEYS") or ""
    if not ssh_keys:
        return

    ssh_dir = os.path.join(d.getVar("IMAGE_ROOTFS"), "home", "root", ".ssh")
    if not os.path.exists(ssh_dir):
        os.makedirs(ssh_dir, mode=0o700)

    keys_file = os.path.join(ssh_dir, "authorized_keys")
    with open(keys_file, "w", encoding="utf-8") as f:
        f.write(ssh_keys + "\n")
    
    os.chmod(keys_file, 0o600)
}

ROOTFS_POSTPROCESS_COMMAND += " set_ssh_authorized_keys;"

# Create the /data mountpoint anchor on the root filesystem so WIC can mount the 4th partition there
create_data_mountpoint () {
    install -d ${IMAGE_ROOTFS}/data
}

ROOTFS_POSTPROCESS_COMMAND += " create_data_mountpoint;"

# Disable SSH password authentication for production security
disable_ssh_passwords () {
    if [ -f ${IMAGE_ROOTFS}/etc/ssh/sshd_config ]; then
        sed -i -e 's/^[#[:space:]]*PasswordAuthentication.*/PasswordAuthentication no/' ${IMAGE_ROOTFS}/etc/ssh/sshd_config
        sed -i -e 's/^[#[:space:]]*PermitEmptyPasswords.*/PermitEmptyPasswords no/' ${IMAGE_ROOTFS}/etc/ssh/sshd_config
    fi
}

ROOTFS_POSTPROCESS_COMMAND += " disable_ssh_passwords;"

IMAGE_ROOTFS_EXTRA_SPACE:append = "${@bb.utils.contains("DISTRO_FEATURES", "systemd", " + 4096", "", d)}"
