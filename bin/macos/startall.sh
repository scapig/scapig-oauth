#!/bin/bash

sh start.sh tapi-api-definition 7000
sh start.sh tapi-api-scope 7010
sh start.sh tapi-application 7020
sh start.sh tapi-delegated-authority 7030
sh start.sh tapi-oauth 7040
sh start.sh tapi-oauth-login 7050
sh start.sh tapi-requested-authority 7060
sh start.sh tapi-developer 8000
sh start.sh tapi-developer-hub 8010
sh start.sh tapi-documentation 8020
sh start.sh tapi-gateway 8030
sh start.sh tapi-publisher 8040
sh start.sh tapi-hello 8080
