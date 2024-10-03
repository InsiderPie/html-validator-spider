# HTML validator spider

A Java app that recursively validates HTML pages and CSS files.

The app starts at a base URL, and recursively visits all same-origin HTML pages linked with an `<a>` Tag, and CSS files referenced through a `<link rel=stylesheet>` or `@import`ed by another CSS file. All encountered HTML and CSS files are validated using the open source [nu validator](https://validator.nu/).

## Configuration

Configuration is done through environment variables:

| Name | Syntax | Description          | Required |
|------|--------|----------------------|----------|
| `BASE_URL` | An absolute http: or https: URL | The starting point for crawling | yes |
| `TREAT_WARNINGS_AS_ERRORS` | - | If set to any value, validation warnings will be treated as errors and cause a non-zero exit code | no |

## Usage

[Install Apache Maven](https://maven.apache.org/install.html), then run:

```
mvn package
```

Validation results will be written to standard output and all other messages to standard error. 

If an invalid configuration was provided, the exit code will be a negative number. Otherwise, the exit code is the number of pages with errors (respecting the `TREAT_WARNINGS_AS_ERRORS` configuration).

## License

Apache-2.0
