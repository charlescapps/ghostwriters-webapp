#!/bin/bash
export DATABASE_URL=postgres://postgres:password@localhost:5432/ghostwriters
java -cp target/classes:target/dependency/* net.capps.word.heroku.Main
