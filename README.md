# meta-device-base

Yocto layer providing an embedded Linux platform for edge devices.
Includes foundations for secure networking, system initialization, and OTA updates.

## Overview

This repository contains the custom Yocto layer (`meta-device-base`) to build the
platform image and configuration for Raspberry Pi.

Key features include:

- **Boot & Recovery:** Uses U-Boot with A/B partition routing and automatic fallback.
- **Read-Only Core:** The primary OS is read-only to prevent file corruption.
- **OverlayFS:** Uses Yocto's `overlayfs` to make paths like `/etc`, `/var`, and `/home` writable by storing changes on a separate data partition.
- **Partitioning:** Creates 4 partitions: Boot (FAT32), Rootfs A (ext4), Rootfs B (ext4), and Data (ext4).
- **Init System:** Uses `systemd`.
- **OTA Updates:** Uses a `boot-mark-good` service to wait 30 seconds before confirming an update is successful.
- **Connectivity & Security:** Includes `wpa_supplicant` for Wi-Fi and allows OpenSSH access using keys only (passwords disabled).

This layer is meant to serve as a solid foundation, allowing project-specific
applications (like UI or custom daemons) to be maintained in separate app layers.

## Tools & Technologies Used

- **Yocto Project (Poky):** The core build framework.
- **WIC:** Generates the final partitioned OS image (`.wic.bz2`).
- **U-Boot:** The bootloader, which handles switching between A/B software slots.
- **libubootenv:** Allows the Linux OS to read and modify U-Boot variables safely.
- **systemd:** The Linux init system and service manager.
- **wpa_supplicant & iw:** Tools for wireless networking.
- **OpenSSH:** Standard suite for secure ssh access.
- **ext4:** The Linux filesystem chosen to format the root partitions.
- **rpidistro-bcm43456:** Firmware for the Raspberry Pi Wi-Fi and Bluetooth.

## Build Notes

1. Ensure your layer is added to `bblayers.conf`.
2. Add the following to your `build-rpi/conf/local.conf`:

```bitbake
DISTRO ?= "device-base"
MACHINE ?= "raspberrypi4-64"
RPI_USE_U_BOOT = "1"
INHERIT += "rm_work"

# Provide your Master SSH Key here
ROOT_SSH_AUTHORIZED_KEYS = "ssh-ed25519 AAAAC3Nz... dev@workstation"
```

3. Build the platform image:

```bash
bitbake device-base-image
```

## QEMU Simulation

To test the image without physical hardware, set `MACHINE = "qemuarm64"` in
`local.conf` and build normally. The image recipe automatically disables the
read-only rootfs and OverlayFS features for QEMU targets. Launch with:

```bash
runqemu qemuarm64 nographic
```

## Secrets

This repository is kept free of real credentials.

Local secrets such as Wi-Fi credentials and the root password hash should be
provided through your build configuration, for example in:

```text
build-rpi/conf/local.conf
```

## License

This layer is licensed under the MIT license. See [COPYING.MIT](COPYING.MIT).
