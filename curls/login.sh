#!/bin/bash
username=$1
password=$2
curl -v -u "$username:$password" -X POST http://localhost:8080/api/login
