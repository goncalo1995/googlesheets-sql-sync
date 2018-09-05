# googlesheets-sql-sync

Keep your SQL database in sync with Google Sheets.

Use this to let users manually insert data using Google Sheets
while using all available SQL tooling for further processing.

SQL uses JDBC and bundles the PostgreSQL driver.
Additional drivers can be added any time.
If you sqlite or mysql support open an issue and support can be added in no time.


## Roadmap

- robust error handling
  - config file not exists / malformed / permissions / write
	- db
	- http request
- does sheet-title default to first sheet ?
- better sql statements
- ensure only one of do-sync and handle-code can happen at a time
- allow sheet + range specification
- detect schema change
  - log to stderr
  - send slack alert (default is no overwrite)
  - add column works without conflict (can still slack notify)
- spec for config
- validate interval > 0
- validate targets exist
- add optional has-header-row and add optional headers (which must be set when has-header-row = false)
- refresh token in a smarter way (not every single time)
- throttle API requests
- figure out how to wait for coroutines on close
- logging
- prometheus metrics
  - duration
  - target and table as labels
  - column count
  - row count
- find spreadsheet-title
- test.check
- circle ci
- use transaction for db
- github release
- support mysql
- support sqlite
- if localhost, use https://clojuredocs.org/clojure.java.browse



## Installation

java 8+


## Usage

- run init command
- fill out config json
- start server
- visit oauth url and confirm

    java -Xmx100m -Xms30m -jar target/uberjar/googlesheets-sql-sync-0.1.0-standalone.jar googlesheets_sql_sync.json

    lein repl

    lein uberjar

### Use a library

require `googlesheets-sql-sync.system`


## License

[MIT](./LICENSE)

