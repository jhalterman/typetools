#!/bin/bash
set -e
VERSION="$1"

echo "Publishing v$VERSION"

# Breathe
sleep 5

# See: https://github.com/cucumber/polyglot-release
polyglot-release "$VERSION"

echo "* Created release in Git"

# Publish to Central
git checkout "v$VERSION"
mvn deploy -Prelease

echo "* Deployed release to Maven Central"

# Publish Javadoc
mvn javadoc:javadoc
rm -rf target/gh-pages
git clone git@github.com:jhalterman/typetools.git target/gh-pages -b gh-pages
pushd target/gh-pages
  git rm -rf javadoc
  mkdir -p javadoc
  mv -v ../reports/apidocs/* javadoc
  git add -A -f javadoc
  git commit -m "Publish v$VERSION Javadoc"
  git push -fq origin gh-pages > /dev/null
popd

echo "* Published JavaDocs to https://jodah.net/typetools/javadoc/"

git checkout master
git status

echo "TODO:"
echo " * Approve the release on https://central.sonatype.com/"
echo " * Publish the release on https://github.com/cucumber/polyglot-release/releases/tag/v$VERSION"
