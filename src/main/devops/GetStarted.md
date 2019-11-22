### installing java (linux) 
- updating machine 
```bash
sudo apt update 
sudo apt upgrade  
``` 
- installing default-jre and default-jdk
``` bash
sudo apt install default-jdk
```
- test if java installed 
```bash
java -version
javac -version
```

### installing java (windows) 
- go to ```https://www.oracle.com/technetwork/java/javase/downloads/index.html``` 
- download jdk program 
- follow the default installation process  

### installing git on pc  (linux)
installing git 
```
sudo apt install git
```
- to check git is downloaded ```git --version```
### installing git on pc  (windows)
- Download the most current version for your operating system from link ```https://git-scm.com/downloads```
- follow the default installation gide 
- to check git is downloaded ```git --version``` in cmd or you will find git app installed 


### clone Project from github (linux)
- creating dir for development 
```
mkdir Development
cd /Development
mkdir code
```
- clone Project 
```
git clone https://github.com/ali475/mena_masters.git
```
- switch to branch to dev
```
cd mena_masters
git checkout dev
```
### clone Project from github (windows)
- creating folder where code will be in 
- open git add cli 
- go to folder using command 
```
cd /path/to/folder
 ```
- clone Project 
```
git clone https://github.com/ali475/mena_masters.git
```
- switch to branch to dev
```
cd mena_masters
git checkout dev
```

## installing intellij IDEA community Edition 
- Download from link ```https://www.jetbrains.com/idea/download/```
- install use default installation gide 
- open project as maven project 
- run maven ```pom.xml``` to download dependencies 
