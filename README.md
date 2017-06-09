# bimserver
This project is designed to show the usage of Java API for <a href="bimserver.org">BIMServer</a> projects.
It's using gradle instead of recommended maven requested by my client in Singapore.

BIMServer is widely used in Europe and will replace traditionally method of project management in property development.

Pre-requisite:
1) Latest JDK, 1.8.
2) Gradle
3) A running BIM Server

<b><u>Usage</u></b>
1) download or clone this project
2) cd to the project folder
3) modify the hostServer and userEmail in config/config file according to your BIMServer config
4) run 'gradle shadowJar'
5) run 'java -jar build/libs/CIDBim-all.jar'

