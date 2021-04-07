# PlayerTracker
![Build with Maven](https://github.com/DESTROYMC-NET/PlayerTracker/workflows/Build%20with%20Maven/badge.svg) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A plugin to track first join and last logout times.
## Features
- Super easy setup. Drop plugin into `plugins` folder. Setup the config and restart your server.
- Imports from current player data. Bukkit tracks this information, but it's a bit weird.
- Use `/ptreload` to reload the plugin.
- Use either JSON files or MYSQL to save information. MYSQL is a bit weird, so I recommend using JSON. This is my first attempt at using databases, so shit might hit the fan.

![Image](https://raw.githubusercontent.com/DESTROYMC-NET/PlayerTracker/master/image1.png)

![Image](https://raw.githubusercontent.com/DESTROYMC-NET/PlayerTracker/master/image2.png)
## Download
To download the latest build, head over to the [Actions](https://github.com/DESTROYMC-NET/PlayerTracker/actions) page and grab the latest build. It will download a zip file. Inside the zip file is the jar file.
