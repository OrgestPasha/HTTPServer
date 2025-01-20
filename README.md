##This is a simple HTTP server implemented in Java. 
It supports GET, POST, PUT, and DELETE requests to manage HTML files in the www directory. 
## Prerequisites 
- Java Development Kit (JDK) installed - json-20210307.jar library 
## Project Structure 
your-project/ ├── libs/ │ └── json-20210307.jar ├── www/ ├── Server.java └── bin/ 
## Compile the Java Code 
javac -cp "libs/json-20210307.jar" -d bin Server.java 
## Run the Java Program 
java -cp "libs/json-20210307.jar;bin" Server 
## Example Requests: 

- GET Request: curl -X GET http://localhost:4221/example.html 
- POST Request: curl -X POST -H "Content-Type: application/json" -d '{"filename": "example.html", "content": "This is a new HTML file"}' http://localhost:4221
- DELETE Request: curl -X DELETE http://localhost:4221/example.html
- PUT Request: curl -X PUT -H "Content-Type: application/json" -d '{"content": "This is an updated HTML file"}' http://localhost:4221/example.html
