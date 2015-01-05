#!/bin/bash
export DATABASE_URL=postgres://postgres:password@localhost:5432/wordattack
java -cp target/classes:target/dependency/* net.capps.word.heroku.Main
