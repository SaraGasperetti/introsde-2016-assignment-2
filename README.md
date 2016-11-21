# introsde-2016-assignment-2 (worked alone)
**Second assignment | University of Trento**

Documentation about assignment 02: RESTful Services

## Project structure
The project is divided into 6 packages:

* ```introsde.rest.client```: it contains ```MyClient.java```, which performs requests to the server deployed on Heroku, and ```WriterOnFile```, a singleton class that stores the requests on the log files;
* ```introsde.rest.health```: it contains ```App.java``` and ```MyApplicationConfig.java``` to run the standalone server;
* ```introsde.rest.health.dao```: it contains ```LifeCoachDao.java``` that manages the connection to the database;
* ```introsde.rest.health.model```: it contains ```HealthMeasureHistory.java```, ```LifeStatus.java```, ```MeasureDefinition.java``` and ```Person.java``` that represent the corresponding tables in the database. They also contain methods to query the database;
* ```introsde.rest.health.resources```: it contains ```MeasureTypeResource.java```, ```PersonCollectionResource.java```, ```PersonHistoryResource.java```, ```PersonHistoryWithMidResource.java``` and ```PersonResource.java.java```. These classes declare which CRUD operation are allowed and how to perform them;
* ```introsde.rest.health.test```: it contains some tests for the model.

## Configuration files

The configuration files are:

* ```build.xml```: it contains all the targets to run the code;
* ```ivy.xml```: it contains all the dependencies needed to run the project and it downloads them.

## Setup

```git clone https://github.com/SaraGasperetti/introsde-2016-assignment-2.git```
```cd introsde-2016-assignment-2```
```ant execute.client```

## How to run the client

* ```execute.client```: run the client
