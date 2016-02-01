# Connectivity Manager

The Connectivity Manager is part of the [Nubomedia](http://www.nubomedia.eu) project. This component enable QoS requirements for Multimedia applications.
This is a short guide to deploy and install it.

## Getting Started

The Connectivity-Manager is implemented in java using the [spring.io] framework. This manager requires that all infrastructure is running:

* [Connectivity Manager Agent][cma] configured and running
* [Openbaton][orchestrator] is up and running
* The [MS-VNFM][vnfm] is up, running and registered to Openbaton

## Installation

You can install the Connectivity-Manager either automatically by downloading and executing the bootstrap or manually.
Both options are described below.

### Automatic installation and start

The [bootstrap] repository contains the script to install and start the Connectivity Manager automatically.
In order to do it you can run the following command:

```bash
bash <(curl -fsSkl https://raw.githubusercontent.com/tub-nubomedia/connectivity-manager/develop/bootstrap)
```

Afterwards the source code of the Connectivity-Manager is located in `/opt/nubomedia/connectivity-manager`.
Check if the NFVO and/or the MS-VNFM is not installed and started otherwise the Connectivity-Manager start will fail and you need to start it manually when the NFVO and the MS-VNFM are up and running.

In case the Connectivity-Manager are already installed you can start them manually using the provided script as described [here](#start-the-connectivity-manager-manually)

### Install the Connectivity Manager manually

1. Download the source code from git:

```bash
git clone https://github.com/tub-nubomedia/connectivity-manager.git
```

2. Change the properties file to reflect your infrastructure configuration:

```bash
vim connectivity-manager/src/main/resources/qos.properties
```

3. Run the provided script to create the base folder for properties file (and copy the file in it)

```bash
cd connectivity-manager/
./connectivity-manager.sh init
```

3. Compile the code using the provided script

```bash
cd connectivity-manager/
./connectivity-manager.sh compile
```

### Start the Connectivity Manager Manually

The Connectivity Manager can be started by executing the following command (in the directory connectivity-manager)

```bash
./connectivity-manager.sh start
```

Once the Connectivity Manager is started you can access the screen session that is in another window with the ms-vnfm running:

```bash
screen -x connectivity-manager
```
and move to the windows named `connectivity-manager`

## Configuration

The configuration can be fount in `/etc/nubomedia/qos.properties`.

Here you can configure:

* Connectivity Manager Agent URL
* NFVO
* Log Levels

After changing any configuration, you need to restart

## LICENSE

See [LICENSE][LICENSE]

#### Development

Do you want to contribute? Great! [Write us](mailto:nubomedia@av.tu-berlin.de)!.


[orchestrator]:http://openbaton.github.io
[vnfm]:https://github.com/tub-nubomedia/ms-vnfm
[spring.io]:https://spring.io/
[cma]:https://github.com/tub-nubomedia/connectivity-manager-agent.git
[bootstrap]:https://raw.githubusercontent.com/tub-nubomedia/connectivity-manager/develop/bootstrap
[NFV MANO]:http://www.etsi.org/deliver/etsi_gs/NFV-MAN/001_099/001/01.01.01_60/gs_nfv-man001v010101p.pdf
[nubomedia]:https://www.nubomedia.eu
[LICENSE]:./LICENSE
