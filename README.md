### Status
[![Build Status](https://travis-ci.org/iteratec/OsmDetailAnalysis.svg?branch=develop)](https://travis-ci.org/iteratec/OpenSpeedMonitor)

# OsmDetailAnalysis
Microservice that provides a dashboard to analyze web performance waterfall data.

It can be integrated with [OpenSpeedMonitor](https://github.com/iteratec/OpenSpeedMonitor).

Compatibility with OpenSpeedMonitor:

| Version OpenSpeedMonitor  | Compatible version OsmDetailAnalysis |
| ------------------------- |:------------------------------------:|
| 4.1.1                     | 1.0                                  |
| \>=4.1.2                  | 1.2.X                                |



Setup and integration with OpenSpeedMonitor
---
The DetailAnalysis (DA) is designed to run next to an existing OpenSeedMonitor instance and is not able run as a standalone application.
To connect both Applications, you need to edit their configurations:

As for Open Speed Monitor, the configuration file for the DA should be located in
```~/.grails/OsmDetailAnalysis-config.yml```
A sample file can be found in ```grails-app/conf/OsmDetailAnalysis-config.yml.sample```.
Create an API key in this file under the key ```grails.de.iteratec.osm.da.apiKeys```. The URL has to match with the one which is used by your OpenSpeedMonitor instance, which you want to connect.
For a local installation:
```
apiKeys:
 list:
    - key: aKeyLongerThanThis
      osmUrl: "http://localhost:8080"
 ```
 Note that one DA Instance can be used by multiple OSM Instances, just add more keys.
 On start of the DA, all Keys in the Database will be synchronized with this config file, while the config file is the single source of truth.
 
 Next you should update your OSM configuration, typically located in ``~/.grails/OpenSpeedMonitor-config.yml``:
 ````
environments:
     development:
       grails:
         de.iteratec.osm.detailAnalysis:
           enablePersistenceOfDetailAnalysisData: true
           microserviceUrl: http://localhost:8081
           apiKey: aKeyLongerThanthis
````
Note again, that the URL has to match your DA Url. If you want to run this setup in production, do the same thing for ``environments.productions...``.

Now everything you need is a running MonoDB. Currently we support version 3.2. 
````
mkdir mongodbdata
docker run -p 27017:27017 -v mongodbdata:/data/db mongo:3.2
````

Now start both applications:
```
cd OpenSpeedMonitor
./gradlew bootRun
cd ../OsmDetailAnalysis
./gradlew bootRun
```
The persistence of detail data can be configured in OpenSpeedMonitor. The persistence will be enabled per JobGroup. Login and navigate to ``http://localhost:8080/jobGroup/index``, click on the job group and set `Persist DetailData` to `true`.
Now every time you get a new result, the DA will be informed and will fetch all detail data. 

To show your data, navigate to ``https://osm.otto.iteratec.de/detailAnalysis/show``, set the filter and click on show. Note that you will only see data, since the Job Group was configured to persist detail data. If you want to load data before that, you have to be logged in, now you should see an extended button:

![Postload](documentation/postload.png)

Note that the process of downloading old data take some time. So depending on the amount this could take minutes to hours. A batch job will be created, where you can see the current progress.

Now you should be able to see something like this:

![Dashboard](https://raw.githubusercontent.com/wiki/iteratec/OpenSpeedMonitor/images/osm_v4_detailanalysis.jpg)

Storage
---
The amount of data need for the detail analysis can easily reach multiple gb, depending on your number jobs and their frequency. 
Therefore the default configuration will delete data older then 60 days. To change that, just edit the configuration file
```
grails:
    ...
    de:
      iteratec:
        osm:
          da:
            cleanup:
              daysToKeep: 60
 ```
No matter how long the DA will persist the data, you have always the possibility to post load old data, as long as your wpt server still has all the data needed.
 
Performing a Release
---

Releases are automated by [Travis CI](https://travis-ci.org/iteratec/OsmDetailAnalysis).

To create a release update the version in `build.gradle`, commit it and create a tag with the same version:

    $ git add build.gradle
    $ git commit -m "Release 1.2.3"
    $ git tag v1.2.3
    $ git push --tags
    $ git push

Travis will then build the project, release it to github and push to docker.


