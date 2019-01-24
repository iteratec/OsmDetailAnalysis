### Status
[![Build Status](https://travis-ci.org/iteratec/OsmDetailAnalysis.svg?branch=develop)](https://travis-ci.org/iteratec/OpenSpeedMonitor)

# OsmDetailAnalysis
Microservice that provides a dashboard to analyze web performance waterfall data.

It can be integrated with [OpenSpeedMonitor](https://github.com/iteratec/OpenSpeedMonitor).

Compatibility with OpenSpeedMonitor:

| Version OpenSpeedMonitor  | Compatible version OsmDetailAnalysis |
| ------------------------- |:------------------------------------:|
| 4.1.1                     | 1.0                                  |
| \>=4.1.2                  | 1.2.0                                  |
 
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
