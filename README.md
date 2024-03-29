# Decentralised Content Negotiation

An implementation for the approach enabling decentralised content negotiation. The [paper](https://dmkg-workshop.github.io/papers/paper9797.pdf) was presented at Data Management for Knowledge Graphs (DMKG - ESWC 2023)

### Prerequisites
* Java 9+
* Curl or Postman for testing.

### Building the project
To build the project, just use:

```shell
./gradlew build
```

The Gradle task `build` generates a fat-jar in the build/libs directory.

### Running DCN project

To start the project:

```shell
java -jar .\build\libs\decentralised-cn-0.0.1-SNAPSHOT.jar
```

Open your browser to http://localhost:8080. You should see the message `DCN project is running ...`.

### Examples

#### Media Type Dimension

To request an HTML representation of the resource identified by the URI http://www.uniprot.org/taxonomy/3330, 
send the `GET` request:
```shell
curl -v http://localhost:8080/dcn/api/media-type?iri=http://www.uniprot.org/taxonomy/3330 -H "accept: text/html"
```
If a request for a Turtle representation of the resource identified by the URI http://www.uniprot.org/taxonomy/3330,
is sent directly with the request:
```shell
curl -v http://www.uniprot.org/taxonomy/3330 -H "accept: text/turtle"
```

No adequate representation is served. However, if a request is sent through the API, i.e. send the `GET` request:
```shell
curl -v http://localhost:8080/dcn/api/media-type?iri=http://www.uniprot.org/taxonomy/3330 -H "accept: text/turtle"
```
A representation is served after utilizing the `Equavalance Links` such as `owl:sameAs`
to look for potential acceptable representations.


#### Profile Dimension

A `Profile` in this implementation is a SHACL shape graphs. Examples of such profiles could be found in the [profiles folder](/src/main/resources/static/profiles). 

To ask for a representation of the resource identified by the URI http://www.uniprot.org/taxonomy/3330,
that validates the `example-shape-graph-1` profile, a request of this type should be sent:
```shell
curl -v http://localhost:8080/dcn/api/profile?iri=http://www.uniprot.org/taxonomy/3330 -H "accept-profile: http://localhost:8080/profiles/example-shape-graph-1.ttl"
```

Similarly, if other profiles are preferred, only a change of the `accept-profile` header is needed.

### Test with Swagger

A Swagger-UI is also available at the address http://localhost:8080/swagger-ui/index.html to enable easy testing if so desired. 
