#!/bin/bash
#TRAVIS_TAG contains the tag assigned to this commit e. g. v1.2.3
version="${TRAVIS_TAG:1}"
v=( ${version//./ } )
version="${v[0]}.${v[1]}.${v[2]}"
majorTag="${v[0]}"
minorTag="${v[0]}.${v[1]}"
patchTag="${v[0]}.${v[1]}.${v[2]}"


function tagAndPush() {
   tag=$1
   docker tag iteratec/osm-detail-analysis iteratec/osm-detail-analysis:$tag
   docker push iteratec/osm-detail-analysis:$tag
   echo "Push tag $tag"
}

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build -t "iteratec/osm-detail-analysis" .

tagAndPush ${patchTag}
tagAndPush ${minorTag}
tagAndPush ${majorTag}
tagAndPush "latest"

