# introsde-2016-assignment-2
**Second assignment | University of Trento**

Documentation about assignment 02: RESTful Services

## Project structure
The project is divided into 6 packages:

* ```introsde.rest.client```: it contains ```MyClient.java```, which performs requests to the server deployed on Heroku, and ```WriterOnFile```, a singleton class that stores the requests on the log files;
* ```introsde.rest.health```: it contains ```App.java``` and ```MyApplicationConfig.java``` to run the standalone server;
* ```introsde.rest.health.dao```: it contains ```LifeCoachDao.java``` that manages the connection to the database;
* ```introsde.rest.health.model```: it contains ```HealthMeasureHistory.java```, ```LifeStatus.java```, ```MeasureDefinition.java``` and ```Person.java``` that represent the corresponding tables in the database. They also contain methods to query the database;
* ```introsde.rest.health.resources```: it contains ```MeasureTypeResource.java```, ```PersonCollectionResource.java```, ```PersonHistoryResource.java```, ```PersonHistoryWithMidResource.java``` and ```PersonResource.java.java```. These classes declare which CRUD operations are allowed and how to perform them;
* ```introsde.rest.health.test```: it contains some tests for the model.

## Configuration files

The configuration files are:

* ```build.xml```: it contains all the targets to run the code;
* ```ivy.xml```: it contains all the dependencies needed to run the project and it downloads them.

## Setup

In order to clone the project and run it against the server deployed on Heroku:
* ```git clone https://github.com/SaraGasperetti/introsde-2016-assignment-2.git```

#### How to run the client

I worked in pair with Michele Bof (his repository is at https://github.com/michelebof/introsde-2016-assignment-2).  
His server link: https://introsde2016-assignment2.herokuapp.com/assignment/  
My server link: https://introsde2016-assignment-2.herokuapp.com/sdelab/  

In order to get response from my own server deployed on Heroku, execute: 
* ```cd introsde-2016-assignment-2```
* ```ant execute.myclient```


In order to get response from Michele Bof's server deployed on Heroku, execute: 
* ```cd introsde-2016-assignment-2```
* ```ant execute.client```
