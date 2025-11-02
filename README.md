# GIS-MP-API-basic
A sample project that works with the API of the GIS MP service.

GIS MP (_ГИС МТ_) is a Russian digital system created for the labeling and traceability of goods. 
It ensures the control over the movement of marked products, reducing counterfeiting and improving the consumers safety.

Their official web-site: [link](https://markirovka.ru).

This is a sample utility that can be used in order to register new products from a larger Java application.

## Some details

It basically consists of a single script file: `CrptApi.java`.

In order to use the utility we must create an instance of this object.
The constructor takes two arguments (`TimeUnit` and `requestLimit`), that are required to prevent
the server from being overloaded with requests. 

> You can look at the source code for more details.

In order to register a product document we use the `createDocument()` method of CrptApi.
It takes two arguments: the document itself (a simple data POJO) and the user's signature (can be obtained from the service).
The method returns a completable future containing the server response in it; so, many threads can possibly
use a single instance of CrptApi without making any troubles to each other.

## Post-scriptum

**It is not a production ready solution**, but it can be easily expanded (most of the 'dummy' placeholders can
be overriden through inheritance).

The project contains some JUnit tests I made for myself; these basically have no value to anyone else.

Also, since I have no access to the service, some of the code snippets may be invalid
(the only proof of the stability is the `403` response from the server). It only applies
to the requests, though; everything else is actually stable.
