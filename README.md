# Gawati Package Sign

REST Service to Sign and Validate AKN metadata documents.

to build run:

```
mvn package
```

which will generate: `pkgsign-0.0.1-SNAPSHOT.war`, rename to pkgsign.war and deploy in Tomcat / Jetty.

APIs available: 

## Sign API 

Signs an AKN metadata document. 

Has the endpoint:

`/pkgsign/xml/doc/sign` - expects `multipart/form-data submission`

Expects the parameters as HTTP POST:

`input_file` - required, AKN metadata XML file
`public_key` - required, Public key file
`private_key` - required, Private key file

Returns:

Signed AKN XML returned as XML.
This is nothing but the AKN metadata xml with the signature embedded in the <akomaNtoso> tag.

## Validate API 

Validates a signed AKN metadata document. 

Has the endpoint:

`/pkgsign/xml/doc/validate` - expects `multipart/form-data submission`

Expects the parameters as HTTP POST:

`input_file` - required, AKN metadata XML file
`public_key` - required, Public key file

Returns:

A JSON string `{ valid: Boolean }` certifying the validity of the input file.

## Test API 

`/pkgsign/xml/doc/test` - http get. Returns version number.


## Generating Compatible Keys

Keys accepted by the Library can also be generated using ssh-keygen:

```
ssh-keygen -b 1024 -t dsa
```

and then convertd to pkcs8 format:

```
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key_file  -nocrypt > pkcs8_key
```

