#!/usr/bin/env bash

# Add the cuid module for perl
#cpan App::cpanminus
#cpanm Data::Cuid

#set -x
export LOC=http://localhost:9000/api
export LO=$(curl --silent --show-error -H "Content-Type: application/json" -X POST -d '{"username": "jpalinic","password": "test12345"}' "$LOC/user/login")
if [[ $LO == *"Forbidden"* ]]; then
echo "Register a Company and create the admin user"
curl --silent --show-error -H "Content-Type: application/json" -X POST -d '{"company": "Digital Cat", "firstName": "Damir","lastName": "Palinic","email": "damir@palinic.com","username": "dpalinic","password": "test12345"}' "$LOC/client/registration"
printf "\n"
sleep 12
echo "Login as the admin user"
export LO=$(curl --silent --show-error -H "Content-Type: application/json" -X POST -d '{"username": "dpalinic","password": "test12345"}' "$LOC/user/login")
printf "\n"
export AT=$(echo ${LO} | jq '.authToken' | sed -e 's/"//g')
#AT=${AT%$'\r'}
export RT=$(echo ${LO} | jq '.refreshToken' | sed -e 's/"//g')
echo "Get the identity status"
sleep 1
curl --silent --show-error --header "Authorization: Bearer ${AT}" ${LOC}/state/identity
printf "\n"
echo "Create a second user"
sleep 1
curl --silent --show-error --header "Authorization: Bearer ${AT}" -X POST -d '{"firstName": "Jelena","lastName": "Palinic","email": "jelena@palinic.com","username": "jpalinic","password": "test12345"}' ${LOC}/user
printf "\n"
echo "Login as the second user"
sleep 12
export LO=$(curl --silent --show-error -H "Content-Type: application/json" -X POST -d '{"username": "jpalinic","password": "test12345"}' "$LOC/user/login")
printf "\n"
export AT=$(echo ${LO} | jq '.authToken' | sed -e 's/"//g')
#AT=${AT%$'\r'}
export RT=$(echo ${LO} | jq '.refreshToken' | sed -e 's/"//g')
echo "Get the identity status"
sleep 1
curl --silent --show-error --header "Authorization: Bearer ${AT}" ${LOC}/state/identity
printf "\n"
else
export AT=$(echo ${LO} | jq '.authToken' | sed -e 's/"//g')
fi
#set +x

#curl -sS -H "Content-Type: application/json" -X POST -d '{"username": "dpalinic","password": "test12345"}' http://localhost:9000/api/user/login | jq '.authToken'

#refreshToken
export CT='"Content-Type: application/vnd.example-com.hello-world.hal+json; version=1.0"'
# or just for the latest version
# export CT='"Content-Type: application/json"'
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "first", "description": "first description"}}' http://localhost:9000/api/hello-worlds
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "first", "description": "first description"}}' http://localhost:9000/api/hello-worlds
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "second", "description": "second description"}}' http://localhost:9000/api/hello-worlds
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "third", "description": "third description"}}' http://localhost:9000/api/hello-worlds
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "fourth", "description": "fourth description"}}' http://localhost:9000/api/hello-worlds
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "fifth", "description": "fifth description"}}' http://localhost:9000/api/hello-worlds
printf "\n"
ID=$(perl -MData::Cuid=cuid -e 'print cuid()')
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "sixth", "description": "sixth description"}}' http://localhost:9000/api/hello-worlds/${ID}/creation/$(perl -MData::Cuid=cuid -e 'print cuid()')
printf "\n"
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X GET http://localhost:9000/api/hello-worlds/${ID}
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "seventh", "description": "seventh description"}}' http://localhost:9000/api/hello-worlds/creation/$(perl -MData::Cuid=cuid -e 'print cuid()')
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "eigth", "description": "eigth description"}}' http://localhost:9000/api/hello-worlds/$(perl -MData::Cuid=cuid -e 'print cuid()')/creation
printf "\n"
curl --silent --show-error --header "Authorization: Bearer ${AT}" -H ${CT} -X POST -d '{"helloWorld": {"name": "tenth", "description": "tenth description"}}' http://localhost:9000/api/hello-worlds/creation
printf "\n"

sleep 12
cqlsh localhost 4000 <<EOT
select * from hello_world.hello_world;
EOT

#curl http://example.com
#res=$?
#if test "$res" != "0"; then
#   echo "the curl command failed with: $res"
#fi

#http_code=$(curl -s -o out.html -w '%{http_code}'  http://www.google.com/linux;)

#if [[ $http_code -eq 200 ]]; then
#    exit 0
#fi

## decide which status you want to return for 404 or 500
#exit  204

# Refresh Token
#PUT http://localhost:9000/api/user/token