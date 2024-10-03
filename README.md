# HTML validator spider

A Java app that recursively validates HTML pages and CSS files.

The app starts at a base URL, and recursively visits all same-origin HTML pages linked with an `<a>` Tag, and CSS files referenced through a `<link rel=stylesheet>` or `@import`ed by another CSS file. All encountered HTML and CSS files are validated using the open source [nu validator](https://validator.nu/).

HTML pages will only be validated if the response has a `content-type` header with an essence of `text/html`. Pages with a different value, including pages with no `content-type` header, will be treated as non-HTML and ignored.

CSS files will always be validated, but it will be considered an error if a `<link rel=stylesheet>` or a CSS `@import` leads to a URL where the response `content-type` is not `text/css`.

## Configuration

Configuration is done through environment variables:

| Name                       | Syntax                          | Description                                                                                       | Required |
|----------------------------|---------------------------------|---------------------------------------------------------------------------------------------------|----------|
| `BASE_URL`                 | An absolute http: or https: URL | The starting point for crawling                                                                   | yes      |
| `TREAT_WARNINGS_AS_ERRORS` | -                               | If set to any value, validation warnings will be treated as errors and cause a non-zero exit code | no       |
| `IGNORE_CSS`               | -                               | If set to any value, CSS files will be ignored and no CSS validation will take place.             | no       |

## Usage

You can use the app with docker (recommended) or directly with Maven.

### with docker

Run:

```commandline
docker build --tag=html-validator-spider .
docker run -e BASE_URL=... html-validator-spider
```

Note that the dockerfile automatically builds and uses the latest version of the nu validator from github.

### with maven

[Install Apache Maven](https://maven.apache.org/install.html), then run:

```commandline
mvn package
```

Then run the produced .jar file.

Note that this will use the latest version of the nu validator from Maven central repository, which tends to be out of date. To use the latest version, either run [with docker](#with-docker) directly, or use the Dockerfile as instruction on how to build and include the latest version of the validator.  

### Behavior

Validation results will be written to standard output and all other messages to standard error. 

If an invalid configuration was provided, the exit code will be a negative number. Otherwise, the exit code is the number of pages with errors (respecting the `TREAT_WARNINGS_AS_ERRORS` configuration).

## Tests

To run unit tests, [install Apache Maven](https://maven.apache.org/install.html), then run:

```commandline
mvn test
```

To run end-to-end tests, install docker, then run:

```commandline
./e2e/e2e-test.sh
```

## License

Apache-2.0
