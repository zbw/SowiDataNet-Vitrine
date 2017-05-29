#SowiDataNet Vitrine

The SowiDataNet (SDN) - Vitrine is an institutional frontend for the [SowiDataNet](https://sowidatanet.de/).
It opens the view for a filtered access to the institutional data in a Corporate Design.

##System requirements
* Java JDK 1.8
* a free Port

## Play Framework
This application is built on the [Play Framework](https://www.playframework.com/documentation/2.3.x/Home).

## Installation
There are diffent ways installing and running the application using activator:  
* Using the [play console](https://www.playframework.com/documentation/2.3.x/PlayConsole) is for playing around  
* or in [production mode](https://www.playframework.com/documentation/2.3.x/Production)  
* Creating a standalone version using [activator dist](https://www.playframework.com/documentation/2.3.x/ProductionDist)  
or a package archive  
* You can also use the provided vitrine.sh script after configuring it at your needs. You can stop/update/start it.  
 

## Configuration
The application is started with the path to a configuration file. You can take the application.conf as an example.
In the configuration there is an institution section where the institution is configured. The institutional parameters 
start with the institutional path:


    zbw.name = Zentralbibliothek für Wirtschaftswissenschaften  
    zbw.handle = 27788  
    zbw.prot = http  
    zbw.host = ite-srv20.zbw-kiel.de  
    zbw.port = 8080  
    zbw.basepath = xmlui  
    zbw.basehandle =  10836  
    zbw.metadata = [dc.title  
                dc.contributor.author  
                dc.date.accessioned  
                dc.identifier.uri  
                dc.description  
                dc.identifier.isbn  
                dc.relation.ispartof  
                dc.relation.ispartofseries  
                dc.subject.country  
                dc.eiuindustrylist  
                dc.eiuregion  
                dc.country  
                dc.issue  
              ]
*name*: Name of the institution  
*handle*: handle of the institutional collection in Dspace without the prefix  
*prot*: the protocoll  
*port*: the port of the Dspace installation  
*basepath*: the basepath of the repository  
*basehandle*: the handle prefix  
*metadata*: show these metadata in the item view

For layouting the institutional view, copy a subdir in the assets.stylesheets folder and name it like the institutional path above.
You can make your less configuration there.


