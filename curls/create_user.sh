username=$1
password=$2
cookie=$3
curl -H "Content-Type: application/json" -d "{\"username\": \"${username}\", \"password\": \"${password}\"}" --cookie "$cookie" http://localhost:8080/api/users
