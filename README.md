# meta-edge-platform

Embedded Linux platform layer built with Yocto for edge applications, including
networking, Qt UI, and CAN integration.

## Overview

This repository contains a custom Yocto layer for building an edge-platform
image on Raspberry Pi hardware. The layer currently includes:

- a custom image recipe
- Wi-Fi provisioning through `wpa_supplicant`
- SSH enabled on boot
- UART console support
- Raspberry Pi firmware and module selection needed for networking

The layer is intended to grow with the project and can later include recipes
for Qt applications, CAN services, and other platform components.

## Layer Name

The Yocto layer directory is:

```text
meta-edge-platform
```

## Build Notes

Add the layer to `bblayers.conf`, then build:

```bash
bitbake edge-platform-image
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
