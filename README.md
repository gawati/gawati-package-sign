
Keys accepted by the Library can also be generated using ssh-keygen:

```
ssh-keygen -b 1024 -t dsa
```

and then convertd to pkcs8 format:

```
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key_file  -nocrypt > pkcs8_key
```
