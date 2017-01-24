[![][NUBOMEDIA Logo]][NUBOMEDIA]

Copyright © 2016 [NUBOMEDIA]. Licensed under [Apache 2.0 License].
# Connectivity Manager

The Connectivity Manager is part of the [Nubomedia](http://www.nubomedia.eu) project. This component enable QoS requirements for Multimedia applications.
This is a short guide explaining how to deploy and install it.

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
bash <(curl -fsSkl https://raw.githubusercontent.com/nubomedia/connectivity-manager/master/bootstrap)
```

Afterwards the source code of the Connectivity-Manager is located in `/opt/nubomedia/connectivity-manager`.
Check if the NFVO and/or the MS-VNFM is not installed and started otherwise the Connectivity-Manager start will fail and you need to start it manually when the NFVO and the MS-VNFM are up and running.

In case the Connectivity-Manager are already installed you can start them manually using the provided script as described [here](#start-the-connectivity-manager-manually)

### Install the Connectivity Manager manually

1. Download the source code from git:

```bash
git clone https://github.com/nubomedia/connectivity-manager.git
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

What is NUBOMEDIA
-----------------

This project is part of [NUBOMEDIA], which is an open source cloud Platform as a
Service (PaaS) which makes possible to integrate Real Time Communications (RTC)
and multimedia through advanced media processing capabilities. The aim of
NUBOMEDIA is to democratize multimedia technologies helping all developers to
include advanced multimedia capabilities into their Web and smartphone
applications in a simple, direct and fast manner. To accomplish that objective,
NUBOMEDIA provides a set of APIs that try to abstract all the low level details
of service deployment, management, and exploitation allowing applications to
transparently scale and adapt to the required load while preserving QoS
guarantees.

Documentation
-------------

The [NUBOMEDIA] project provides detailed documentation including tutorials,
installation and [Development Guide].

Source
------

Source code for other NUBOMEDIA projects can be found in the [GitHub NUBOMEDIA
Group].

News
----

Follow us on Twitter @[NUBOMEDIA Twitter].

Issue tracker
-------------

Issues and bug reports should be posted to [GitHub Issues].

Licensing and distribution
--------------------------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.

Contribution policy
-------------------

You can contribute to the NUBOMEDIA community through bug-reports, bug-fixes,
new code or new documentation. For contributing to the NUBOMEDIA community,
drop a post to the [NUBOMEDIA Public Mailing List] providing full information
about your contribution and its value. In your contributions, you must comply
with the following guidelines

* You must specify the specific contents of your contribution either through a
  detailed bug description, through a pull-request or through a patch.
* You must specify the licensing restrictions of the code you contribute.
* For newly created code to be incorporated in the NUBOMEDIA code-base, you
  must accept NUBOMEDIA to own the code copyright, so that its open source
  nature is guaranteed.
* You must justify appropriately the need and value of your contribution. The
  NUBOMEDIA project has no obligations in relation to accepting contributions
  from third parties.
* The NUBOMEDIA project leaders have the right of asking for further
  explanations, tests or validations of any code contributed to the community
  before it being incorporated into the NUBOMEDIA code-base. You must be ready
  to addressing all these kind of concerns before having your code approved.

Support
-------

The NUBOMEDIA community provides support through the [NUBOMEDIA Public Mailing List].

[orchestrator]:http://openbaton.github.io
[vnfm]:https://github.com/nubomedia/nubomedia-msvnfm
[cma]:https://github.com/nubomedia/connectivity-manager-agent.git
[spring.io]:https://spring.io/
[bootstrap]:https://raw.githubusercontent.com/nubomedia/connectivity-manager/master/bootstrap
[Apache 2.0 License]: https://www.apache.org/licenses/LICENSE-2.0.txt
[Development Guide]: http://nubomedia.readthedocs.org/
[GitHub Issues]: https://github.com/nubomedia/bugtracker/issues
[GitHub NUBOMEDIA Group]: https://github.com/nubomedia
[NUBOMEDIA Logo]: http://www.nubomedia.eu/sites/default/files/nubomedia_logo-small.png
[NUBOMEDIA Twitter]: https://twitter.com/nubomedia
[NUBOMEDIA Public Mailing list]: https://groups.google.com/forum/#!forum/nubomedia-dev
[NUBOMEDIA]: http://www.nubomedia.eu
[LICENSE]:./LICENSE
