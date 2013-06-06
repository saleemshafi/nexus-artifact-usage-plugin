# Nexus Artifact Usage plugin
This nexus plugin allows the user to get the list of artifacts depending on a given artifact; some people call this backward dependencies.

## How to build :
Simply clone this repo and run mvn clean install

## How to install :
Unzip the archive in target named artifact-usage-plugin-0.10.0-SNAPSHOT-bundle.zip to NEXUSHOME/nexus/WEB-INF/plugin-repository and restart nexus.

## How to use :
Simply search for an artifact and select the "Artifact Usage" tab : 
![Screenshot](https://raw.github.com/anthonydahanne/nexus-artifact-usage-plugin/master/screenshots/artifact-usage.png "Artifact Usage")

This plugin stores the dependencies information in memory every time an artifact is uploaded or deleted; so every time you restart nexus, all the information is lost.
Don't worry though, you can automate the artifact usage indexing through a scheduled task :
![Screenshot](https://raw.github.com/anthonydahanne/nexus-artifact-usage-plugin/master/screenshots/scheduled-task.png "Scheduled Task")


## Thanks to :
- The initial contributor [Saleem Shafi from ebay](https://github.com/saleemshafi/)
- [Jason](https://github.com/jvoegele) for making me aware of this great plugin
- [Sonatype Nexus team](https://github.com/sonatype/nexus-oss) for their time helping me out on their [chat](https://www.hipchat.com/gW26B2y2Z) !