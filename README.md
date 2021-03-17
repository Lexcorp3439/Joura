# JourA
JourA - (**J**ournaling **A**dvanced) is a library for tracking and journaling objects runtime changes using static analysis.  


## Analisys strategis
There are three analysis strategis: _None_, _Default_, _Deep **(not completed)**_
### 1. NONE 
Each method call is followed by the transmission of a notification about the change of all class fields, 
regardless of whether they changed during the execution of the method or not.
### 2. DEFAULT 
In compilation time class`s methods are being analyzed. After that we get information about fileds, which change during method execution. 
**Important:** if fields is passed as an argument to a method, it will be tracked like editable, even if they are not changed inside the method.
### 3. DEEP _(not completed)_
Not implemented yet
